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
    public final double SCALE_DISTANCE = 1e6;

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
        camera.setFarClip(10000.0);
        camera.getTransforms().addAll(rotateX, rotateY);
        camera.setTranslateX(WIDTH/2);
        camera.setTranslateY(HEIGHT/2);
        camera.setTranslateZ(-1000);
        scene.setCamera(camera);
    }

    // Configuration des contrôles de caméra
    private void setupCameraControls(Scene scene) {
        // Désactiver le zoom avec la molette
        scene.setOnScroll(event -> {
            event.consume();
        });

        // Rotation avec clic droit
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

                // Inverser UNIQUEMENT la rotation horizontale (Y) si la caméra est en Z+
                double invertX = (camera.getTranslateZ() > 0) ? -1 : 1;

                rotateY.setAngle(rotateY.getAngle() - mouseDeltaX * modifier);
                rotateX.setAngle(rotateX.getAngle() + mouseDeltaY * modifier * invertX); // Pas d'inversion pour X
            }
        });

        // Déplacement avec ZQSD (relatif à la direction de la caméra)
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
                    camera.setTranslateX(camera.getTranslateX() - right.getX() * moveSpeed);
                    camera.setTranslateZ(camera.getTranslateZ() - right.getZ() * moveSpeed);
                    break;
                case D:
                    camera.setTranslateX(camera.getTranslateX() + right.getX() * moveSpeed);
                    camera.setTranslateZ(camera.getTranslateZ() + right.getZ() * moveSpeed);
                    break;
            }
        });
    }



    @Override
    public void start(Stage primaryStage) {
        Group root = new Group();
        primaryStage.setTitle("Système Solaire");
        Scene scene = new Scene(root, WIDTH, HEIGHT, true);
        scene.setFill(Color.BLACK);
        initCamera(scene);
        setupCameraControls(scene);

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

        primaryStage.setScene(scene);
        primaryStage.show();

        // Animation avec affichage des coordonnées
        new AnimationTimer() {
            private long lastTime = System.nanoTime();
            private long lastPrintTime = 0;
            @Override
            public void handle(long now) {
                double deltaT = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;
                if (now - lastPrintTime > 1_000_000_000L) {
                    printCameraPosition();
                    lastPrintTime = now;
                }
            }
        }.start();
    }

    // Méthodes pour récupérer les coordonnées de la caméra
    public double getCameraX() {
        return camera.getTranslateX();
    }

    public double getCameraY() {
        return camera.getTranslateY();
    }

    public double getCameraZ() {
        return camera.getTranslateZ();
    }

    public double getRotationX() {
        return rotateX.getAngle();
    }

    public double getRotationY() {
        return rotateY.getAngle();
    }

    // Méthode pour récupérer toutes les coordonnées formatées
    public String getCameraCoordinates() {
        return String.format("Position: (%.2f, %.2f, %.2f) - Rotation: (%.2f°, %.2f°)",
                           getCameraX(), getCameraY(), getCameraZ(),
                           getRotationX(), getRotationY());
    }

    // Méthode pour afficher les coordonnées dans la console
    public void printCameraPosition() {
        System.out.println("=== COORDONNÉES CAMÉRA ===");
        System.out.println(getCameraCoordinates());
        System.out.println("==========================");
    }

    // Méthode pour réinitialiser la vue
    public void resetCamera() {
        rotateX.setAngle(0);
        rotateY.setAngle(0);
        camera.setTranslateX(WIDTH/2);
        camera.setTranslateY(HEIGHT/2);
        camera.setTranslateZ(-300);
        System.out.println("Caméra réinitialisée !");
        printCameraPosition();
    }
}
