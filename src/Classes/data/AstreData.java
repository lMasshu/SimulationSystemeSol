package Classes.data;

import Classes.model.Astre;
import javafx.scene.Group;
import javafx.scene.paint.Color;

/**
 * Base de données astronomique du système solaire.
 *
 * Chaque constante représente un corps céleste et encapsule :
 *  - Ses propriétés physiques (masse, diamètre)
 *  - Ses paramètres orbitaux réels (périapside, apoapside, période, inclinaison…)
 *  - Son astre parent ({@code null} pour le Soleil)
 *  - Le chemin de texture PNG
 *  - Un facteur d'affichage optionnel pour agrandir visuellement les orbites de lunes
 *
 * Utiliser {@link #creerAstre(Group, double)} pour instancier l'objet 3D.
 */
public enum AstreData {

    // ===========================================================================
    //  ÉTOILE
    // ===========================================================================
    SOLEIL("Soleil",     1.989e30,  1.3927e6,
           0.0,          0.0,       0.0,
           0.0,          0.0,       0.0,
           Color.YELLOW, "/resources/textures/soleil.png",
           null,         1.0,       609.6), // ~25.4 jours

    // ===========================================================================
    //  PLANÈTES   — orbitent autour du SOLEIL, displayDistanceFactor = 1.0
    // ===========================================================================
    MERCURE("Mercure",  3.6e24,    4879.4,
            0.3075,     0.4667,    0.2408,
            7.005,      48.331,    29.124,
            Color.GRAY, "/resources/textures/mercure.png",
            SOLEIL,     1.0,       1407.6),

    VENUS("Vénus",       4.867e25,  12103.6,
          0.7184,        0.7282,    0.6152,
          3.395,         76.680,    54.884,
          Color.ORANGE,  "/resources/textures/venus.png",
          SOLEIL,        1.0,       -5832.5), // Rétrograde

    TERRE("Terre",       5.972e25,  12756.0,
          0.9833,        1.0167,    1.0000,
          0.000,         0.000,     114.208,
          Color.BLUE,    "/resources/textures/terre.png",
          SOLEIL,        1.0,        23.934),

    MARS("Mars",         6.418e24,  6779.5,
         1.3814,         1.6660,    1.8809,
         1.850,          49.558,    286.502,
         Color.RED,      "/resources/textures/mars.png",
         SOLEIL,         1.0,        24.623),

    JUPITER("Jupiter",  1.898e28,   139820.0,
            4.9504,     5.4549,     11.862,
            1.303,      100.464,    273.867,
            Color.ORANGE, "/resources/textures/jupiter.png",
            SOLEIL,     1.0,         9.925),

    SATURNE("Saturne",  5.683e27,   116464.0,
            9.0481,     10.1159,    29.447,
            2.489,      113.665,    339.392,
            Color.BROWN, "/resources/textures/saturne.png",
            SOLEIL,     1.0,        10.656),

    URANUS("Uranus",    8.681e26,   50724.0,
           18.3755,     20.0747,    84.016,
           0.773,       74.006,     96.998,
           Color.CYAN,  "/resources/textures/uranus.png",
           SOLEIL,      1.0,       -17.24), // Rétrograde

    NEPTUNE("Neptune",  1.024e27,   49244.0,
            29.8203,    30.3271,    164.8,
            1.770,      131.784,    276.336,
            Color.DARKBLUE, "/resources/textures/neptune.png",
            SOLEIL,     1.0,        16.11),

    // ===========================================================================
    //  LUNE DE LA TERRE   — displayDistanceFactor = 10.0 (orbite agrandie)
    // ===========================================================================
    LUNE("Lune",         7.342e22,  3474.8,
         0.002573,       0.002718,  0.074803,
         5.145,          125.044,   318.308,
         Color.LIGHTGRAY, "/resources/textures/lune.png",
         TERRE,          10.0,       655.7), // Verrouillage gravitationnel

    // ===========================================================================
    //  LUNES DE JUPITER   — displayDistanceFactor = 20.0
    // ===========================================================================
    IO("Io",             8.93e22,   3643.0,
       0.002806,         0.002829,  0.004843,
       0.036,            43.977,    84.129,
       Color.YELLOW,    "/resources/textures/io.png",
       JUPITER,          20.0,      42.46),

    EUROPA("Europa",     4.799e22,  3122.0,
           0.004444,     0.004525,  0.009722,
           0.469,        219.106,   88.970,
           Color.LIGHTGRAY, "/resources/textures/europa.png",
           JUPITER,      20.0,      85.23),

    GANYMEDE("Ganymède", 1.4819e23, 5262.0,
             0.007145,   0.007164,  0.019588,
             0.21,       63.552,    192.417,
             Color.GRAY,  "/resources/textures/ganymede.png",
             JUPITER,    20.0,     171.7),

    CALLISTO("Callisto", 1.075938e23, 4820.0,
             0.012490,   0.012677,  0.045692,
             0.192,      298.848,   52.643,
             Color.DARKGRAY, "/resources/textures/callisto.png",
             JUPITER,    20.0,     400.5),

    // ===========================================================================
    //  LUNES DE SATURNE   — displayDistanceFactor = 15.0
    // ===========================================================================
    TITAN("Titan",       1.3452e23, 5150.0,
          0.008168,      0.008168,  0.04365,
          0.3,           169.529,   186.585,
          Color.ORANGE,  "/resources/textures/titan.png",
          SATURNE,       15.0,     382.7),

    MIMAS("Mimas",       3.7493e19, 396.0,
          0.001216,      0.001265,  0.002579,
          1.574,         139.771,   333.877,
          Color.LIGHTGRAY, "/resources/textures/mimas.png",
          SATURNE,       15.0,      22.6),

    ENCELADE("Encelade", 1.080318e20, 504.0,
             0.001584,   0.001599,  0.003751,
             0.019,      169.508,   13.456,
             Color.WHITE, "/resources/textures/encelade.png",
             SATURNE,    15.0,      32.9),

    TETHYS("Téthys",     6.1745e20, 1066.0,
           0.001969,     0.001984,  0.005169,
           1.12,         167.789,   262.334,
           Color.LIGHTGRAY, "/resources/textures/tethys.png",
           SATURNE,      15.0,      45.3),

    DIONE("Dioné",       1.095452e21, 1123.0,
          0.002517,      0.002523,  0.007493,
          0.019,         128.538,   91.796,
          Color.LIGHTGRAY, "/resources/textures/dione.png",
          SATURNE,       15.0,      65.7),

    RHEA("Rhéa",         2.306518e21, 1527.0,
         0.003522,       0.003524,  0.012369,
         0.345,          169.837,   201.164,
         Color.LIGHTGRAY, "/resources/textures/rhea.png",
         SATURNE,        15.0,     108.4),

    IAPETUS("Japet",     1.805635e21, 1469.0,
            0.02313,     0.02447,   0.217194,
            15.47,       81.098,    271.606,
            Color.DARKGRAY, "/resources/textures/iapetus.png",
            SATURNE,     15.0,     1903.0);

    // ===========================================================================
    //  CHAMPS
    // ===========================================================================
    public final String     nom;
    public final double     masse;
    public final double     diametreKm;
    public final double     periapsideUA;
    public final double     apoapsideUA;
    public final double     periodeOrbitaleAnnees;
    public final double     inclinaison;
    public final double     longitudeNoeud;
    public final double     argumentPeriapside;
    public final Color      couleur;
    /** Chemin vers la texture PNG dans les resources. */
    public final String     texturePath;
    /** Corps parent autour duquel orbite cet astre ({@code null} pour le Soleil). */
    public final AstreData  parentData;
    /**
     * Facteur multiplicatif appliqué à {@code SCALE_DISTANCE} pour les satellites.
     * Rend les orbites de lunes visuellement lisibles (ex: lunes de Jupiter : ×20).
     */
    public final double     displayDistanceFactor;
    /** Période de rotation propre (jour sidéral) en heures. Valeur négative pour rotation rétrograde. */
    public final double     periodeRotationHeures;

    AstreData(String nom, double masse, double diametreKm,
              double periapsideUA, double apoapsideUA, double periodeOrbitaleAnnees,
              double inclinaison, double longitudeNoeud, double argumentPeriapside,
              Color couleur, String texturePath, AstreData parentData, double displayDistanceFactor,
              double periodeRotationHeures) {
        this.nom                  = nom;
        this.masse                = masse;
        this.diametreKm           = diametreKm;
        this.periapsideUA         = periapsideUA;
        this.apoapsideUA          = apoapsideUA;
        this.periodeOrbitaleAnnees = periodeOrbitaleAnnees;
        this.inclinaison          = inclinaison;
        this.longitudeNoeud       = longitudeNoeud;
        this.argumentPeriapside   = argumentPeriapside;
        this.couleur              = couleur;
        this.texturePath          = texturePath;
        this.parentData           = parentData;
        this.displayDistanceFactor = displayDistanceFactor;
        this.periodeRotationHeures = periodeRotationHeures;
    }

    // ===========================================================================
    //  MÉTHODES UTILITAIRES
    // ===========================================================================

    /** @return {@code true} si cet astre orbite directement autour du Soleil. */
    public boolean isPlanete() { return parentData == SOLEIL; }

    /** @return {@code true} si cet astre est un satellite d'une planète. */
    public boolean isSatellite() { return parentData != null && parentData != SOLEIL; }

    /**
     * Instancie un objet {@link Astre} 3D prêt à être ajouté à la scène JavaFX.
     * @param root          Groupe JavaFX de la scène.
     * @param scaleDiameter Facteur de conversion km → pixels pour les diamètres.
     */
    public Astre creerAstre(Group root, double scaleDiameter) {
        return new Astre(
            nom, masse, diametreKm / scaleDiameter,
            periapsideUA, apoapsideUA, periodeOrbitaleAnnees,
            inclinaison, longitudeNoeud, argumentPeriapside,
            periodeRotationHeures,
            root, couleur
        );
    }
}
