package Classes;

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
    private Point3D previousPosition = null;

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
        
        
        
        this.position = new Point3D(
            pos[0] * SCALE_DISTANCE + soleilPosition.getX(), 
            pos[1] * SCALE_DISTANCE + soleilPosition.getY(), 
            pos[2] * SCALE_DISTANCE + soleilPosition.getZ()  
        );
    }


    public void updatePositionAroundAstre(double t, Point3D planetePosition, double SCALE_DISTANCE, double factor){
        double[] pos = orbite.calculerPosition(t);
        
        double echelleLunaire = SCALE_DISTANCE * factor; // Facteur 10 pour rendre la Lune visible
        
        this.position = new Point3D(
            pos[0] * echelleLunaire + planetePosition.getX(),
            pos[1] * echelleLunaire + planetePosition.getY(), 
            pos[2] * echelleLunaire + planetePosition.getZ()
        );
    }




    public void updatePositionAroundPlanet(double t, Point3D planetPosition, double distanceMoyenne, double SCREEN_MAX_RADIUS) {
        // Calcul de la position sur l'orbite (en unités réelles)
        double[] pos = orbite.calculerPosition(t);

        // Calcul dynamique de l'échelle
        double echelle = SCREEN_MAX_RADIUS / distanceMoyenne;

        // Mise à jour de la position en pixels
        this.position = new Point3D(
            pos[0] * echelle + planetPosition.getX(),
            pos[1] * echelle + planetPosition.getY(),
            pos[2] * echelle + planetPosition.getZ()
        );
    }

   public boolean renderAstreSansTrajectoire(boolean traj, String texturePath) {
        try {
            if (this.sprite == null) {
                Sphere sphere = new Sphere(this.diametre / 2, 32);

                // Création du matériau avec texture
                PhongMaterial material = new PhongMaterial();
                if (texturePath != null && !texturePath.isEmpty()) {
                    InputStream textureStream = getClass().getResourceAsStream(texturePath);
                    if (textureStream != null) {
                        Image texture = new Image(textureStream);
                        material.setDiffuseMap(texture); // applique la texture
                    } else {
                        System.err.println("Erreur : texture introuvable à " + texturePath);
                        material.setDiffuseColor(Color.GRAY); // fallback
                    }
                } else {
                    material.setDiffuseColor(Color.GRAY); // fallback
                }

                sphere.setMaterial(material);
                this.material = material; // garder référence
                this.sprite = sphere;
                root.getChildren().add(sphere);
                this.previousPosition = this.position; // Initialiser la position précédente
            }

            // Mettre à jour seulement la position du sprite
            this.sprite.setTranslateX(this.position.getX());
            this.sprite.setTranslateY(this.position.getY());
            this.sprite.setTranslateZ(this.position.getZ());

            if (traj) {
                // Créer une ligne blanche semi-transparente entre previousPosition et position
                if (previousPosition != null && !previousPosition.equals(position)) {
                    Point3D start = previousPosition;
                    Point3D end = position;

                    Point3D diff = end.subtract(start);
                    double height = diff.magnitude();

                    if (height > 0) {
                        Cylinder line = new Cylinder(0.4, height); // 0.4px d'épaisseur
                        PhongMaterial lineMaterial = new PhongMaterial();
                        lineMaterial.setDiffuseColor(Color.rgb(255, 255, 255, 0.5)); // 50% transparent
                        lineMaterial.setSpecularColor(Color.WHITE);
                        line.setMaterial(lineMaterial);

                        // Positionner au milieu
                        Point3D mid = start.midpoint(end);
                        line.setTranslateX(mid.getX());
                        line.setTranslateY(mid.getY());
                        line.setTranslateZ(mid.getZ());

                        // Orientation
                        Point3D yAxis = new Point3D(0, 1, 0);
                        Point3D axisOfRotation = yAxis.crossProduct(diff);
                        double angle = Math.toDegrees(Math.acos(diff.normalize().dotProduct(yAxis)));
                        if (!Double.isNaN(angle) && axisOfRotation.magnitude() > 0) {
                            line.getTransforms().add(new Rotate(angle, axisOfRotation));
                        }

                        root.getChildren().add(line);
                    }
                }

                // Mettre à jour pour le prochain frame
                previousPosition = position;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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
