import Classes.Astre;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Point3D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import Classes.CameraController;
import javafx.stage.Stage;


public class SimulationSystemeSolaire extends Application {
    public final int WIDTH = 800;
    public final int HEIGHT = 600;
    public final double SCALE_DISTANCE = 150;    // 150 pixels = 1 UA
    public final double SCALE_DIAMETER = 1e4;    // 10 000 km par pixel pour les diamètres
    public boolean Tpressed = false;
    public boolean doTrajectotyRender = false;

    // Variables pour la gestion du temps
    private double timeSpeed = 500.0;          // Facteur de vitesse par défaut
    private boolean isPaused = false;          // État de pause
    private final double MIN_SPEED = 1.0;      // Vitesse minimale
    private final double MAX_SPEED = 50000.0;  // Vitesse maximale
    private final double SPEED_MULTIPLIER = 1.5; // Facteur de multiplication

    
    // Variables pour la gestion de la caméra
    private PerspectiveCamera camera;
    private CameraController cameraController;

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

    // Paramètres orbitaux de la Lune (par rapport à la Terre)
    public double periodOrbitalLune = 27.322 / 365.25; // 27.322 jours convertis en années
    public double perigeeLune = 0.002573; // 384 400 km convertis en UA (384400 / 149597870.7)
    public double apogeeLune = 0.002718; // 406 700 km convertis en UA
    public double inclinaisonLune = 5.145; // Inclinaison par rapport à l'écliptique
    public double longitudeNoeudLune = 125.044; // Longitude du nœud ascendant
    public double argumentPerigeeLune = 318.308; // Argument du périgée

    // Paramètres approximatifs
    double perihelieTitan = 1_222_000 / 149597870.7;    // Conversion km -> UA
    double aphelieTitan = 1_222_000 / 149597870.7;      // Conversion km -> UA
    double periodeOrbitaleTitan = 15.945 / 365.25;  
    double inclinaisonTitan = 0.3;          
    double longitudeNoeudTitan = 169.529;
    double argumentPerihelieTitan = 186.585;

    // **IO (Jupiter I)**
    double periapsideIo = 420_000 / 149597870.7;        // 0,002806 UA
    double apoapsideIo = 423_400 / 149597870.7;         // 0,002829 UA
    double periodeOrbitaleIo = 1.769 / 365.25;          // 0,004844 années
    double inclinaisonIo = 0.036;                        // 0,036° (par rapport à l'équateur de Jupiter)
    double longitudeNoeudIo = 43.977;                    // Longitude du nœud ascendant (J2000)
    double argumentPeriapsideIo = 84.129;                // Argument du périapside (J2000)

    // Paramètres Europa (Jupiter II)
    double perihelieEuropa = 664_862 / 149597870.7;  // Périapside en UA
    double aphelieEuropa = 676_938 / 149597870.7;    // Apoapside en UA         
    double periodeOrbitaleEuropa = 3.551181 / 365.25; // Période plus précise
    double inclinaisonEuropa = 0.469;                 // Valeur corrigée
    double longitudeNoeudEuropa = 219.106;
    double argumentPerihelieEuropa = 88.970;


    // **GANYMEDE (Jupiter III)**
    double periapsideGanymede = 1_069_008 / 149597870.7;  // 0,007145 UA
    double apoapsideGanymede = 1_071_792 / 149597870.7;   // 0,007164 UA
    double periodeOrbitaleGanymede = 7.1545529 / 365.25;  // 0,019589 années
    double inclinaisonGanymede = 0.21;                     // 0,21° (par rapport à l'équateur de Jupiter)
    double longitudeNoeudGanymede = 63.552;                // Longitude du nœud ascendant (J2000)
    double argumentPeriapsideGanymede = 192.417;           // Argument du périapside (J2000)

    // **CALLISTO (Jupiter IV)**
    double periapsideCallisto = 1_869_000 / 149597870.7;  // 0,012490 UA
    double apoapsideCallisto = 1_897_000 / 149597870.7;   // 0,012677 UA
    double periodeOrbitaleCallisto = 16.6890184 / 365.25; // 0,045707 années
    double inclinaisonCallisto = 0.192;                    // 0,192° (par rapport à l'équateur de Jupiter)
    double longitudeNoeudCallisto = 298.848;               // Longitude du nœud ascendant (J2000)
    double argumentPeriapsideCallisto = 52.643;            // Argument du périapside (J2000)

    // Mimas
    double periapsideMimas = 181_902 / 149597870.7;     // 0,001216 UA
    double apoapsideMimas = 189_176 / 149597870.7;      // 0,001265 UA
    double periodeOrbittraleMimas = 0.942 / 365.25;     // 0,00258 années
    double inclinaisonMimas = 1.574;
    double longitudeNoeudMimas = 139.771;
    double argumentPeriapsideMimas = 333.877;

    // Encelade
    double periapsideEncelade = 236_918 / 149597870.7;   // 0,001584 UA
    double apoapsideEncelade = 239_156 / 149597870.7;    // 0,001599 UA
    double periodeOrbitraleEncelade = 1.370 / 365.25;    // 0,00375 années
    double inclinaisonEncelade = 0.019;
    double longitudeNoeudEncelade = 169.508;
    double argumentPeriapsideEncelade = 13.456;

    // Téthys
    double periapsideTethys = 294_619 / 149597870.7;     // 0,001969 UA
    double apoapsideTethys = 296_890 / 149597870.7;      // 0,001984 UA
    double periodeOrbitaleTethys = 1.888 / 365.25;       // 0,00517 années
    double inclinaisonTethys = 1.12;
    double longitudeNoeudTethys = 167.789;
    double argumentPeriapsideTethys = 262.334;

    // Dioné
    double periapsideDione = 376_566 / 149597870.7;      // 0,002517 UA
    double apoapsideDione = 377_396 / 149597870.7;       // 0,002523 UA
    double periodeOrbitaleDione = 2.737 / 365.25;        // 0,007496 années
    double inclinaisonDione = 0.019;
    double longitudeNoeudDione = 128.538;
    double argumentPeriapsideDione = 91.796;

    // Rhéa
    double periapsideRhea = 527_039 / 149597870.7;       // 0,003522 UA
    double apoapsideRhea = 527_363 / 149597870.7;        // 0,003524 UA
    double periodeOrbitraleRhea = 4.518 / 365.25;        // 0,012373 années
    double inclinaisonRhea = 0.345;
    double longitudeNoeudRhea = 169.837;
    double argumentPeriapsideRhea = 201.164;

    // Japet
    double periapsideIapetus = 3_460_820 / 149597870.7;  // 0,02313 UA
    double apoapsideIapetus = 3_661_300 / 149597870.7;   // 0,02447 UA
    double periodeOrbitraleIapetus = 79.330183 / 365.25; // 0,21725 années
    double inclinaisonIapetus = 15.47;
    double longitudeNoeudIapetus = 81.098;
    double argumentPeriapsideIapetus = 271.606;


    private Astre soleil, mercure, venus, terre, mars, jupiter, saturne, uranus, neptune;
    private Astre lune, titan, europa, callisto, ganymede, io, mimas, encelade, tethys, dione, rhea, iapetus;

    public static void main(String[] args) {
        launch(args);
    }

    // Configuration des contrôles de la scène principaux
    private void setupSceneControls(Scene scene) {
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case DIGIT1:
                    cameraController.focusOn(mercure, rayonMercure * SCALE_DISTANCE + 50);
                    break;
                case DIGIT2:
                    cameraController.focusOn(venus, rayonVenus * SCALE_DISTANCE + 50);
                    break;
                case DIGIT3:
                    cameraController.focusOn(terre, rayonTerre * SCALE_DISTANCE + 50);
                    break;
                case DIGIT4:
                    cameraController.focusOn(mars, rayonMars * SCALE_DISTANCE + 50);
                    break;
                case DIGIT5:
                    cameraController.focusOn(jupiter, rayonJupiter * SCALE_DISTANCE + 200);
                    break;
                case DIGIT6:
                    cameraController.focusOn(saturne, rayonSaturne * SCALE_DISTANCE + 200);
                    break;
                case DIGIT7:
                    cameraController.focusOn(uranus, rayonUranus * SCALE_DISTANCE + 100);
                    break;
                case DIGIT8:
                    cameraController.focusOn(neptune, rayonNeptune * SCALE_DISTANCE + 100);
                    break;
                case DIGIT9:
                    // On réduit le temps (ralentit l'animation)
                    timeSpeed = Math.min(timeSpeed * SPEED_MULTIPLIER, MAX_SPEED);
                    System.out.println("Vitesse réduite - Facteur: " + String.format("%.1f", timeSpeed));
                    break;

                case DIGIT0:
                    // On augmente le temps (accélère l'animation)
                    timeSpeed = Math.max(timeSpeed / SPEED_MULTIPLIER, MIN_SPEED);
                    System.out.println("Vitesse augmentée - Facteur: " + String.format("%.1f", timeSpeed));
                    break;
                    
                case ENTER:
                    // On stop/reprend le temps
                    isPaused = !isPaused;
                    System.out.println(isPaused ? "⏸️ Simulation en PAUSE" : "▶️ Simulation REPRISE");
                    break;
                case R:
                    initCamera(scene);
                    break;
                case T:
                    doTrajectotyRender = true;
                    break;
                case G:
                    doTrajectotyRender = false;

                    mercure.resetTrajectory();
                    venus.resetTrajectory();
                    terre.resetTrajectory();
                    mars.resetTrajectory();
                    jupiter.resetTrajectory();
                    saturne.resetTrajectory();
                    uranus.resetTrajectory();
                    neptune.resetTrajectory();



                    break;
                    
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

        camera.setTranslateX(0);
        camera.setTranslateY(0);
        camera.setTranslateZ(-500); 
        scene.setCamera(camera);
        
        if (cameraController == null) {
            cameraController = new CameraController(camera);
            cameraController.setupControls(scene);
        } else {
            cameraController.detach();
            cameraController.setPositionAndLookAt(new Point3D(0, 0, -500), new Point3D(0, 0, 0));
        }
    }


    @Override
    public void start(Stage primaryStage) {
        // Group root = new Group();
        // Scene scene = new Scene(root, WIDTH, HEIGHT, true);
        // scene.setFill(Color.BLACK);
        // initCamera(scene);
        // setupCameraControls(scene);

        // MÉTHODE 1 : Utiliser ImagePattern avec setFill (Simple)
            Group root = new Group();
            Scene scene = new Scene(root, WIDTH, HEIGHT, true);

            // Charger votre image de texture (remplacez par votre chemin)
            Image backgroundTexture = new Image("/resources/textures/stars.png");
            // Ou depuis les ressources du projet :
            // Image backgroundTexture = new Image(getClass().getResourceAsStream("/images/space_background.jpg"));

            // Créer un pattern d'image
            ImagePattern texturePattern = new ImagePattern(backgroundTexture);

            // Appliquer la texture à la scène
            scene.setFill(texturePattern);

            initCamera(scene);
            setupSceneControls(scene);

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
        soleil.position = new Point3D(0, 0, 0);
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

        io = new Astre(
            "Io",
            8.93e22,
            3643 / SCALE_DIAMETER,
            periapsideIo,
            apoapsideIo,
            periodeOrbitaleIo,
            inclinaisonIo,
            longitudeNoeudIo,
            argumentPeriapsideIo,
            root,
            Color.YELLOW
        );

        ganymede = new Astre(
            "Ganymède", 1.4819e23, 5262 / SCALE_DIAMETER,
            periapsideGanymede, apoapsideGanymede, periodeOrbitaleGanymede,
            inclinaisonGanymede, longitudeNoeudGanymede, argumentPeriapsideGanymede,
            root, Color.GRAY
        );

        callisto = new Astre(
            "Callisto", 1.075938e23, 4820 / SCALE_DIAMETER,
            periapsideCallisto, apoapsideCallisto, periodeOrbitaleCallisto,
            inclinaisonCallisto, longitudeNoeudCallisto, argumentPeriapsideCallisto,
            root, Color.DARKGRAY
        );

        mimas = new Astre(
            "Mimas", 3.7493e19, 396 / SCALE_DIAMETER,
            periapsideMimas, apoapsideMimas, periodeOrbittraleMimas,
            inclinaisonMimas, longitudeNoeudMimas, argumentPeriapsideMimas,
            root, Color.LIGHTGRAY
        );

        encelade = new Astre(
            "Encelade", 1.080318e20, 504 / SCALE_DIAMETER,
            periapsideEncelade, apoapsideEncelade, periodeOrbitraleEncelade,
            inclinaisonEncelade, longitudeNoeudEncelade, argumentPeriapsideEncelade,
            root, Color.WHITE
        );

        tethys = new Astre(
            "Téthys", 6.1745e20, 1066 / SCALE_DIAMETER,
            periapsideTethys, apoapsideTethys, periodeOrbitaleTethys,
            inclinaisonTethys, longitudeNoeudTethys, argumentPeriapsideTethys,
            root, Color.LIGHTGRAY
        );

        dione = new Astre(
            "Dioné", 1.095452e21, 1123 / SCALE_DIAMETER,
            periapsideDione, apoapsideDione, periodeOrbitaleDione,
            inclinaisonDione, longitudeNoeudDione, argumentPeriapsideDione,
            root, Color.LIGHTGRAY
        );

        rhea = new Astre(
            "Rhéa", 2.306518e21, 1527 / SCALE_DIAMETER,
            periapsideRhea, apoapsideRhea, periodeOrbitraleRhea,
            inclinaisonRhea, longitudeNoeudRhea, argumentPeriapsideRhea,
            root, Color.LIGHTGRAY
        );

        iapetus = new Astre(
            "Japet", 1.805635e21, 1469 / SCALE_DIAMETER,
            periapsideIapetus, apoapsideIapetus, periodeOrbitraleIapetus,
            inclinaisonIapetus, longitudeNoeudIapetus, argumentPeriapsideIapetus,
            root, Color.DARKGRAY
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

            if (!isPaused) {
                time += deltaT / timeSpeed; // Application du facteur de vitesse
            }

            double factorTerre = 10.0;
            double factorJupiter = 20.0;
            double factorSaturne = 15.0;

            // Mise à jour des positions des planètes
            mercure.updatePosition(time, SCALE_DISTANCE, soleil.position);
            venus.updatePosition(time, SCALE_DISTANCE, soleil.position);
            terre.updatePosition(time, SCALE_DISTANCE, soleil.position);
            mars.updatePosition(time, SCALE_DISTANCE, soleil.position);
            jupiter.updatePosition(time, SCALE_DISTANCE, soleil.position);
            saturne.updatePosition(time, SCALE_DISTANCE, soleil.position);
            uranus.updatePosition(time, SCALE_DISTANCE, soleil.position);
            neptune.updatePosition(time, SCALE_DISTANCE, soleil.position);

            //Mise à jour des positions des satélites
            lune.updatePositionAroundAstre(time, terre.position,  SCALE_DISTANCE, factorTerre);
            europa.updatePositionAroundAstre(time, jupiter.position, SCALE_DISTANCE, factorJupiter); 
            io.updatePositionAroundAstre(time, jupiter.position, SCALE_DISTANCE, factorJupiter);
            ganymede.updatePositionAroundAstre(time, jupiter.position, SCALE_DISTANCE, factorJupiter);
            callisto.updatePositionAroundAstre(time, jupiter.position, SCALE_DISTANCE, factorJupiter);
            titan.updatePositionAroundAstre(time, saturne.position, SCALE_DISTANCE, factorSaturne); 
            dione.updatePositionAroundAstre(time, saturne.position, SCALE_DISTANCE, factorSaturne);
            iapetus.updatePositionAroundAstre(time, saturne.position, SCALE_DISTANCE, factorSaturne);
            rhea.updatePositionAroundAstre(time, saturne.position, SCALE_DISTANCE, factorSaturne);
            encelade.updatePositionAroundAstre(time, saturne.position, SCALE_DISTANCE, factorSaturne);
            tethys.updatePositionAroundAstre(time, saturne.position, SCALE_DISTANCE, factorSaturne);
            
            if (cameraController != null) {
                cameraController.update();
            }

            if (now - lastPrintTime > 5_000_000_00L) { // Toutes les 5 secondes
                affPos();
                // printCameraPosition();
                lastPrintTime = now;
            }

            // Rendering des planètes
            mercure.renderAstreSansTrajectoire(doTrajectotyRender,"/resources/textures/mercure.png");
            venus.renderAstreSansTrajectoire(doTrajectotyRender,"/resources/textures/venus.png");
            terre.renderAstreSansTrajectoire(doTrajectotyRender, "/resources/textures/terre.png");
            mars.renderAstreSansTrajectoire(doTrajectotyRender,"/resources/textures/mars.png");
            jupiter.renderAstreSansTrajectoire(doTrajectotyRender,"/resources/textures/jupiter.png");
            saturne.renderAstreSansTrajectoire(doTrajectotyRender,"/resources/textures/saturne.png");
            uranus.renderAstreSansTrajectoire(doTrajectotyRender,"/resources/textures/uranus.png");
            neptune.renderAstreSansTrajectoire(doTrajectotyRender,"/resources/textures/neptune.png");

            // Rendering des Sattélites 
            lune.renderAstreSansTrajectoire(false,"/resources/textures/lune.png");
            io.renderAstreSansTrajectoire(false, "/resources/textures/io.png");
            ganymede.renderAstreSansTrajectoire(false, "/resources/textures/ganymede.png");
            callisto.renderAstreSansTrajectoire(false, "/resources/textures/callisto.png");
            europa.renderAstreSansTrajectoire(false, "/resources/textures/europa.png"); 
            titan.renderAstreSansTrajectoire(false, "/resources/textures/titan.png");
            tethys.renderAstreSansTrajectoire(false, "/resources/textures/tethys.png");
            encelade.renderAstreSansTrajectoire(false, "/resources/textures/encelade.png");
            rhea.renderAstreSansTrajectoire(false, "/resources/textures/rhea.png");
            iapetus.renderAstreSansTrajectoire(false, "/resources/textures/iapetus.png");
            dione.renderAstreSansTrajectoire(false, "/resources/textures/dione.png");


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
