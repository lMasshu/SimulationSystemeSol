package Classes;

public class OrbitePlanete {
    private double a; // Demi-grand axe (en UA)
    private double e; // Excentricité
    private double T; // Période orbitale (en années)
    private double i; // Inclinaison (en radians)
    private double omega; // Longitude du nœud ascendant (en radians)
    private double w; // Argument du périhélie (en radians)

    public final double SCALE_DISTANCE = 149.6;

    public OrbitePlanete(double perihelieUA, double aphelieUA, double T, 
                        double inclinaison, double longitudeNoeud, double argumentPerihelie) {
        // GARDER LES VALEURS EN UA (ne pas multiplier par 1.496e11)
        this.a = (perihelieUA + aphelieUA) / 2;
        this.e = (aphelieUA - perihelieUA) / (aphelieUA + perihelieUA);
        this.T = T;
        this.i = Math.toRadians(inclinaison);
        this.omega = Math.toRadians(longitudeNoeud);
        this.w = Math.toRadians(argumentPerihelie);
        
        System.out.println("Orbite créée pour " + T + " ans: a=" + a + " UA, e=" + e);
    }


    // Constructeur simplifié pour rétro-compatibilité (orbites dans le plan)
    public OrbitePlanete(double perihelieUA, double aphelieUA, double T) {
        this(perihelieUA, aphelieUA, T, 0, 0, 0); // Orbite dans le plan XY
    }

    public double[] calculerPosition(double t) {
            // Anomalie moyenne
            double M = 2 * Math.PI * (t / T);
            
            // Anomalie excentrique
            double E = resolverKepler(M, e);
            
            // Anomalie vraie
            double theta = 2 * Math.atan2(
                Math.sqrt(1 + e) * Math.sin(E / 2),
                Math.sqrt(1 - e) * Math.cos(E / 2)
            );
            
            // Distance au foyer (en UA)
            double r = a * (1 - e * e) / (1 + e * Math.cos(theta));
            
            // Position dans le plan orbital (version simplifiée d'abord)
            double x = r * Math.cos(theta + Math.toRadians(w));
            double y = r * Math.sin(theta + Math.toRadians(w));
            
            // Application de l'inclinaison (rotation autour de l'axe X)
            double yInclined = y * Math.cos(i);
            double z = y * Math.sin(i);
            
            // Application de la longitude du nœud (rotation autour de l'axe Z)
            double xFinal = x * Math.cos(omega) - yInclined * Math.sin(omega);
            double yFinal = x * Math.sin(omega) + yInclined * Math.cos(omega);
            double zFinal = z;
            
            // Debug occasionnel
            if (Math.random() < 0.001) {
                System.out.printf("Position calculée: r=%.3f UA, theta=%.1f°%n", 
                                r, Math.toDegrees(theta));
            }
            
            return new double[]{xFinal, yFinal, zFinal}; // Position en UA
        }

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


