package Classes.camera;

import Classes.model.Astre;
import javafx.geometry.Point3D;
import javafx.scene.Camera;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.input.ScrollEvent;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;

/**
 * Contrôleur de caméra 3D de type "Simulateur Spatial".
 *
 * Supporte deux modes :
 *  - {@link CameraMode#FREE_FLY}  : vol libre avec inertie et déplacement ZQSD.
 *  - {@link CameraMode#ORBITAL}   : orbite stabilisée autour d'un astre cible.
 *
 * L'orientation utilise deux rotations composées (Yaw Y + Pitch X) pour éviter
 * le gimbal lock partiel, avec un clamp de pitch à ±89°.
 */
public class CameraController {

    private Camera camera;
    private CameraMode currentMode = CameraMode.FREE_FLY;

    // --- Rotations  ---
    private final Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);

    // --- Souris ---
    private double mouseOldX, mouseOldY;

    // --- Clavier ---
    private boolean moveForward, moveBackward, moveLeft, moveRight, moveUp, moveDown;
    private boolean isShiftDown;

    // --- Vecteurs de direction (recalculés chaque frame) ---
    private Point3D cameraForward = new Point3D(0, 0, 1);
    private Point3D cameraRight   = new Point3D(1, 0, 0);
    private Point3D cameraUp      = new Point3D(0, -1, 0);

    // --- Mode Orbital ---
    private Astre targetAstre;
    private double orbitDistance = 1000;

    // --- Inertie (Free Fly) ---
    private double currentSpeedX = 0, currentSpeedY = 0, currentSpeedZ = 0;
    private static final double ACCELERATION  = 2.0;
    private static final double DECELERATION  = 0.90;
    @SuppressWarnings("unused")
    private static final double MAX_SPEED     = 25.0; // Réservé pour usage futur

    // --- Transition fluide vers une orbite ---
    private boolean  isTransitioning   = false;
    private Point3D  transitionStartPos;
    private double   transitionProgress = 0;

    // ---------------------------------------------------------------
    public CameraController(Camera camera) {
        this.camera = camera;
        this.camera.getTransforms().addAll(rotateY, rotateX);
    }

    // ===================================================================
    //  BOUCLE DE MISE À JOUR (appelée chaque frame)
    // ===================================================================
    public void update() {
        updateCameraDirections();

        if (isTransitioning) {
            handleTransition();
        } else if (currentMode == CameraMode.FREE_FLY) {
            handleFreeFlyMovement();
        } else if (currentMode == CameraMode.ORBITAL && targetAstre != null) {
            handleOrbitalMovement();
        }
    }

    // ===================================================================
    //  MODES DE DÉPLACEMENT
    // ===================================================================
    private void handleTransition() {
        transitionProgress += 0.05;
        if (transitionProgress >= 1.0) {
            transitionProgress = 1.0;
            isTransitioning = false;
            currentMode = CameraMode.ORBITAL;
            Point3D toCamera = camera.localToParent(Point3D.ZERO).subtract(getTargetPosition());
            orbitDistance = toCamera.magnitude();
        }

        double t    = transitionProgress;
        double ease = t < 0.5 ? 2 * t * t : -1 + (4 - 2 * t) * t;

        Point3D targetPos = getTargetPosition().add(cameraForward.multiply(-orbitDistance));
        camera.setTranslateX(transitionStartPos.getX() + (targetPos.getX() - transitionStartPos.getX()) * ease);
        camera.setTranslateY(transitionStartPos.getY() + (targetPos.getY() - transitionStartPos.getY()) * ease);
        camera.setTranslateZ(transitionStartPos.getZ() + (targetPos.getZ() - transitionStartPos.getZ()) * ease);
    }

    private void handleFreeFlyMovement() {
        double mult = isShiftDown ? 10.0 : 1.0;
        double accelX = 0, accelY = 0, accelZ = 0;

        if (moveForward)  { accelX += cameraForward.getX(); accelY += cameraForward.getY(); accelZ += cameraForward.getZ(); }
        if (moveBackward) { accelX -= cameraForward.getX(); accelY -= cameraForward.getY(); accelZ -= cameraForward.getZ(); }
        if (moveLeft)     { accelX -= cameraRight.getX();   accelY -= cameraRight.getY();   accelZ -= cameraRight.getZ();   }
        if (moveRight)    { accelX += cameraRight.getX();   accelY += cameraRight.getY();   accelZ += cameraRight.getZ();   }
        if (moveUp)       { accelY -= 1; }
        if (moveDown)     { accelY += 1; }

        currentSpeedX += accelX * ACCELERATION * mult;
        currentSpeedY += accelY * ACCELERATION * mult;
        currentSpeedZ += accelZ * ACCELERATION * mult;

        // Inertie spatiale (glissement)
        currentSpeedX *= DECELERATION;
        currentSpeedY *= DECELERATION;
        currentSpeedZ *= DECELERATION;

        camera.setTranslateX(camera.getTranslateX() + currentSpeedX);
        camera.setTranslateY(camera.getTranslateY() + currentSpeedY);
        camera.setTranslateZ(camera.getTranslateZ() + currentSpeedZ);
    }

    private void handleOrbitalMovement() {
        Point3D pos = getTargetPosition().add(cameraForward.multiply(-orbitDistance));
        camera.setTranslateX(pos.getX());
        camera.setTranslateY(pos.getY());
        camera.setTranslateZ(pos.getZ());
    }

    // ===================================================================
    //  CIBLAGE
    // ===================================================================
    public void focusOn(Astre astre, double defaultDistance) {
        this.targetAstre         = astre;
        this.orbitDistance       = defaultDistance;
        this.transitionStartPos  = new Point3D(camera.getTranslateX(), camera.getTranslateY(), camera.getTranslateZ());
        this.transitionProgress  = 0;
        this.isTransitioning     = true;

        Point3D dir    = getTargetPosition().subtract(transitionStartPos).normalize();
        double angleY  = Math.toDegrees(Math.atan2(dir.getX(), dir.getZ()));
        double angleX  = Math.toDegrees(Math.asin(-dir.getY()));
        rotateY.setAngle(angleY);
        rotateX.setAngle(angleX);
        updateCameraDirections();
    }

    public void detach() {
        if (currentMode == CameraMode.ORBITAL) {
            currentMode = CameraMode.FREE_FLY;
            targetAstre = null;
        }
    }

    // ===================================================================
    //  UTILITAIRES INTERNES
    // ===================================================================
    private Point3D getTargetPosition() {
        return (targetAstre != null && targetAstre.position != null)
            ? targetAstre.position : Point3D.ZERO;
    }

    private void updateCameraDirections() {
        Transform t  = camera.getLocalToSceneTransform();
        cameraForward = t.deltaTransform(0, 0, 1).normalize();
        cameraRight   = t.deltaTransform(1, 0, 0).normalize();
        cameraUp      = t.deltaTransform(0, -1, 0).normalize();
    }

    // ===================================================================
    //  ENREGISTREMENT DES EVENTS
    // ===================================================================
    public void setupControls(Scene scene) {
        scene.addEventHandler(MouseEvent.MOUSE_PRESSED,  this::handleMousePressed);
        scene.addEventHandler(MouseEvent.MOUSE_DRAGGED,  this::handleMouseDragged);
        scene.addEventHandler(ScrollEvent.SCROLL,        this::handleScroll);
        scene.addEventHandler(KeyEvent.KEY_PRESSED,      this::handleKeyPressed);
        scene.addEventHandler(KeyEvent.KEY_RELEASED,     this::handleKeyReleased);
        scene.addEventHandler(MouseEvent.MOUSE_CLICKED,  this::handleMouseClicked);
    }

    // ===================================================================
    //  HANDLERS
    // ===================================================================
    private void handleMouseClicked(MouseEvent event) {
        PickResult res = event.getPickResult();
        if (res != null && res.getIntersectedNode() != null) {
            Node node = res.getIntersectedNode();
            if (node.getUserData() instanceof Astre) {
                Astre clicked = (Astre) node.getUserData();
                focusOn(clicked, clicked.diametre / 2 + 500);
                return;
            }
        }
        if (event.getButton() == MouseButton.PRIMARY) {
            detach();
        }
    }

    private void handleMousePressed(MouseEvent event) {
        mouseOldX = event.getSceneX();
        mouseOldY = event.getSceneY();
    }

    private void handleMouseDragged(MouseEvent event) {
        double dx = event.getSceneX() - mouseOldX;
        double dy = event.getSceneY() - mouseOldY;
        mouseOldX = event.getSceneX();
        mouseOldY = event.getSceneY();

        if (event.isSecondaryButtonDown()) {
            // Rotation de la tête / orbite autour de la cible
            rotateY.setAngle(rotateY.getAngle() + dx * 0.15);
            rotateX.setAngle(Math.max(-89, Math.min(89, rotateX.getAngle() - dy * 0.15)));

        } else if (event.isPrimaryButtonDown() || event.isMiddleButtonDown()) {
            if (currentMode == CameraMode.FREE_FLY) {
                double panSpeed = event.isShiftDown() ? 2.5 : 0.5;
                camera.setTranslateX(camera.getTranslateX() - cameraRight.getX() * dx * panSpeed);
                camera.setTranslateY(camera.getTranslateY() - cameraRight.getY() * dx * panSpeed);
                camera.setTranslateZ(camera.getTranslateZ() - cameraRight.getZ() * dx * panSpeed);
                camera.setTranslateX(camera.getTranslateX() + cameraUp.getX() * dy * panSpeed);
                camera.setTranslateY(camera.getTranslateY() + cameraUp.getY() * dy * panSpeed);
                camera.setTranslateZ(camera.getTranslateZ() + cameraUp.getZ() * dy * panSpeed);
            }
        }
    }

    private void handleScroll(ScrollEvent event) {
        if (currentMode == CameraMode.ORBITAL) {
            double factor = event.getDeltaY() > 0 ? (isShiftDown ? 0.5 : 0.9) : (isShiftDown ? 2.0 : 1.1);
            orbitDistance = Math.max(targetAstre.diametre / 4, orbitDistance * factor);
        } else {
            double speed = isShiftDown ? 400.0 : 80.0;
            double sign  = event.getDeltaY() > 0 ? 1 : -1;
            currentSpeedX += cameraForward.getX() * speed * sign;
            currentSpeedY += cameraForward.getY() * speed * sign;
            currentSpeedZ += cameraForward.getZ() * speed * sign;
        }
    }

    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.SHIFT) isShiftDown = true;
        switch (event.getCode()) {
            case Z: moveForward   = true; break;
            case S: moveBackward  = true; break;
            case Q: moveLeft      = true; break;
            case D: moveRight     = true; break;
            case SPACE:   moveUp   = true; break;
            case CONTROL: moveDown = true; break;
            default: break;
        }
        if (moveForward || moveBackward || moveLeft || moveRight || moveUp || moveDown) detach();
    }

    private void handleKeyReleased(KeyEvent event) {
        if (event.getCode() == KeyCode.SHIFT) isShiftDown = false;
        switch (event.getCode()) {
            case Z: moveForward   = false; break;
            case S: moveBackward  = false; break;
            case Q: moveLeft      = false; break;
            case D: moveRight     = false; break;
            case SPACE:   moveUp   = false; break;
            case CONTROL: moveDown = false; break;
            default: break;
        }
    }

    // ===================================================================
    //  API PUBLIQUE
    // ===================================================================
    public void setPositionAndLookAt(Point3D pos, Point3D target) {
        camera.setTranslateX(pos.getX());
        camera.setTranslateY(pos.getY());
        camera.setTranslateZ(pos.getZ());

        Point3D dir    = target.subtract(pos).normalize();
        rotateY.setAngle(Math.toDegrees(Math.atan2(dir.getX(), dir.getZ())));
        rotateX.setAngle(Math.toDegrees(Math.asin(-dir.getY())));
        updateCameraDirections();
    }
}
