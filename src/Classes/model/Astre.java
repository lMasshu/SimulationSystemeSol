package Classes.model;

import java.io.InputStream;
import java.util.Vector;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import Classes.data.Config;

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
    public double  periodeRotation; // heures
    public OrbitePlanete orbite;

    // --- État spatial ---
    public Point3D         position;  // pixels
    public Vector<Double>  vitesse;   // km/s (réservé)

    // --- Rendu JavaFX ---
    public Group         root;
    public Color         couleur;
    protected Sphere        sprite;
    protected Group         orbitPathGroup;
    protected PhongMaterial material;
    private   Rotate        selfRotation;

    // ===================================================================
    //  CONSTRUCTEURS
    // ===================================================================

    /** Constructeur complet avec paramètres orbitaux 3D (inclinaison, Ω, ω). */
    public Astre(String nom, double masse, double diametre,
                 double perihelie, double aphelie, double periodeOrbitale,
                 double inclinaison, double longitudeNoeud, double argumentPerihelie,
                 double periodeRotationHeures,
                 Group root, Color couleur) {
        this.nom            = nom;
        this.masse          = masse;
        this.diametre       = diametre;
        this.perihelie      = perihelie;
        this.aphelie        = aphelie;
        this.periodeOrbitale = periodeOrbitale;
        this.periodeRotation = periodeRotationHeures;
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
             0, 0, 0, 0, root, couleur);
    }

    private void initVectors() {
        this.position = new Point3D(0, 0, 0);
        this.vitesse  = new Vector<>();
        this.vitesse.add(0.0); this.vitesse.add(0.0); this.vitesse.add(0.0);
        this.material = new PhongMaterial();
        this.material.setDiffuseColor(couleur);
        
        // Initialisation de la rotation (autour de l'axe Y par défaut)
        this.selfRotation = new Rotate(0, Rotate.Y_AXIS);
    }

    // ===================================================================
    //  MISE À JOUR DE POSITION
    // ===================================================================

    /** Met à jour la position en orbite autour du Soleil (ou d'un centre quelconque). */
    public void updatePosition(double t, double scaleDistance, Point3D centrePosition) {
        double[] pos = orbite.calculerPosition(t);
        this.position = new Point3D(
            pos[0] * scaleDistance + centrePosition.getX(),
            pos[2] * scaleDistance + centrePosition.getY(), // Inclinaison sur Y
            pos[1] * scaleDistance + centrePosition.getZ()  // Ecliptique sur Z
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
            pos[2] * scale + parentPosition.getY(), // Inclinaison sur Y
            pos[1] * scale + parentPosition.getZ()  // Ecliptique sur Z
        );
    }

    /**
     * Met à jour la rotation propre de l'astre.
     * @param t Temps écoulé en années terrestres.
     */
    public void updateSelfRotation(double t) {
        if (periodeRotation == 0) return;
        
        // Conversion du temps (années) en heures
        double totalHours = t * 365.25 * 24.0;
        
        // Calcul de l'angle (une rotation complète = 360°)
        // On utilise le modulo pour éviter des valeurs d'angle trop grandes
        double angle = (totalHours / periodeRotation) * 360.0;
        selfRotation.setAngle(angle % 360.0);
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
    /**
     * Mise à jour du sprite 3D et du tracé des trajectoires.
     *
     * @param traj           Activer le tracé de trajectoire (optionnel).
     * @param texturePath    Chemin vers la texture PNG.
     * @param parentPosition Position de l'astre parent (pour translater l'orbite si nécessaire).
     */
    public boolean render(boolean traj, String texturePath, Point3D parentPosition) {
        try {
            // --- Initialisation unique du sprite ---
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
                sphere.setUserData(this);
                sphere.getTransforms().add(selfRotation);

                this.material        = mat;
                this.sprite          = sphere;
                root.getChildren().add(sphere);
            }

            // --- Déplacement du sprite ---
            sprite.setTranslateX(position.getX());
            sprite.setTranslateY(position.getY());
            sprite.setTranslateZ(position.getZ());

            // --- Visibilité et position de l'orbite ---
            if (orbitPathGroup != null) {
                orbitPathGroup.setVisible(traj);
                if (parentPosition != null) {
                    orbitPathGroup.setTranslateX(parentPosition.getX());
                    orbitPathGroup.setTranslateY(parentPosition.getY());
                    orbitPathGroup.setTranslateZ(parentPosition.getZ());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /** Overload pour les astres sans parent (ex: Soleil). */
    public boolean render(boolean traj, String texturePath) {
        return render(traj, texturePath, null);
    }

    /**
     * Calcule et affiche l'orbite complète de l'astre.
     * @param scaleDistance Facteur d'échelle pour les distances.
     * @param factor Facteur multiplicatif pour rendre les orbites visibles (lunes).
     */
    public void initOrbitPath(double scaleDistance, double factor) {
        if (periodeOrbitale <= 0 || orbitPathGroup != null) return;

        orbitPathGroup = new Group();
        double step = periodeOrbitale / 360.0; // 360 segments
        
        Point3D prev = null;
        for (int j = 0; j <= 360; j++) {
            double t = j * step;
            double[] pos = orbite.calculerPosition(t);
            Point3D current = new Point3D(
                pos[0] * scaleDistance * factor,
                pos[2] * scaleDistance * factor, // Inclinaison sur Y
                pos[1] * scaleDistance * factor  // Ecliptique sur Z
            );

            if (prev != null) {
                Point3D diff = current.subtract(prev);
                double height = diff.magnitude();
                if (height > 0) {
                    Cylinder segment = new Cylinder(Config.ORBIT_RADIUS, height);
                    PhongMaterial sm = new PhongMaterial();
                    sm.setDiffuseColor(new Color(couleur.getRed(), couleur.getGreen(), couleur.getBlue(), Config.ORBIT_OPACITY));
                    segment.setMaterial(sm);

                    Point3D mid = prev.midpoint(current);
                    segment.setTranslateX(mid.getX());
                    segment.setTranslateY(mid.getY());
                    segment.setTranslateZ(mid.getZ());

                    Point3D yAxis = new Point3D(0, 1, 0);
                    Point3D axis = yAxis.crossProduct(diff);
                    double angle = Math.toDegrees(Math.acos(diff.normalize().dotProduct(yAxis)));
                    if (!Double.isNaN(angle) && axis.magnitude() > 0) {
                        segment.getTransforms().add(new Rotate(angle, axis));
                    }
                    orbitPathGroup.getChildren().add(segment);
                }
            }
            prev = current;
        }
        
        // On n'ajoute pas encore à root car pour les lunes, il faudra peut-être l'ajouter au parent
        // Mais par défaut, on peut l'ajouter ici si on veut rester sur une structure simple.
        root.getChildren().add(orbitPathGroup);
        orbitPathGroup.setVisible(false);
    }

    // ===================================================================
    //  UTILITAIRES
    // ===================================================================

    /** Efface les trajectoires accumulées (non utilisé avec le nouveau système). */
    public void resetTrajectory() {
        if (orbitPathGroup != null) orbitPathGroup.setVisible(false);
    }

    @Override
    public String toString() {
        return String.format("Astre{nom='%s', masse=%.2e kg, diamètre=%.2f km, position=%s}",
                             nom, masse, diametre, position);
    }
}
