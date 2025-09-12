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

    public Point3D getPosition() {
        return this.position;
    }
}
