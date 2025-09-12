package Classes;

import java.util.Vector;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;

public class Terre extends Astre{
    public Terre(String nom, double masse, double diametre, Point3D position, Vector<Double> vitesse, Group root,
            Color couleur) {
        super(nom, masse, diametre, position, vitesse, root, couleur);
    }
    // Constantes physiques
    private static final double PI = Math.PI;
    private static final double AU = 1.495978707e11; // 1 unité astronomique en mètres
    private static final double SECONDS_PER_DAY = 86400.0;
    private static final double DAYS_PER_YEAR = 365.256;

    // Paramètres orbitaux moyens de la Terre
    private static final double a = AU; // demi-grand axe (m)
    private static final double e = 0.0167; // excentricité
    private static final double T = DAYS_PER_YEAR * SECONDS_PER_DAY; // période orbitale (s)
    private static final double n = 2 * PI / T; // mouvement moyen (rad/s)

    // Date du périhélie (environ 3 janvier)
    private static final int PERIHELION_MONTH = 1;
    private static final int PERIHELION_DAY = 3;


    // tout les getter

    public double getSecPerDay(){
        return SECONDS_PER_DAY;
    }

    public double getDayPerYear(){
        return DAYS_PER_YEAR;
    }

    public double getDemiGrandAxe(){
        return a; 
    }

    public double getExcen(){
        return e;
    }

    public double getPeriodeOrbital(){
        return T;
    }

    public double getMouvMoyen(){
        return n;
    }

    public int getPerihelionMonth(){
        return PERIHELION_MONTH;
    }

    public int getPerihelionDay(){
        return PERIHELION_DAY;
    }

    public void updatePosition(Point3D nouvellePosition) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updatePosition'");
    }

}