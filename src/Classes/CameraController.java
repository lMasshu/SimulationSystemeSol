package Classes;

import javafx.geometry.Point3D;
import javafx.scene.Camera;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.Node;
import javafx.scene.input.PickResult;

public class CameraController {

    public enum Mode {
        FREE_FLY,
        ORBITAL
    }

    private Camera camera;
    private Mode currentMode = Mode.FREE_FLY;

    // Rotations pour le mode Free Fly
    private Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);

    // État de la souris
    private double mouseOldX, mouseOldY;

    // État du clavier pour un mouvement fluide
    private boolean moveForward, moveBackward, moveLeft, moveRight, moveUp, moveDown;
    private boolean isShiftDown;

    // Vecteurs de direction (mis à jour dynamiquement)
    private Point3D cameraForward = new Point3D(0, 0, 1);
    private Point3D cameraRight = new Point3D(1, 0, 0);
    private Point3D cameraUp = new Point3D(0, -1, 0);

    // État Orbital
    private Astre targetAstre;
    private double orbitDistance = 1000;
    
    // Inertie / Vitesse
    private double currentSpeedX = 0;
    private double currentSpeedY = 0;
    private double currentSpeedZ = 0;
    private final double ACCELERATION = 2.0;
    private final double DECELERATION = 0.90; // Friction
    private final double MAX_SPEED = 25.0;

    // Transition fluide (Orbite)
    private boolean isTransitioning = false;
    private Point3D transitionStartPos;
    private double transitionProgress = 0;

    public CameraController(Camera camera) {
        this.camera = camera;
        this.camera.getTransforms().addAll(rotateY, rotateX);
    }

    // --- MISE À JOUR PAR FRAME ---
    public void update() {
        updateCameraDirections();

        if (isTransitioning) {
            handleTransition();
        } else if (currentMode == Mode.FREE_FLY) {
            handleFreeFlyMovement();
        } else if (currentMode == Mode.ORBITAL && targetAstre != null) {
            handleOrbitalMovement();
        }
    }

    private Point3D getTargetPosition() {
        return targetAstre != null && targetAstre.position != null ? targetAstre.position : Point3D.ZERO;
    }

    private void handleTransition() {
        transitionProgress += 0.05; // Vitesse de la transition
        if (transitionProgress >= 1.0) {
            transitionProgress = 1.0;
            isTransitioning = false;
            currentMode = Mode.ORBITAL;
            
            // Ajuster l'orbite initiale pour ne pas sauter
            Point3D toCamera = camera.localToParent(Point3D.ZERO).subtract(getTargetPosition());
            orbitDistance = toCamera.magnitude();
        }

        // Interpolation cubique simple (Ease-in-out)
        double t = transitionProgress;
        double ease = t < 0.5 ? 2 * t * t : -1 + (4 - 2 * t) * t;

        // Position cible idéale en fin de transition
        Point3D targetPos = getTargetPosition().add(cameraForward.multiply(-orbitDistance));

        camera.setTranslateX(transitionStartPos.getX() + (targetPos.getX() - transitionStartPos.getX()) * ease);
        camera.setTranslateY(transitionStartPos.getY() + (targetPos.getY() - transitionStartPos.getY()) * ease);
        camera.setTranslateZ(transitionStartPos.getZ() + (targetPos.getZ() - transitionStartPos.getZ()) * ease);
    }

    private void handleFreeFlyMovement() {
        double mult = isShiftDown ? 10.0 : 1.0;
        
        // Calcul de l'accélération demandée
        double accelX = 0, accelY = 0, accelZ = 0;

        if (moveForward) { accelX += cameraForward.getX(); accelY += cameraForward.getY(); accelZ += cameraForward.getZ(); }
        if (moveBackward) { accelX -= cameraForward.getX(); accelY -= cameraForward.getY(); accelZ -= cameraForward.getZ(); }
        if (moveLeft) { accelX -= cameraRight.getX(); accelY -= cameraRight.getY(); accelZ -= cameraRight.getZ(); }
        if (moveRight) { accelX += cameraRight.getX(); accelY += cameraRight.getY(); accelZ += cameraRight.getZ(); }
        if (moveUp) { accelY -= 1; } // Axe absolu
        if (moveDown) { accelY += 1; } // Axe absolu

        // Appliquer l'accélération
        currentSpeedX += accelX * ACCELERATION * mult;
        currentSpeedY += accelY * ACCELERATION * mult;
        currentSpeedZ += accelZ * ACCELERATION * mult;

        // Limiter la vitesse si on ne freine pas avec l'inertie
        // Appliquer la friction pour l'inertie (glissement)
        currentSpeedX *= DECELERATION;
        currentSpeedY *= DECELERATION;
        currentSpeedZ *= DECELERATION;

        // Appliquer la position
        camera.setTranslateX(camera.getTranslateX() + currentSpeedX);
        camera.setTranslateY(camera.getTranslateY() + currentSpeedY);
        camera.setTranslateZ(camera.getTranslateZ() + currentSpeedZ);
    }

    private void handleOrbitalMovement() {
        // En mode orbital, on force la position par rapport à la cible et on regarde toujours selon notre rotation actuelle.
        // La rotation de la camera définit où elle regarde, donc sa position est: cible - avant * distance
        Point3D pos = getTargetPosition().add(cameraForward.multiply(-orbitDistance));
        camera.setTranslateX(pos.getX());
        camera.setTranslateY(pos.getY());
        camera.setTranslateZ(pos.getZ());
    }

    // --- CIBLAGE ---
    public void focusOn(Astre astre, double defaultDistance) {
        this.targetAstre = astre;
        this.orbitDistance = defaultDistance;
        this.transitionStartPos = new Point3D(camera.getTranslateX(), camera.getTranslateY(), camera.getTranslateZ());
        this.transitionProgress = 0;
        this.isTransitioning = true;
        
        // Orienter grossièrement la caméra vers l'astre 
        // Note: une vraie orientation interpolée nécessiterait des quaternions, on va rester simple
        Point3D targetPos = getTargetPosition();
        Point3D dir = targetPos.subtract(transitionStartPos).normalize();
        
        double angleY = Math.toDegrees(Math.atan2(dir.getX(), dir.getZ()));
        double angleX = Math.toDegrees(Math.asin(-dir.getY()));
        
        rotateY.setAngle(angleY);
        rotateX.setAngle(angleX);
        
        updateCameraDirections();
    }

    public void detach() {
        if (currentMode == Mode.ORBITAL) {
            currentMode = Mode.FREE_FLY;
            targetAstre = null;
        }
    }

    // --- MATRICES ---
    private void updateCameraDirections() {
        Transform transform = camera.getLocalToSceneTransform();
        cameraForward = transform.deltaTransform(0, 0, 1).normalize();
        cameraRight = transform.deltaTransform(1, 0, 0).normalize();
        cameraUp = transform.deltaTransform(0, -1, 0).normalize();
    }

    // --- GESTIONNAIRES D'ÉVÉNEMENTS ---
    public void setupControls(Scene scene) {
        scene.addEventHandler(MouseEvent.MOUSE_PRESSED, this::handleMousePressed);
        scene.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::handleMouseDragged);
        scene.addEventHandler(ScrollEvent.SCROLL, this::handleScroll);
        scene.addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
        scene.addEventHandler(KeyEvent.KEY_RELEASED, this::handleKeyReleased);
        scene.addEventHandler(MouseEvent.MOUSE_CLICKED, this::handleMouseClicked);
    }

    private void handleMouseClicked(MouseEvent event) {
        // Détection du picking (clic sur un objet 3D)
        PickResult res = event.getPickResult();
        if (res != null && res.getIntersectedNode() != null) {
            Node node = res.getIntersectedNode();
            if (node.getUserData() instanceof Astre) {
                Astre clickedAstre = (Astre) node.getUserData();
                focusOn(clickedAstre, clickedAstre.diametre / 2 + 500); // Distance par défaut
                return;
            }
        }
        
        // Si clic dans le vide (pas un Astre), détacher si on était en focus
        if (event.getButton() == MouseButton.PRIMARY) {
            detach();
        }
    }

    private void handleMousePressed(MouseEvent event) {
        mouseOldX = event.getSceneX();
        mouseOldY = event.getSceneY();
    }

    private void handleMouseDragged(MouseEvent event) {
        double mousePosX = event.getSceneX();
        double mousePosY = event.getSceneY();
        double mouseDeltaX = mousePosX - mouseOldX;
        double mouseDeltaY = mousePosY - mouseOldY;

        mouseOldX = mousePosX;
        mouseOldY = mousePosY;

        if (event.isSecondaryButtonDown()) {
            // Rotation de la caméra (Tourner la tête)
            // En mode libre, ça tourne la tête. En mode orbital, vu qu'on update la position 
            // par rapport à "cameraForward", tourner la tête fait physiquement pivoter la caméra AUTOUR de l'astre !
            double modifier = 0.15;
            rotateY.setAngle(rotateY.getAngle() + mouseDeltaX * modifier);
            rotateX.setAngle(rotateX.getAngle() - mouseDeltaY * modifier);
            
            // Empêcher de faire un "looping" qui inverserait la caméra (gimbal lock style)
            if (rotateX.getAngle() > 89) rotateX.setAngle(89);
            if (rotateX.getAngle() < -89) rotateX.setAngle(-89);

        } else if (event.isPrimaryButtonDown() || event.isMiddleButtonDown()) {
            if (currentMode == Mode.FREE_FLY) {
                // Déplacement de la caméra (Panning)
                double panSpeed = 0.5;
                if (event.isShiftDown()) panSpeed *= 5;
                
                camera.setTranslateX(camera.getTranslateX() - cameraRight.getX() * mouseDeltaX * panSpeed);
                camera.setTranslateY(camera.getTranslateY() - cameraRight.getY() * mouseDeltaX * panSpeed);
                camera.setTranslateZ(camera.getTranslateZ() - cameraRight.getZ() * mouseDeltaX * panSpeed);

                camera.setTranslateX(camera.getTranslateX() + cameraUp.getX() * mouseDeltaY * panSpeed);
                camera.setTranslateY(camera.getTranslateY() + cameraUp.getY() * mouseDeltaY * panSpeed);
                camera.setTranslateZ(camera.getTranslateZ() + cameraUp.getZ() * mouseDeltaY * panSpeed);
            }
        }
    }

    private void handleScroll(ScrollEvent event) {
        if (currentMode == Mode.ORBITAL) {
            // Zoom vers/loin de la cible
            double zoomFactor = event.getDeltaY() > 0 ? 0.9 : 1.1; // 10% de zoom stat
            if (isShiftDown) zoomFactor = event.getDeltaY() > 0 ? 0.5 : 2.0;

            orbitDistance *= zoomFactor;
            // Éviter de rentrer physiquement complètement dans la cible indéfiniment
            if (orbitDistance < targetAstre.diametre / 4) {
                orbitDistance = targetAstre.diametre / 4;
            }
        } else {
            // Zoom = avancer brusquement dans la direction caméra
            double zoomSpeed = 80.0;
            if (isShiftDown) zoomSpeed *= 5;

            if (event.getDeltaY() > 0) {
                currentSpeedX += cameraForward.getX() * zoomSpeed;
                currentSpeedY += cameraForward.getY() * zoomSpeed;
                currentSpeedZ += cameraForward.getZ() * zoomSpeed;
            } else {
                currentSpeedX -= cameraForward.getX() * zoomSpeed;
                currentSpeedY -= cameraForward.getY() * zoomSpeed;
                currentSpeedZ -= cameraForward.getZ() * zoomSpeed;
            }
        }
    }

    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.SHIFT) isShiftDown = true;
        
        switch (event.getCode()) {
            case Z: moveForward = true; break;
            case S: moveBackward = true; break;
            case Q: moveLeft = true; break;
            case D: moveRight = true; break;
            case SPACE: moveUp = true; break;
            case CONTROL: moveDown = true; break;
            default: break;
        }

        // Si on appuie sur ZQSD, on sort du mode orbital automatiquement
        if (moveForward || moveBackward || moveLeft || moveRight || moveUp || moveDown) {
            detach();
        }
    }

    private void handleKeyReleased(KeyEvent event) {
        if (event.getCode() == KeyCode.SHIFT) isShiftDown = false;

        switch (event.getCode()) {
            case Z: moveForward = false; break;
            case S: moveBackward = false; break;
            case Q: moveLeft = false; break;
            case D: moveRight = false; break;
            case SPACE: moveUp = false; break;
            case CONTROL: moveDown = false; break;
            default: break;
        }
    }

    public void setPositionAndLookAt(Point3D pos, Point3D target) {
        camera.setTranslateX(pos.getX());
        camera.setTranslateY(pos.getY());
        camera.setTranslateZ(pos.getZ());
        
        Point3D dir = target.subtract(pos).normalize();
        double angleY = Math.toDegrees(Math.atan2(dir.getX(), dir.getZ()));
        double angleX = Math.toDegrees(Math.asin(-dir.getY()));
        
        rotateY.setAngle(angleY);
        rotateX.setAngle(angleX);
        updateCameraDirections();
    }
}
