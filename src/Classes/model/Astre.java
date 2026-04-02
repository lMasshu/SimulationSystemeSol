package Classes.model;

import java.io.InputStream;
import java.util.Vector;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;
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
    private   MeshView      orbitMeshView;
    private   TriangleMesh  orbitMesh;
    private   Point3D[]     orbitCalculatedPoints;
    private   PhongMaterial orbitMaterial;
    protected PhongMaterial material;
    private   Rotate        selfRotation;
    private   long          lastVisualUpdateMillis = 0;
    private   Point3D       lastCameraVisualPos    = null;

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
    /**
     * Initialise la trajectoire orbitale complète sous forme de MeshView unique.
     * @param scaleDistance Facteur d'échelle pour l'UA.
     * @param factor Facteur d'affichage propre à l'astre (lunes).
     */
    public void initOrbitPath(double scaleDistance, double factor) {
        if (periodeOrbitale <= 0 || orbitMeshView != null) return;

        // --- OPTIMISATION : 120 segments au lieu de 360 ---
        int numPoints = 120;
        orbitCalculatedPoints = new Point3D[numPoints];
        double step = periodeOrbitale / (double)numPoints;

        for (int i = 0; i < numPoints; i++) {
            double[] pos = orbite.calculerPosition(i * step);
            orbitCalculatedPoints[i] = new Point3D(
                pos[0] * scaleDistance * factor,
                pos[2] * scaleDistance * factor, // Inclinaison sur Y
                pos[1] * scaleDistance * factor  // Ecliptique sur Z
            );
        }

        // --- Construction du Mesh initial ---
        orbitMesh = new TriangleMesh();
        
        // Coordonnées de texture vides (requises par TriangleMesh)
        orbitMesh.getTexCoords().addAll(0, 0);

        // Faces : On relie chaque segment (4 faces par segment, 2 triangles par face)
        for (int i = 0; i < numPoints; i++) {
            int next = (i + 1) % numPoints;
            int i4 = i * 4;
            int next4 = next * 4;

            // 4 faces du tube (carré)
            for (int f = 0; f < 4; f++) {
                int fNext = (f + 1) % 4;
                // Triangle 1
                orbitMesh.getFaces().addAll(i4 + f, 0, next4 + f, 0, next4 + fNext, 0);
                // Triangle 2
                orbitMesh.getFaces().addAll(i4 + f, 0, next4 + fNext, 0, i4 + fNext, 0);
            }
        }

        orbitMeshView = new MeshView(orbitMesh);
        orbitMaterial = new PhongMaterial();
        orbitMaterial.setDiffuseColor(new Color(couleur.getRed(), couleur.getGreen(), couleur.getBlue(), Config.ORBIT_MIN_OPACITY));
        orbitMeshView.setMaterial(orbitMaterial);
        
        orbitPathGroup = new Group(orbitMeshView);
        root.getChildren().add(orbitPathGroup);
        orbitPathGroup.setVisible(false);

        // Initialisation des points du Mesh à une épaisseur de base
        updateMeshPoints(Config.ORBIT_MIN_RADIUS);
    }

    /**
     * Calcule le niveau de détail dynamique de l'orbite en fonction de la distance à la caméra.
     */
    public void updateOrbitVisuals(Point3D cameraPos) {
        if (orbitMeshView == null || !orbitPathGroup.isVisible()) return;

        long now = System.currentTimeMillis();
        if (now - lastVisualUpdateMillis < 100) return;
        if (lastCameraVisualPos != null && cameraPos.distance(lastCameraVisualPos) < 5.0) return;

        lastVisualUpdateMillis = now;
        lastCameraVisualPos = cameraPos;

        double distance = cameraPos.distance(this.position);
        
        // --- NOUVELLE LOGIQUE : Rayon proportionnel à la distance ---
        // On utilise la distance de référence pour définir le rayon de base
        double factor = distance / Config.ORBIT_REFERENCE_DIST;
        
        // Update Opacity (logarithmique reste bien pour l'opacité)
        double logRatio = Math.log10(factor + 1);
        double opacity = Math.min(Config.ORBIT_MAX_OPACITY, Config.ORBIT_MIN_OPACITY * (1.0 + logRatio * 10.0));
        orbitMaterial.setDiffuseColor(new Color(couleur.getRed(), couleur.getGreen(), couleur.getBlue(), opacity));

        // Update Thickness (linéaire pour plus de réactivité au zoom)
        // On bride entre Config.ORBIT_MIN_RADIUS et Config.ORBIT_MAX_RADIUS
        double radius = Math.max(Config.ORBIT_MIN_RADIUS, Math.min(Config.ORBIT_MAX_RADIUS, factor * 2.0));
        updateMeshPoints(radius);
    }

    private void updateMeshPoints(double radius) {
        if (orbitMesh == null) return;
        
        int numPoints = orbitCalculatedPoints.length;
        float[] points = new float[numPoints * 4 * 3]; // 4 vertices par point, 3 coordonées (x,y,z)
        float r = (float)radius;

        for (int i = 0; i < numPoints; i++) {
            Point3D p = orbitCalculatedPoints[i];
            
            // Calcul de la direction du segment (tangente locale)
            Point3D next = orbitCalculatedPoints[(i + 1) % numPoints];
            Point3D prev = orbitCalculatedPoints[(i - 1 + numPoints) % numPoints];
            Point3D tangent = next.subtract(prev).normalize();

            // Création d'un repère local (Vecteurs orthogonaux U et V)
            // On utilise un vecteur vertical arbitraire pour débuter le cross product
            Point3D up = (Math.abs(tangent.getY()) > 0.9) ? new Point3D(1, 0, 0) : new Point3D(0, 1, 0);
            Point3D u  = tangent.crossProduct(up).normalize();
            Point3D v  = tangent.crossProduct(u).normalize();

            // 4 vertices formant un carré perpendiculaire à la tangente
            // Point 0
            points[i * 12 + 0] = (float)(p.getX() + (u.getX() + v.getX()) * r);
            points[i * 12 + 1] = (float)(p.getY() + (u.getY() + v.getY()) * r);
            points[i * 12 + 2] = (float)(p.getZ() + (u.getZ() + v.getZ()) * r);
            // Point 1
            points[i * 12 + 3] = (float)(p.getX() + (u.getX() - v.getX()) * r);
            points[i * 12 + 4] = (float)(p.getY() + (u.getY() - v.getY()) * r);
            points[i * 12 + 5] = (float)(p.getZ() + (u.getZ() - v.getZ()) * r);
            // Point 2
            points[i * 12 + 6] = (float)(p.getX() + (-u.getX() - v.getX()) * r);
            points[i * 12 + 7] = (float)(p.getY() + (-u.getY() - v.getY()) * r);
            points[i * 12 + 8] = (float)(p.getZ() + (-u.getZ() - v.getZ()) * r);
            // Point 3
            points[i * 12 + 9] = (float)(p.getX() + (-u.getX() + v.getX()) * r);
            points[i * 12 + 10] = (float)(p.getY() + (-u.getY() + v.getY()) * r);
            points[i * 12 + 11] = (float)(p.getZ() + (-u.getZ() + v.getZ()) * r);
        }

        orbitMesh.getPoints().setAll(points);
    }

    // ===================================================================
    //  UTILITAIRES
    // ===================================================================

    /** Efface les trajectoires accumulées (non utilisé avec le nouveau système). */
    public void resetTrajectory() {
        if (orbitPathGroup != null) orbitPathGroup.setVisible(false);
    }

    //getter pour getTranslateX
    public double getTranslateX() {
        return sprite.getTranslateX();
    }

    //getter pour getTranslateY
    public double getTranslateY() {
        return sprite.getTranslateY();
    }

    //getter pour getTranslateZ
    public double getTranslateZ() {
        return sprite.getTranslateZ();
    }

    @Override
    public String toString() {
        return String.format("Astre{nom='%s', masse=%.2e kg, diamètre=%.2f km, position=%s}",
                             nom, masse, diametre, position);
    }
}
