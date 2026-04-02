import Classes.camera.CameraController;
import Classes.data.AstreData;
import Classes.data.Config;
import Classes.model.Astre;
import Classes.simulation.SystemeManager;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.stage.Stage;

/**
 * Point d'entrée du Simulateur du Système Solaire 3D.
 *
 * Cette classe a une seule responsabilité : assembler les sous-systèmes
 * ({@link SystemeManager}, {@link CameraController}) et lancer la boucle JavaFX.
 *
 * <ul>
 *   <li>Données astronomiques → {@link AstreData}</li>
 *   <li>Constantes globales    → {@link Config}</li>
 *   <li>Gestion des astres     → {@link SystemeManager}</li>
 *   <li>Navigation 3D          → {@link CameraController}</li>
 * </ul>
 */
public class SimulationSystemeSolaire extends Application {

    // --- Sous-systèmes ---
    private final SystemeManager   systeme  = new SystemeManager();
    private       PerspectiveCamera camera;
    private       CameraController cameraController;

    // --- État de la simulation ---
    private double  timeSpeed         = Config.DEFAULT_TIME_SPEED;
    private boolean isPaused          = false;
    private boolean doTrajectoryRender = false;

    // ===================================================================
    //  POINT D'ENTRÉE
    // ===================================================================
    public static void main(String[] args) { launch(args); }

    // ===================================================================
    //  DÉMARRAGE JAVAFX
    // ===================================================================
    @Override
    public void start(Stage primaryStage) {
        Group root  = new Group();
        Scene scene = new Scene(root, Config.WIDTH, Config.HEIGHT, true);
        scene.setFill(new ImagePattern(new Image("/resources/textures/stars.png")));

        initCamera(scene);
        setupControls(scene);
        systeme.init(root);
        startAnimationLoop();

        primaryStage.setTitle("Simulateur du Système Solaire 3D");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // ===================================================================
    //  CAMÉRA
    // ===================================================================
    private void initCamera(Scene scene) {
        camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(100_000.0);
        camera.setTranslateZ(-500);
        scene.setCamera(camera);

        if (cameraController == null) {
            cameraController = new CameraController(camera);
            cameraController.setupControls(scene);
            // On se place un peu au-dessus et en recul pour voir le plan X-Z penché
            cameraController.setPositionAndLookAt(new Point3D(0, -600, -1000), Point3D.ZERO);
        } else {
            cameraController.detach();
            cameraController.setPositionAndLookAt(new Point3D(0, -600, -1000), Point3D.ZERO);
        }
    }

    // ===================================================================
    //  CONTRÔLES CLAVIER
    // ===================================================================
    /** Correspondance touches 1–8 → planètes (index = digit - 1). */
    private static final AstreData[] PLANET_SHORTCUTS = {
        AstreData.MERCURE, AstreData.VENUS,   AstreData.TERRE,   AstreData.MARS,
        AstreData.JUPITER, AstreData.SATURNE, AstreData.URANUS,  AstreData.NEPTUNE
    };

    private void setupControls(Scene scene) {
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {

                // --- Focus rapide des planètes (touches 1–8) ---
                case DIGIT1: case DIGIT2: case DIGIT3: case DIGIT4:
                case DIGIT5: case DIGIT6: case DIGIT7: case DIGIT8: {
                    int idx = event.getCode().ordinal()
                            - javafx.scene.input.KeyCode.DIGIT1.ordinal();
                    if (idx >= 0 && idx < PLANET_SHORTCUTS.length) {
                        AstreData target = PLANET_SHORTCUTS[idx];
                        Astre astre = systeme.getAstre(target);
                        double dist = astre.diametre / 2.0 * Config.SCALE_DISTANCE + 200;
                        cameraController.focusOn(astre, dist);
                    }
                    break;
                }

                // --- Vitesse de simulation ---
                case DIGIT9:
                    timeSpeed = Math.min(timeSpeed * Config.SPEED_MULTIPLIER, Config.MAX_SPEED);
                    System.out.printf("⏩ Vitesse ×%.0f%n", timeSpeed);
                    break;
                case DIGIT0:
                    timeSpeed = Math.max(timeSpeed / Config.SPEED_MULTIPLIER, Config.MIN_SPEED);
                    System.out.printf("⏪ Vitesse ×%.0f%n", timeSpeed);
                    break;

                // --- Pause ---
                case ENTER:
                    isPaused = !isPaused;
                    System.out.println(isPaused ? "⏸ Pause" : "▶ Reprise");
                    break;

                // --- Reset caméra ---
                case R:
                    initCamera(scene);
                    break;

                // --- Trajectoires orbitales ---
                case T:
                    doTrajectoryRender = !doTrajectoryRender;
                    System.out.printf("🛰 Trajectoires : %s%n", doTrajectoryRender ? "ON" : "OFF");
                    break;

                default:
                    break;
            }
            event.consume();
        });
    }

    // ===================================================================
    //  BOUCLE D'ANIMATION
    // ===================================================================
    private void startAnimationLoop() {
        new AnimationTimer() {
            private long   lastTime      = System.nanoTime();
            private double time          = 0;
            private long   lastPrintTime = 0;

            @Override
            public void handle(long now) {
                double deltaT = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;

                if (!isPaused) time += deltaT / timeSpeed;

                systeme.update(time, doTrajectoryRender);
                if (cameraController != null) cameraController.update();

                if (now - lastPrintTime > 5_000_000_000L) {
                    systeme.printPositions();
                    lastPrintTime = now;
                }
            }
        }.start();
    }
}
