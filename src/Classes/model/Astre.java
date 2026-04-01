package Classes.model;

import java.io.InputStream;
import java.util.Vector;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;

/**
 * Représentation d'un corps céleste dans la simulation 3D.
 *
 * Regroupe l'état physique (masse, orbite, position) et les éléments
 * graphiques JavaFX (sphère 3D, trajectoire). La texture est chargée
 * une seule fois ({@link #renderAstreSansTrajectoire}) et mise en cache.
 */
public class Astre {

    // --- Propriétés physiques ---
    public String  nom;
    public double  masse;           // kg
    public double  diametre;        // pixels (déjà redimensionné à la construction)
    public double  perihelie;       // UA
    public double  aphelie;         // UA
    public double  excentricite;
    public double  periodeOrbitale; // années
    public OrbitePlanete orbite;

    // --- État spatial ---
    public Point3D         position;  // pixels
    public Vector<Double>  vitesse;   // km/s (réservé)

    // --- Rendu JavaFX ---
    public Group         root;
    public Color         couleur;
    protected Sphere        sprite;
    protected Polyline      trajectory;
    protected PhongMaterial material;
    private   Point3D       previousPosition = null;

    // ===================================================================
    //  CONSTRUCTEURS
    // ===================================================================

    /** Constructeur complet avec paramètres orbitaux 3D (inclinaison, Ω, ω). */
    public Astre(String nom, double masse, double diametre,
                 double perihelie, double aphelie, double periodeOrbitale,
                 double inclinaison, double longitudeNoeud, double argumentPerihelie,
                 Group root, Color couleur) {
        this.nom            = nom;
        this.masse          = masse;
        this.diametre       = diametre;
        this.perihelie      = perihelie;
        this.aphelie        = aphelie;
        this.periodeOrbitale = periodeOrbitale;
        this.excentricite   = (aphelie - perihelie) / (aphelie + perihelie);
        this.orbite         = new OrbitePlanete(perihelie, aphelie, periodeOrbitale,
                                                inclinaison, longitudeNoeud, argumentPerihelie);
        this.root    = root;
        this.couleur = couleur;
        initVectors();
    }

    /** Constructeur simplifié pour orbites planes (i=0, Ω=0, ω=0). */
    public Astre(String nom, double masse, double diametre,
                 double perihelie, double aphelie, double periodeOrbitale,
                 Group root, Color couleur) {
        this(nom, masse, diametre, perihelie, aphelie, periodeOrbitale,
             0, 0, 0, root, couleur);
    }

    private void initVectors() {
        this.position = new Point3D(0, 0, 0);
        this.vitesse  = new Vector<>();
        this.vitesse.add(0.0); this.vitesse.add(0.0); this.vitesse.add(0.0);
        this.material = new PhongMaterial();
        this.material.setDiffuseColor(couleur);
    }

    // ===================================================================
    //  MISE À JOUR DE POSITION
    // ===================================================================

    /** Met à jour la position en orbite autour du Soleil (ou d'un centre quelconque). */
    public void updatePosition(double t, double scaleDistance, Point3D centrePosition) {
        double[] pos = orbite.calculerPosition(t);
        this.position = new Point3D(
            pos[0] * scaleDistance + centrePosition.getX(),
            pos[1] * scaleDistance + centrePosition.getY(),
            pos[2] * scaleDistance + centrePosition.getZ()
        );
    }

    /**
     * Met à jour la position en orbite autour d'une planète parente.
     * @param factor Facteur multiplicatif de distance (rend l'orbite visible à l'écran).
     */
    public void updatePositionAroundAstre(double t, Point3D parentPosition,
                                          double scaleDistance, double factor) {
        double[] pos   = orbite.calculerPosition(t);
        double   scale = scaleDistance * factor;
        this.position  = new Point3D(
            pos[0] * scale + parentPosition.getX(),
            pos[1] * scale + parentPosition.getY(),
            pos[2] * scale + parentPosition.getZ()
        );
    }

    // ===================================================================
    //  RENDU
    // ===================================================================

    /**
     * Affiche ou met à jour le sprite 3D. La sphère et la texture ne sont
     * créées qu'une seule fois (première invocation), les frames suivantes
     * ne font que déplacer le sprite.
     *
     * @param traj        Activer le tracé de trajectoire (cylindres entre positions).
     * @param texturePath Chemin vers la texture PNG dans les resources.
     */
    public boolean renderAstreSansTrajectoire(boolean traj, String texturePath) {
        try {
            // --- Initialisation unique ---
            if (this.sprite == null) {
                Sphere sphere = new Sphere(this.diametre / 2, 32);

                PhongMaterial mat = new PhongMaterial();
                InputStream stream = getClass().getResourceAsStream(texturePath);
                if (stream != null) {
                    mat.setDiffuseMap(new Image(stream));
                } else {
                    System.err.println("Texture introuvable : " + texturePath);
                    mat.setDiffuseColor(Color.GRAY);
                }
                sphere.setMaterial(mat);
                sphere.setUserData(this); // Pour le mouse picking de CameraController

                this.material        = mat;
                this.sprite          = sphere;
                this.previousPosition = this.position;
                root.getChildren().add(sphere);
            }

            // --- Déplacement du sprite ---
            sprite.setTranslateX(position.getX());
            sprite.setTranslateY(position.getY());
            sprite.setTranslateZ(position.getZ());

            // --- Trajectoire (cylindres) ---
            if (traj && previousPosition != null && !previousPosition.equals(position)) {
                Point3D diff   = position.subtract(previousPosition);
                double  height = diff.magnitude();
                if (height > 0) {
                    Cylinder line = new Cylinder(0.4, height);
                    PhongMaterial lm = new PhongMaterial();
                    lm.setDiffuseColor(Color.rgb(255, 255, 255, 0.5));
                    lm.setSpecularColor(Color.WHITE);
                    line.setMaterial(lm);

                    Point3D mid = previousPosition.midpoint(position);
                    line.setTranslateX(mid.getX());
                    line.setTranslateY(mid.getY());
                    line.setTranslateZ(mid.getZ());

                    Point3D yAxis = new Point3D(0, 1, 0);
                    Point3D axis  = yAxis.crossProduct(diff);
                    double  angle = Math.toDegrees(Math.acos(diff.normalize().dotProduct(yAxis)));
                    if (!Double.isNaN(angle) && axis.magnitude() > 0) {
                        line.getTransforms().add(new Rotate(angle, axis));
                    }
                    root.getChildren().add(line);
                }
            }
            previousPosition = position;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    // ===================================================================
    //  UTILITAIRES
    // ===================================================================

    /** Efface les points de trajectoire accumulés. */
    public void resetTrajectory() {
        if (trajectory != null) trajectory.getPoints().clear();
    }

    @Override
    public String toString() {
        return String.format("Astre{nom='%s', masse=%.2e kg, diamètre=%.2f km, position=%s}",
                             nom, masse, diametre, position);
    }
}
