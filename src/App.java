import java.util.Arrays;
import java.util.Vector;
import Classes.Astre;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Point3D;
import javafx.scene.Scene;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.stage.Stage;

public class App extends Application {
    public final int WIDTH = 800;
    public final int HEIGHT = 600;

    // Variables pour la caméra et les transformations
    private PerspectiveCamera camera;
    private final Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private double mousePosX, mousePosY;
    private double mouseOldX, mouseOldY;

    // Planètes
    private Astre soleil, mercure, venus, terre, mars, jupiter, saturne, uranus, neptune;

    public static void main(String[] args) {
        launch(args);
    }

    // Initialisation de la caméra
    private void initCamera(Scene scene) {
        camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.getTransforms().addAll(rotateX, rotateY);
        camera.setTranslateZ(-1000); // Position initiale
        
        scene.setCamera(camera);
    }

    // Configuration des contrôles de caméra
    private void setupCameraControls(Scene scene) {
        // Gestion du zoom avec la molette
        scene.setOnScroll(event -> {
            double delta = event.getDeltaY();
            double zoomFactor = 1.1;
            
            if (delta > 0) {
                camera.setTranslateZ(camera.getTranslateZ() / zoomFactor);
            } else {
                camera.setTranslateZ(camera.getTranslateZ() * zoomFactor);
            }
            
            event.consume();
        });

        // Gestion du clic droit pour tourner dans l'espace
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
                
                // Sensibilité de la rotation
                double modifier = 0.05;
                
                // Rotation autour de l'axe Y (mouvement horizontal de la souris)
                rotateY.setAngle(rotateY.getAngle() - mouseDeltaX * modifier);
                
                // Rotation autour de l'axe X (mouvement vertical de la souris)
                rotateX.setAngle(rotateX.getAngle() + mouseDeltaY * modifier);
            }
        });
    }

    @Override
    public void start(Stage primaryStage) {
        Group root = new Group();
        primaryStage.setTitle("Système Solaire");
        Scene scene = new Scene(root, WIDTH, HEIGHT, true);
        scene.setFill(Color.BLACK);

        // 1. D'ABORD initialiser la caméra
        initCamera(scene);

        // 2. ENSUITE configurer les contrôles de caméra
        setupCameraControls(scene);

        // 3. PUIS créer les objets
        // Création du Soleil
        soleil = new Astre(
            "Soleil",
            1.989e30,
            30,
            new Point3D(WIDTH/2, HEIGHT/2, 0),
            new Vector<>(Arrays.asList(0.0, 0.0, 0.0)),
            root,
            Color.YELLOW
        );
        soleil.renderAstre();

        // 4. ENFIN afficher la scène
        primaryStage.setScene(scene);
        primaryStage.show();

        // Animation
        new AnimationTimer() {
            private long lastTime = System.nanoTime();

            @Override
            public void handle(long now) {
                double deltaT = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;

                // Ajoutez ici votre logique d'animation
                // Exemple : mise à jour des positions des planètes
            }
        }.start();
    }

    // Méthode pour réinitialiser la vue
    public void resetCamera() {
        rotateX.setAngle(0);
        rotateY.setAngle(0);
        // Revenir à la position centrée sur le soleil
        camera.setTranslateX(WIDTH/2);
        camera.setTranslateY(HEIGHT/2);
        camera.setTranslateZ(-300);
    }
}