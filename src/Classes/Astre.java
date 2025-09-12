package Classes;

import java.util.Vector;
import javafx.collections.ObservableList;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Sphere;

public class Astre {
    public String nom;
    public double masse;
    public double diametre;
    public Point3D position;
    public Vector<Double> vitesse;
    public Group root;
    public Color couleur;
    protected Sphere sprite;
    protected Polyline trajectory;

    public Astre(String nom, double masse, double diametre, Point3D position, Vector<Double> vitesse, Group root, Color couleur) {
        this.root = root;
        this.nom = nom;
        this.masse = masse;
        this.diametre = diametre;
        this.position = position;
        this.vitesse = vitesse;
        this.couleur = couleur;
    }

    public double distance(Astre autreAstre) {
        return this.position.distance(autreAstre.position);
    }

    public boolean renderAstre() {
        if (this.sprite == null) {
            Sphere sphere = new Sphere(this.diametre / 2, 32);
            PhongMaterial material = new PhongMaterial();
            material.setDiffuseColor(couleur);
            sphere.setMaterial(material);
            this.sprite = sphere;
            root.getChildren().add(sphere);
        }

        if (this.trajectory == null) {
            this.trajectory = new Polyline();
            this.trajectory.setStroke(couleur);
            this.trajectory.setStrokeWidth(1.0);
            root.getChildren().add(this.trajectory);
        }

        this.sprite.setTranslateX(this.position.getX());
        this.sprite.setTranslateY(this.position.getY());
        this.sprite.setTranslateZ(this.position.getZ());

        return true;
    }

    public void updatePosition(Point3D nouvellePosition) {
        this.position = nouvellePosition;
        if (this.sprite != null) {
            this.sprite.setTranslateX(nouvellePosition.getX());
            this.sprite.setTranslateY(nouvellePosition.getY());
            this.sprite.setTranslateZ(nouvellePosition.getZ());
        }
        this.updateTrajectory();
    }

    public void updateTrajectory() {
        if (this.trajectory != null) {
            ObservableList<Double> points = this.trajectory.getPoints();
            points.addAll(this.position.getX(), this.position.getY());

            if (points.size() > 2000) {
                points.remove(0, 2);
            }
        }
    }

    public void clearTrajectory() {
        if (this.trajectory != null) {
            this.trajectory.getPoints().clear();
        }
    }

    public void setScale(double scale) {
        if (this.sprite != null) {
            this.sprite.setRadius(this.diametre / 2 * scale);
        }
    }

    public double calculerForceGravitationnelle(Astre autreAstre, double G, double distanceScale) {
        double distance = this.distance(autreAstre) * distanceScale;
        return G * (this.masse * autreAstre.masse) / (distance * distance);
    }

    public Point3D calculerAcceleration(Astre autreAstre, double G, double distanceScale) {
        double distance = this.distance(autreAstre) * distanceScale;
        double force = calculerForceGravitationnelle(autreAstre, G, distanceScale);
        double accelerationMagnitude = force / this.masse;

        Point3D direction = autreAstre.position.subtract(this.position).normalize();
        return direction.multiply(accelerationMagnitude);
    }

    public void mettreAJourPositionVitesse(Point3D acceleration, double deltaT, double distanceScale) {
        double newVx = this.vitesse.get(0) + acceleration.getX() * deltaT;
        double newVy = this.vitesse.get(1) + acceleration.getY() * deltaT;
        double newVz = this.vitesse.get(2) + acceleration.getZ() * deltaT;

        double newX = this.position.getX() + newVx * deltaT * distanceScale;
        double newY = this.position.getY() + newVy * deltaT * distanceScale;
        double newZ = this.position.getZ() + newVz * deltaT * distanceScale;

        this.vitesse.set(0, newVx);
        this.vitesse.set(1, newVy);
        this.vitesse.set(2, newVz);
        this.position = new Point3D(newX, newY, newZ);
    }

    @Override
    public String toString() {
        return "Astre{" +
                "nom='" + nom + '\'' +
                ", masse=" + masse +
                ", diametre=" + diametre +
                ", position=" + position +
                ", vitesse=" + vitesse +
                '}';
    }
}
