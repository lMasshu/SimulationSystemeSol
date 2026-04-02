package Classes.data;

/**
 * Constantes globales immuables de la simulation.
 * Centralise les paramètres de l'application pour supprimer les "magic numbers".
 */
public final class Config {

    private Config() {} // Non instanciable

    // --- Fenêtre ---
    public static final int WIDTH  = 1920;
    public static final int HEIGHT = 1080;

    // --- Échelles spatiales ---
    /** 1 UA (Unité Astronomique) = 150 pixels. */
    public static final double SCALE_DISTANCE    = 150.0;

    /** 1 pixel = 10 000 km pour les diamètres d'astres. */
    public static final double SCALE_DIAMETER    = 1e4;

    /** Échelle de diamètre spéciale pour le Soleil (évite de bloquer l'écran). */
    public static final double SCALE_DIAMETER_SUN = 1e5;

    // --- Vitesse de simulation ---
    public static final double DEFAULT_TIME_SPEED = 500.0;
    public static final double MIN_SPEED          = 1.0;
    public static final double MAX_SPEED          = 50000.0;
    public static final double SPEED_MULTIPLIER   = 1.5;

    // --- Trajectoires ---
    /** Opacité des trajectoires orbitales (base). */
    public static final double ORBIT_MIN_OPACITY      = 0.10;
    public static final double ORBIT_MAX_OPACITY      = 0.60;
    
    /** Épaisseur (rayon) du trait de trajectoire (base). */
    public static final double ORBIT_MIN_RADIUS       = 0.005;
    public static final double ORBIT_MAX_RADIUS       = 15.0;

    /** Distance de référence (UA) pour le calcul du facteur d'échelle dynamique. */
    public static final double ORBIT_REFERENCE_DIST  = 3000.0;
}
