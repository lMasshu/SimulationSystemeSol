package Classes;

import java.util.Vector;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Sphere;

public class Astre {
    // Constantes
    

    // Propriétés de l'astre
    public String nom;
    public double masse; // en kg
    public double diametre; // en km
    public double perihelie; // en UA
    public double aphelie; // en UA
    public double excentricite;
    public double periodeOrbitale; // en années
    public OrbitePlanete orbite;
    public Point3D position; // en pixels
    public Vector<Double> vitesse; // en km/s
    public Group root;
    public Color couleur;

    // Éléments graphiques
    protected Sphere sprite;
    protected Polyline trajectory;
    protected PhongMaterial material;

    // Constructeur
    public Astre(String nom, double masse, double diametre, double perihelie, double aphelie, double periodeOrbitale, Group root, Color couleur) {
        this.nom = nom;
        this.masse = masse;
        this.diametre = diametre;
        this.perihelie = perihelie;
        this.aphelie = aphelie;
        this.periodeOrbitale = periodeOrbitale;
        this.excentricite = (aphelie - perihelie) / (aphelie + perihelie);
        this.orbite = new OrbitePlanete(perihelie, aphelie, periodeOrbitale);
        this.root = root;
        this.couleur = couleur;
        this.position = new Point3D(0, 0, 0);
        this.vitesse = new Vector<>();
        this.vitesse.add(0.0);
        this.vitesse.add(0.0);
        this.vitesse.add(0.0);
        this.material = new PhongMaterial();
        this.material.setDiffuseColor(couleur);
    }

    public Astre(String nom, double masse, double diametre, double perihelie, double aphelie, 
                double periodeOrbitale, double inclinaison, double longitudeNoeud, 
                double argumentPerihelie, Group root, Color couleur) {
        this.nom = nom;
        this.masse = masse;
        this.diametre = diametre;
        this.perihelie = perihelie;
        this.aphelie = aphelie;
        this.periodeOrbitale = periodeOrbitale;
        this.excentricite = (aphelie - perihelie) / (aphelie + perihelie);
        
        // Création d'une orbite 3D avec tous les paramètres
        this.orbite = new OrbitePlanete(perihelie, aphelie, periodeOrbitale, 
                                    inclinaison, longitudeNoeud, argumentPerihelie);
        
        this.root = root;
        this.couleur = couleur;
        this.position = new Point3D(0, 0, 0);
        this.vitesse = new Vector<>();
        this.vitesse.add(0.0);
        this.vitesse.add(0.0);
        this.vitesse.add(0.0);
        this.material = new PhongMaterial();
        this.material.setDiffuseColor(couleur);
    }

    // Met à jour la position de l'astre en fonction du temps
    public void updatePosition(double t, double SCALE_DISTANCE, Point3D soleilPosition) {
        double[] pos = orbite.calculerPosition(t);
        
        // CORRECTION : Multiplier par SCALE_DISTANCE pour convertir UA en pixels
        this.position = new Point3D(
            pos[0] * SCALE_DISTANCE + soleilPosition.getX(), // UA × pixels/UA + centre
            pos[1] * SCALE_DISTANCE + soleilPosition.getY(), // UA × pixels/UA + centre
            pos[2] * SCALE_DISTANCE + soleilPosition.getZ()  // UA × pixels/UA + centre (pour la 3D)
        );
    }
    
    // Affiche l'astre et sa trajectoire
    public boolean renderAstreSansTrajectoire() {
        if (this.sprite == null) {
            Sphere sphere = new Sphere(this.diametre / 2, 32);
            sphere.setMaterial(this.material);
            this.sprite = sphere;
            root.getChildren().add(sphere);
        }

        // Mettre à jour seulement la position du sprite (pas de trajectoire)
        this.sprite.setTranslateX(this.position.getX());
        this.sprite.setTranslateY(this.position.getY());
        this.sprite.setTranslateZ(this.position.getZ());

        return true;
    }

    // Réinitialise la trajectoire
    public void resetTrajectory() {
        if (this.trajectory != null) {
            this.trajectory.getPoints().clear();
        }
    }

    // Retourne une représentation textuelle de l'astre
    @Override
    public String toString() {
        return String.format(
            "Astre{nom='%s', masse=%.2e kg, diamètre=%.2f km, position=%s}",
            nom, masse, diametre, position.toString()
        );
    }
}
