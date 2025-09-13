import java.util.Arrays;
import java.util.Vector;
import Classes.Astre;
import Classes.OrbitePlanete;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Point3D;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.stage.Stage;

public class App extends Application {
    public final int WIDTH = 800;
    public final int HEIGHT = 600;
    public final double SCALE_DISTANCE = 150;    // 150 pixels = 1 UA
    public final double SCALE_DIAMETER = 1e4;    // 10 000 km par pixel pour les diamètres

    // Périodes orbitales des planètes (en années terrestres)
    public double periodOrbitalMercure = 0.2408;
    public double periodOrbitalVenus = 0.6152;
    public double periodOrbitalTerre = 1.0000;
    public double periodOrbitalMars = 1.8809;
    public double periodOrbitalJupiter = 11.862;
    public double periodOrbitalSaturne = 29.447;
    public double periodOrbitalUranus = 84.016;
    public double periodOrbitalNeptune = 164.8;

    // Périhélies (en UA)
    public double perihelieMercure = 0.3075;
    public double perihelieVenus = 0.7184;
    public double perihelieTerre = 0.9833;
    public double perihelieMars = 1.3814;
    public double perihelieJupiter = 4.9504;
    public double perihelieSaturne = 9.0481;
    public double perihelieUranus = 18.3755;
    public double perihelieNeptune = 29.8203;

    // Aphélies (en UA)
    public double aphelieMercure = 0.4667;
    public double aphelieVenus = 0.7282;
    public double aphelieTerre = 1.0167;
    public double aphelieMars = 1.6660;
    public double aphelieJupiter = 5.4549;
    public double aphelieSaturne = 10.1159;
    public double aphelieUranus = 20.0747;
    public double aphelieNeptune = 30.3271;

    // Inclinaisons orbitales (par rapport au plan de l'écliptique)
    public double inclinaisonMercure = 7.005;
    public double inclinaisonVenus = 3.395;
    public double inclinaisonTerre = 0.000; // Référence
    public double inclinaisonMars = 1.850;
    public double inclinaisonJupiter = 1.303;
    public double inclinaisonSaturne = 2.489;
    public double inclinaisonUranus = 0.773;
    public double inclinaisonNeptune = 1.770;

    // Longitude du nœud ascendant (en degrés)
    public double longitudeNoeudMercure = 48.331;
    public double longitudeNoeudVenus = 76.680;
    public double longitudeNoeudTerre = 0.000;
    public double longitudeNoeudMars = 49.558;
    public double longitudeNoeudJupiter = 100.464;
    public double longitudeNoeudSaturne = 113.665;
    public double longitudeNoeudUranus = 74.006;
    public double longitudeNoeudNeptune = 131.784;

    // Argument du périhélie (en degrés)
    public double argumentPerihelieMercure = 29.124;
    public double argumentPerihelieVenus = 54.884;
    public double argumentPerihelieTerre = 114.208;
    public double argumentPerihelieMars = 286.502;
    public double argumentPerihelieJupiter = 273.867;
    public double argumentPerihelieSaturne = 339.392;
    public double argumentPerihelieUranus = 96.998;
    public double argumentPerihelieNeptune = 276.336;

    private PerspectiveCamera camera;
    private final Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private double mousePosX, mousePosY;
    private double mouseOldX, mouseOldY;
    private Astre soleil, mercure, venus, terre, mars, jupiter, saturne, uranus, neptune;

    public static void main(String[] args) {
        launch(args);
    }

    // Méthode pour calculer la direction de la caméra
    private Point3D getCameraDirection() {
        Point3D forward = new Point3D(0, 0, 1);
        forward = rotateY.transform(forward);
        forward = rotateX.transform(forward);
        return forward.normalize();
    }

    // Initialisation de la caméra
    private void initCamera(Scene scene) {
        camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(100000.0);
        camera.getTransforms().addAll(rotateX, rotateY);
        camera.setTranslateX(WIDTH / 2);
        camera.setTranslateY(HEIGHT / 2);
        camera.setTranslateZ(-500);
        scene.setCamera(camera);
    }

    // Configuration des contrôles de caméra
    private void setupCameraControls(Scene scene) {
        // Mouse scroll for zoom
        scene.setOnScroll(event -> {
            double zoomSpeed = 50.0;
            Point3D direction = getCameraDirection();
            
            if (event.getDeltaY() > 0) {
                // Scroll up - zoom in (move forward)
                camera.setTranslateX(camera.getTranslateX() + direction.getX() * zoomSpeed);
                camera.setTranslateY(camera.getTranslateY() + direction.getY() * zoomSpeed);
                camera.setTranslateZ(camera.getTranslateZ() + direction.getZ() * zoomSpeed);
            } else {
                // Scroll down - zoom out (move backward)
                camera.setTranslateX(camera.getTranslateX() - direction.getX() * zoomSpeed);
                camera.setTranslateY(camera.getTranslateY() - direction.getY() * zoomSpeed);
                camera.setTranslateZ(camera.getTranslateZ() - direction.getZ() * zoomSpeed);
            }
            event.consume();
        });

        scene.setOnMousePressed(event -> {
            if (event.isSecondaryButtonDown()) {
                mousePosX = event.getSceneX();
                mousePosY = event.getSceneY();
                mouseOldX = event.getSceneX();
                mouseOldY = event.getSceneY();
            }
        });

        scene.setOnMouseDragged(event -> {
            if (event.isSecondaryButtonDown()) {
                mouseOldX = mousePosX;
                mouseOldY = mousePosY;
                mousePosX = event.getSceneX();
                mousePosY = event.getSceneY();
                double mouseDeltaX = (mousePosX - mouseOldX);
                double mouseDeltaY = (mousePosY - mouseOldY);
                double modifier = 0.05;
                double invertX = (camera.getTranslateZ() > 0) ? -1 : 1;
                rotateY.setAngle(rotateY.getAngle() - mouseDeltaX * modifier);
                rotateX.setAngle(rotateX.getAngle() + mouseDeltaY * modifier * invertX);
            }
        });

        scene.setOnKeyPressed(event -> {
            double moveSpeed = 10.0;
            Point3D direction = getCameraDirection();
            Point3D right = direction.crossProduct(new Point3D(0, 1, 0)).normalize();
            
            switch (event.getCode()) {
                case Z:
                    camera.setTranslateX(camera.getTranslateX() + direction.getX() * moveSpeed);
                    camera.setTranslateY(camera.getTranslateY() + direction.getY() * moveSpeed);
                    camera.setTranslateZ(camera.getTranslateZ() + direction.getZ() * moveSpeed);
                    break;
                case S:
                    camera.setTranslateX(camera.getTranslateX() - direction.getX() * moveSpeed);
                    camera.setTranslateY(camera.getTranslateY() - direction.getY() * moveSpeed);
                    camera.setTranslateZ(camera.getTranslateZ() - direction.getZ() * moveSpeed);
                    break;
                case Q:
                    camera.setTranslateX(camera.getTranslateX() + right.getX() * moveSpeed);
                    camera.setTranslateZ(camera.getTranslateZ() + right.getZ() * moveSpeed);
                    break;
                case D:
                    camera.setTranslateX(camera.getTranslateX() - right.getX() * moveSpeed);
                    camera.setTranslateZ(camera.getTranslateZ() - right.getZ() * moveSpeed);
                    break;
                case R:
                    resetCamera();
                    break;
                case SPACE:
                    // Move camera up (positive Y direction)
                    camera.setTranslateY(camera.getTranslateY() - moveSpeed);
                    break;
                case CONTROL:
                    // Move camera down (negative Y direction)
                    camera.setTranslateY(camera.getTranslateY() + moveSpeed);
                    break;
                case T:
                    // Position camera behind Earth in the direction of the Sun
                    positionCameraBehindPlanet(terre.position, soleil.position, 6_378);
                    break;
                case M:
                    // Position camera behind Mars in the direction of the Sun
                    positionCameraBehindPlanet(mars.position, soleil.position, 3_389);
                    break;
                // case E:
                //     // Focus on Earth
                //     focusOnPlanet(terre.position, 20_000);
                //     break;
                // case A:
                //     // Focus on Mars  
                //     focusOnPlanet(mars.position, 15_000);
                //     break;
                // case O:
                //     // Focus on Sun
                //     focusOnPlanet(soleil.position, 200_000);
                //     break;
                // case DIGIT1:
                //     // Fast movement speed
                //     moveSpeed = 50.0;
                //     break;
                // case DIGIT2:
                //     // Normal movement speed
                //     moveSpeed = 10.0;
                //     break;
                // case DIGIT3:
                //     // Slow movement speed
                //     moveSpeed = 2.0;
                //     break;
            }
            event.consume();
        });
    }

    // Helper method to position camera behind a planet
    private void positionCameraBehindPlanet(Point3D planetPos, Point3D sunPos, double planetRadius) {
        // Calculate direction from sun to planet
        Point3D sunToPlanet = planetPos.subtract(sunPos).normalize();
        
        // Position camera behind the planet (opposite to sun direction)
        double distance = planetRadius * 3; // Adjust multiplier as needed for good viewing distance
        Point3D cameraPos = planetPos.add(sunToPlanet.multiply(distance));
        
        camera.setTranslateX(cameraPos.getX());
        camera.setTranslateY(cameraPos.getY());
        camera.setTranslateZ(cameraPos.getZ());
        
        // Optional: Make camera look at the planet
        lookAt(planetPos);
    }

    // Optional helper method to make camera look at a specific point
    private void lookAt(Point3D target) {
        Point3D cameraPos = new Point3D(camera.getTranslateX(), camera.getTranslateY(), camera.getTranslateZ());
        Point3D direction = target.subtract(cameraPos).normalize();
        
        // Calculate rotation angles (this is a simplified version)
        double yaw = Math.toDegrees(Math.atan2(-direction.getX(), -direction.getZ()));
        double pitch = Math.toDegrees(Math.asin(direction.getY()));
        
        camera.setRotationAxis(Rotate.Y_AXIS);
        camera.setRotate(yaw);
        // Note: For full 6DOF camera control, you'd need more complex rotation handling
    }

    @Override
    public void start(Stage primaryStage) {
        Group root = new Group();
        Scene scene = new Scene(root, WIDTH, HEIGHT, true);
        scene.setFill(Color.BLACK);
        initCamera(scene);
        setupCameraControls(scene);

        // Création du Soleil
        double diamètreSoleil = 1.3927e6;
        soleil = new Astre(
            "Soleil",
            1.989e30,
            diamètreSoleil / 1e5,
            0, 0, 0,
            root,
            Color.YELLOW
        );
        soleil.position = new Point3D(WIDTH / 2, HEIGHT / 2, 0);
        soleil.renderAstreSansTrajectoire();

        // Création de toutes les planètes avec leurs paramètres orbitaux 3D complets

        mercure = new Astre(
            "Mercure",
            3.6e24,
            4879.4 / SCALE_DIAMETER,
            perihelieMercure,
            aphelieMercure,
            periodOrbitalMercure,
            inclinaisonMercure,
            longitudeNoeudMercure,
            argumentPerihelieMercure,
            root,
            Color.GRAY
        );

        venus = new Astre(
            "Vénus",
            4.867e25,
            6051.8 / SCALE_DIAMETER,
            perihelieVenus,
            aphelieVenus,
            periodOrbitalVenus,
            inclinaisonVenus,
            longitudeNoeudVenus,
            argumentPerihelieVenus,
            root,
            Color.ORANGE
        );

        terre = new Astre(
            "Terre",
            5.972e25,
            12756 / SCALE_DIAMETER,
            perihelieTerre,
            aphelieTerre,
            periodOrbitalTerre,
            inclinaisonTerre,
            longitudeNoeudTerre,
            argumentPerihelieTerre,
            root,
            Color.BLUE
        );

        mars = new Astre(
            "Mars",
            6.418e24,
            6779.5 / SCALE_DIAMETER,
            perihelieMars,
            aphelieMars,
            periodOrbitalMars,
            inclinaisonMars,
            longitudeNoeudMars,
            argumentPerihelieMars,
            root,
            Color.RED
        );

        jupiter = new Astre(
            "Jupiter",
            1.898e28,
            139820 / SCALE_DIAMETER,
            perihelieJupiter,
            aphelieJupiter,
            periodOrbitalJupiter,
            inclinaisonJupiter,
            longitudeNoeudJupiter,
            argumentPerihelieJupiter,
            root,
            Color.ORANGE
        );

        saturne = new Astre(
            "Saturne",
            5.683e27,
            58232 / SCALE_DIAMETER,
            perihelieSaturne,
            aphelieSaturne,
            periodOrbitalSaturne,
            inclinaisonSaturne,
            longitudeNoeudSaturne,
            argumentPerihelieSaturne,
            root,
            Color.BROWN
        );

        uranus = new Astre(
            "Uranus",
            8.681e26,
            50724 / SCALE_DIAMETER,
            perihelieUranus,
            aphelieUranus,
            periodOrbitalUranus,
            inclinaisonUranus,
            longitudeNoeudUranus,
            argumentPerihelieUranus,
            root,
            Color.CYAN
        );

        neptune = new Astre(
            "Neptune",
            1.024e27,
            24622 / SCALE_DIAMETER,
            perihelieNeptune,
            aphelieNeptune,
            periodOrbitalNeptune,
            inclinaisonNeptune,
            longitudeNoeudNeptune,
            argumentPerihelieNeptune,
            root,
            Color.DARKBLUE
        );

        // Animation des orbites
        new AnimationTimer() {
        private long lastTime = System.nanoTime();
        private double time = 0;
        private long lastPrintTime = 0;

        @Override
        public void handle(long now) {
            double deltaT = (now - lastTime) / 1_000_000_000.0;
            lastTime = now;
            time += deltaT / 1000; // Vitesse d'animation

            // Mise à jour des positions des planètes
            mercure.updatePosition(time, SCALE_DISTANCE, soleil.position);
            venus.updatePosition(time, SCALE_DISTANCE, soleil.position);
            terre.updatePosition(time, SCALE_DISTANCE, soleil.position);
            mars.updatePosition(time, SCALE_DISTANCE, soleil.position);
            jupiter.updatePosition(time, SCALE_DISTANCE, soleil.position);
            saturne.updatePosition(time, SCALE_DISTANCE, soleil.position);
            uranus.updatePosition(time, SCALE_DISTANCE, soleil.position);
            neptune.updatePosition(time, SCALE_DISTANCE, soleil.position);

            if (now - lastPrintTime > 5_000_000_000L) { // Toutes les 5 secondes
                affPos();
                printCameraPosition();
                lastPrintTime = now;
            }

            
            
            
            mercure.renderAstreSansTrajectoire();
            venus.renderAstreSansTrajectoire();
            terre.renderAstreSansTrajectoire();
            mars.renderAstreSansTrajectoire();
            jupiter.renderAstreSansTrajectoire();
            saturne.renderAstreSansTrajectoire();
            uranus.renderAstreSansTrajectoire();
            neptune.renderAstreSansTrajectoire();
            
        }
    }.start();

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void affPos() {
        System.out.println("=== POSITIONS DES PLANÈTES (en pixels) ===");
        System.out.println(mercure.toString());
        System.out.println(venus.toString());
        System.out.println(terre.toString());
        System.out.println(mars.toString());
        System.out.println(jupiter.toString());
        System.out.println(saturne.toString());
        System.out.println(uranus.toString());
        System.out.println(neptune.toString());


        System.out.println("===============================");
    }

    // Méthode pour récupérer toutes les coordonnées formatées de la caméra
    public String getCameraCoordinates() {
        return String.format("Position: (%.2f, %.2f, %.2f) - Rotation: (%.2f°, %.2f°)",
                           camera.getTranslateX(), camera.getTranslateY(), camera.getTranslateZ(),
                           rotateX.getAngle(), rotateY.getAngle());
    }

    // Méthode pour afficher les coordonnées de la caméra
    public void printCameraPosition() {
        System.out.println("=== COORDONNÉES CAMÉRA ===");
        System.out.println(getCameraCoordinates());
        System.out.println("==========================");
    }

    // Méthode pour réinitialiser la vue
    public void resetCamera() {
        rotateX.setAngle(0);
        rotateY.setAngle(0);
        camera.setTranslateX(WIDTH / 2);
        camera.setTranslateY(HEIGHT / 2);
        camera.setTranslateZ(-500);
        System.out.println("Caméra réinitialisée !");
        printCameraPosition();
    }
}
