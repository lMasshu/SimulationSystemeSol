package Classes.model;

/**
 * Calcul de la position d'un astre sur son orbite elliptique à un instant donné,
 * en appliquant les lois de Kepler et les éléments orbitaux standards.
 *
 * Les angles sont stockés en radians en interne ; le constructeur accepte des degrés.
 */
public class OrbitePlanete {

    private final double a;     // Demi-grand axe (UA)
    private final double e;     // Excentricité
    private final double T;     // Période orbitale (années)
    private final double i;     // Inclinaison (radians)
    private final double omega; // Longitude du nœud ascendant (radians)
    private final double w;     // Argument du périhélie (radians)

    // ===================================================================
    //  CONSTRUCTEURS
    // ===================================================================

    /**
     * Crée une orbite elliptique complète en 3D.
     * @param perihelieUA       Distance au periapside (UA)
     * @param aphelieUA         Distance à l'apoapside (UA)
     * @param T                 Période orbitale (années)
     * @param inclinaison       Inclinaison par rapport au plan de référence (degrés)
     * @param longitudeNoeud    Longitude du nœud ascendant Ω (degrés)
     * @param argumentPerihelie Argument du périhélie ω (degrés)
     */
    public OrbitePlanete(double perihelieUA, double aphelieUA, double T,
                         double inclinaison, double longitudeNoeud, double argumentPerihelie) {
        this.a     = (perihelieUA + aphelieUA) / 2.0;
        this.e     = (aphelieUA - perihelieUA) / (aphelieUA + perihelieUA);
        this.T     = T;
        this.i     = Math.toRadians(inclinaison);
        this.omega = Math.toRadians(longitudeNoeud);
        this.w     = Math.toRadians(argumentPerihelie);

        System.out.printf("Orbite créée : T=%.4f ans, a=%.4f UA, e=%.6f%n", T, a, e);
    }

    /** Orbite plane simplifiée (i = Ω = ω = 0). */
    public OrbitePlanete(double perihelieUA, double aphelieUA, double T) {
        this(perihelieUA, aphelieUA, T, 0, 0, 0);
    }

    // ===================================================================
    //  CALCUL DE POSITION
    // ===================================================================

    /**
     * Calcule la position 3D de l'astre en UA à l'instant {@code t}.
     * @param t Temps écoulé en années terrestres.
     * @return  Tableau [x, y, z] en UA dans le repère héliocentrique.
     */
    public double[] calculerPosition(double t) {
        // Anomalie moyenne
        double M = 2 * Math.PI * (t / T);

        // Anomalie excentrique (méthode itérative de Newton)
        double E = resolverKepler(M, e);

        // Anomalie vraie θ
        double theta = 2 * Math.atan2(
            Math.sqrt(1 + e) * Math.sin(E / 2),
            Math.sqrt(1 - e) * Math.cos(E / 2)
        );

        // Distance au foyer (UA)
        double r = a * (1 - e * e) / (1 + e * Math.cos(theta));

        // Position dans le plan orbital (axe x = direction du périhélie)
        double xOrb = r * Math.cos(theta + w);
        double yOrb = r * Math.sin(theta + w);

        // Application de l'inclinaison (rotation autour de x)
        double yInc = yOrb * Math.cos(i);
        double z    = yOrb * Math.sin(i);

        // Application de Ω (rotation autour de z)
        double xFinal = xOrb * Math.cos(omega) - yInc * Math.sin(omega);
        double yFinal = xOrb * Math.sin(omega) + yInc * Math.cos(omega);

        return new double[]{ xFinal, yFinal, z };
    }

    // ===================================================================
    //  PRIVÉ
    // ===================================================================

    /** Résout l'équation de Kepler E − e·sin(E) = M par la méthode de Newton. */
    private double resolverKepler(double M, double e) {
        double E = M;
        double delta;
        do {
            delta = (M - (E - e * Math.sin(E))) / (1 - e * Math.cos(E));
            E += delta;
        } while (Math.abs(delta) > 1e-6);
        return E;
    }
}
