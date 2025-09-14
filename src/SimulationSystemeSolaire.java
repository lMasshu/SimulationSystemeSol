import Classes.Astre;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Point3D;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.stage.Stage;


public class SimulationSystemeSolaire extends Application {
    public final int WIDTH = 800;
    public final int HEIGHT = 600;
    public final double SCALE_DISTANCE = 150;    // 150 pixels = 1 UA
    public final double SCALE_DIAMETER = 1e4;    // 10 000 km par pixel pour les diamètres
    public boolean Tpressed = false;
    public boolean doTrajectotyRender = false;

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

    // Rayon des planètes

    double rayonMercure = (4879.4 / 2.0) / SCALE_DIAMETER;
    double rayonVenus = (6051.8 * 2.0 / 2.0) / SCALE_DIAMETER;
    double rayonTerre = (12756.0 / 2.0) / SCALE_DIAMETER;
    double rayonMars = (6779.5 / 2.0) / SCALE_DIAMETER;
    double rayonJupiter = (139820.0 / 2.0) / SCALE_DIAMETER;
    double rayonSaturne = (58232.0 / 2.0) / SCALE_DIAMETER;
    double rayonUranus = (50724.0 / 2.0) / SCALE_DIAMETER;
    double rayonNeptune = (24622.0 / 2.0) / SCALE_DIAMETER;
    double rayonLune = (3474.8 / 2.0) / SCALE_DIAMETER;
    double rayonEuropa = ( 3122 / 2.0 ) / SCALE_DIAMETER;
    double rayonTitan = ( 5150 / 2.0 ) / SCALE_DIAMETER;

    // Paramètres approximatifs
    double perihelieEuropa = 670_900;       
    double aphelieEuropa = 670_900;         
    double periodeOrbitaleEuropa = 3.551;  
    double inclinaisonEuropa = 0.47;       
    double longitudeNoeudEuropa = 219.106;
    double argumentPerihelieEuropa = 88.970;

    // Paramètres orbitaux de la Lune (par rapport à la Terre)
    public double periodOrbitalLune = 27.322 / 365.25; // 27.322 jours convertis en années
    public double perigeeLune = 0.002573; // 384 400 km convertis en UA (384400 / 149597870.7)
    public double apogeeLune = 0.002718; // 406 700 km convertis en UA
    public double inclinaisonLune = 5.145; // Inclinaison par rapport à l'écliptique
    public double longitudeNoeudLune = 125.044; // Longitude du nœud ascendant
    public double argumentPerigeeLune = 318.308; // Argument du périgée

    // Paramètres approximatifs
    double perihelieTitan = 1_222_000;      // km, distance minimale Saturne-Titan
    double aphelieTitan = 1_222_000;        // km, pour Titan orbite quasi circulaire
    double periodeOrbitaleTitan = 15.945;   // jours
    double inclinaisonTitan = 0.3;          // degrés par rapport au plan équatorial de Saturne
    double longitudeNoeudTitan = 169.529;
    double argumentPerihelieTitan = 186.585;

    private PerspectiveCamera camera;
    private final Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private double mousePosX, mousePosY;
    private double mouseOldX, mouseOldY;
    private Astre soleil, mercure, venus, terre, mars, jupiter, saturne, uranus, neptune;
    private Astre lune, titan, europa;

    public static void main(String[] args) {
        launch(args);
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
                case SPACE:
                    // Move camera up (positive Y direction)
                    camera.setTranslateY(camera.getTranslateY() - moveSpeed);
                    break;
                case CONTROL:
                    // Move camera down (negative Y direction)
                    camera.setTranslateY(camera.getTranslateY() + moveSpeed);
                    break;
                case DIGIT1:
                    positionCameraBehindPlanet(mercure.position, soleil.position, rayonMercure);
                    break;
                case DIGIT2:
                    positionCameraBehindPlanet(venus.position, soleil.position, rayonVenus);
                    break;
                case DIGIT3:
                    positionCameraBehindPlanet(terre.position, soleil.position, rayonTerre);
                    break;
                case DIGIT4:
                    positionCameraBehindPlanet(mars.position, soleil.position, rayonMars);
                    break;
                case DIGIT5:
                    positionCameraBehindPlanet(jupiter.position, soleil.position, rayonJupiter);
                    break;
                case DIGIT6:
                    positionCameraBehindPlanet(saturne.position, soleil.position, rayonSaturne);
                    break;
                case DIGIT7:
                    positionCameraBehindPlanet(uranus.position, soleil.position, rayonUranus);
                    break;
                case DIGIT8:
                    positionCameraBehindPlanet(neptune.position, soleil.position, rayonNeptune);
                    break;
                case DIGIT9:
                    positionCameraBehindPlanet(lune.position, terre.position, rayonLune);
                    break;
                case DIGIT0:
                    positionCameraBehindPlanet(europa.position, soleil.position, rayonEuropa );
                    break;
                case M:
                    if (titan != null) {
                        positionCameraBehindPlanet(titan.position, saturne.position, rayonTitan);
                    }
                    break;
                case R:
                    initCamera(scene);
                    break;
                case T:
                    doTrajectotyRender = true;
            }
            event.consume();
        });
    }



    private double lastYaw = 0;
    private double lastPitch = 0;
    private static final double EPS = 1e-6;
    private static final double PITCH_OFFSET = 90.0;


    private void initCamera(Scene scene) {
        camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(100000.0);

        // IMPORTANT : ordre = yaw (Y) puis pitch (X)
        camera.getTransforms().clear();
        camera.getTransforms().addAll(rotateY, rotateX);

        // orientation de départ demandée
        rotateY.setAngle(0.0); // yaw initial
        rotateX.setAngle(0.0);  // pitch initial

        camera.setTranslateX(WIDTH / 2);
        camera.setTranslateY(HEIGHT / 2);
        camera.setTranslateZ(-500); // garde un Z initial hors du plan z=0
        scene.setCamera(camera);
    }

    private Point3D getCameraDirection() {
        Point3D forward = new Point3D(0, 0, 1);
        forward = rotateY.transform(forward);
        forward = rotateX.transform(forward);
        return forward.normalize();
    }


    private void positionCameraBehindPlanet(Point3D planetPos, Point3D sunPos, double planetRadius) {
        Point3D sunToPlanet = planetPos.subtract(sunPos);
        if (sunToPlanet.magnitude() < EPS) return;

        Point3D dir = sunToPlanet.normalize();



        double cameraDistance = planetRadius + 20;

        Point3D cameraPos = planetPos.add(dir.multiply(cameraDistance));

        camera.setTranslateX(cameraPos.getX());
        camera.setTranslateY(cameraPos.getY());
        camera.setTranslateZ(cameraPos.getZ());

        // Regarder le Soleil (ou planetPos selon le comportement voulu)
        lookAt(sunPos);
    }


    private void lookAt(Point3D target) {
        Point3D cameraPos = new Point3D(
            camera.getTranslateX(),
            camera.getTranslateY(),
            camera.getTranslateZ()
        );

        Point3D dirVec = target.subtract(cameraPos);
        double dx = dirVec.getX();
        double dy = dirVec.getY();
        double dz = dirVec.getZ();

        double horiz = Math.hypot(dx, dz); // longueur projection sur XZ

        double yaw;
        double pitch;

        if (horiz < EPS) {
            // caméra directement au-dessus/en-dessous : on évite le "flip" en gardant lastYaw
            yaw = lastYaw;
            pitch = (dy > 0) ? -90.0 : 90.0; // si la cible est en dessous => regarder vers le bas
        } else {
            // yaw : angle dans le plan XZ (atan2(dx, dz))
            yaw = Math.toDegrees(Math.atan2(dx, dz));
            // pitch : angle entre horizontal et vecteur cible (negatif dy pour JavaFX Y vers le bas)
            pitch = Math.toDegrees(Math.atan2(-dy, horiz));
        }

        yaw = normalizeAngle(yaw);
        pitch = normalizeAngle(pitch);


        rotateY.setAngle(yaw);
        rotateX.setAngle(pitch);

        lastYaw = yaw;
        lastPitch = pitch;

        // // debug
        // System.out.println("=== Camera lookAt Debug ===");
        // System.out.println("Camera Pos: " + cameraPos);
        // System.out.println("Target Pos: " + target);
        // System.out.println("dirVec: " + dirVec);
        // System.out.printf("Computed yaw=%.3f pitch=%.3f -> applied rotateY=%.3f rotateX=%.3f%n",
        //                 yaw, pitch, rotateY.getAngle(), rotateX.getAngle());
        // System.out.println("===========================");
    }

    private double normalizeAngle(double a) {
        while (a <= -180) a += 360;
        while (a > 180) a -= 360;
        return a;
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
        soleil.renderAstreSansTrajectoire(false, "/resources/textures/soleil.png");

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

        lune = new Astre(
            "Lune",
            7.342e22,
            3474.8 / SCALE_DIAMETER,
            perigeeLune,
            apogeeLune,
            periodOrbitalLune,
            inclinaisonLune,
            longitudeNoeudLune,
            argumentPerigeeLune,
            root,
            Color.LIGHTGRAY
        );

        // Titan (satellite de Saturne)
        titan = new Astre(
            "Titan",
            1.3452e23,
            5150 / SCALE_DIAMETER,
            perihelieTitan,
            aphelieTitan,
            periodeOrbitaleTitan,
            inclinaisonTitan,
            longitudeNoeudTitan,
            argumentPerihelieTitan,
            root,
            Color.ORANGE
        );

        // Europa (satellite de Jupiter)
        europa = new Astre(
            "Europa",
            4.799e22,
            3122 / SCALE_DIAMETER,
            perihelieEuropa,
            aphelieEuropa,
            periodeOrbitaleEuropa,
            inclinaisonEuropa,
            longitudeNoeudEuropa,
            argumentPerihelieEuropa,
            root,
            Color.LIGHTGRAY
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
            time += deltaT / 500; // Vitesse d'animation

            // Mise à jour des positions des planètes
            mercure.updatePosition(time, SCALE_DISTANCE, soleil.position);
            venus.updatePosition(time, SCALE_DISTANCE, soleil.position);
            terre.updatePosition(time, SCALE_DISTANCE, soleil.position);
            mars.updatePosition(time, SCALE_DISTANCE, soleil.position);
            jupiter.updatePosition(time, SCALE_DISTANCE, soleil.position);
            saturne.updatePosition(time, SCALE_DISTANCE, soleil.position);
            uranus.updatePosition(time, SCALE_DISTANCE, soleil.position);
            neptune.updatePosition(time, SCALE_DISTANCE, soleil.position);
            lune.updatePositionAroundTerre(time, terre.position,  SCALE_DISTANCE);
            europa.updatePositionAroundPlanet(time, jupiter.position, 670_900, 40); 
            titan.updatePositionAroundPlanet(time, saturne.position, 1_222_000, 60); 


            if (now - lastPrintTime > 5_000_000_00L) { // Toutes les 5 secondes
                affPos();
                // printCameraPosition();
                lastPrintTime = now;
            }

            mercure.renderAstreSansTrajectoire(doTrajectotyRender,"/resources/textures/mercure.png");
            venus.renderAstreSansTrajectoire(doTrajectotyRender,"/resources/textures/venus.png");
            terre.renderAstreSansTrajectoire(doTrajectotyRender, "/resources/textures/terre.png");
            mars.renderAstreSansTrajectoire(doTrajectotyRender,"/resources/textures/mars.png");
            jupiter.renderAstreSansTrajectoire(doTrajectotyRender,"/resources/textures/jupiter.png");
            saturne.renderAstreSansTrajectoire(doTrajectotyRender,"/resources/textures/saturne.png");
            uranus.renderAstreSansTrajectoire(doTrajectotyRender,"/resources/textures/uranus.png");
            neptune.renderAstreSansTrajectoire(doTrajectotyRender,"/resources/textures/neptune.png");
            lune.renderAstreSansTrajectoire(doTrajectotyRender,"/resources/textures/lune.png");
            titan.renderAstreSansTrajectoire(doTrajectotyRender, "/resources/textures/titan.png");
            europa.renderAstreSansTrajectoire(doTrajectotyRender, "/resources/textures/europa.png"); 
        }
    }.start();

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void affPos() {
        System.out.println("=== POSITIONS DES PLANÈTES (en pixels) ===");
        System.out.println(soleil.toString());
        System.out.println(mercure.toString());
        System.out.println(venus.toString());
        System.out.println(terre.toString());
        System.out.println(lune.toString());
        System.out.println(mars.toString());
        System.out.println(jupiter.toString());
        System.out.println(europa.toString());
        System.out.println(saturne.toString());
        System.out.println(titan.toString());
        System.out.println(uranus.toString());
        System.out.println(neptune.toString());
        


        System.out.println("===============================");
    }
}
