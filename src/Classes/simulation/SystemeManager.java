package Classes.simulation;

import Classes.data.AstreData;
import Classes.data.Config;
import Classes.model.Astre;
import javafx.geometry.Point3D;
import javafx.scene.Group;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Gestionnaire centralisé du système solaire.
 *
 * Rôle unique : orchestrer le cycle de vie de tous les corps célestes :
 * <ol>
 *   <li>Instanciation via {@link #init(Group)}</li>
 *   <li>Mise à jour des positions chaque frame via {@link #update(double, boolean)}</li>
 *   <li>Accès rapide par identifiant via {@link #getAstre(AstreData)}</li>
 * </ol>
 */
public class SystemeManager {

    /** Référence à chaque astre instancié, indexé par son identifiant Enum. */
    private final Map<AstreData, Astre> astres = new EnumMap<>(AstreData.class);

    /** Sous-liste : planètes orbitant directement autour du Soleil. */
    private final List<AstreData> planetes    = new ArrayList<>();

    /** Sous-liste : satellites orbitant autour d'une planète parente. */
    private final List<AstreData> satellites  = new ArrayList<>();

    // ===================================================================
    //  INITIALISATION
    // ===================================================================

    /**
     * Instancie et configure tous les corps célestes dans la scène JavaFX.
     * Le Soleil est positionné à l'origine (0, 0, 0) et rendu immédiatement.
     *
     * @param root Groupe racine de la scène 3D à peupler.
     */
    public void init(Group root) {
        for (AstreData data : AstreData.values()) {
            double scale = (data == AstreData.SOLEIL)
                ? Config.SCALE_DIAMETER_SUN
                : Config.SCALE_DIAMETER;

            Astre astre = data.creerAstre(root, scale);
            astres.put(data, astre);

            if (data == AstreData.SOLEIL) {
                astre.position = new Point3D(0, 0, 0);
                astre.renderAstreSansTrajectoire(false, data.texturePath);
            } else if (data.isPlanete()) {
                planetes.add(data);
            } else if (data.isSatellite()) {
                satellites.add(data);
            }
        }
    }

    // ===================================================================
    //  MISE À JOUR PAR FRAME
    // ===================================================================

    /**
     * Met à jour et rend tous les corps célestes.
     *
     * @param time         Temps simulé courant (en années terrestres).
     * @param doTrajectory Activer le tracé des trajectoires orbitales pour les planètes.
     */
    public void update(double time, boolean doTrajectory) {
        Astre soleil = astres.get(AstreData.SOLEIL);

        // Planètes (orbite héliocentriques)
        for (AstreData data : planetes) {
            Astre astre = astres.get(data);
            astre.updatePosition(time, Config.SCALE_DISTANCE, soleil.position);
            astre.renderAstreSansTrajectoire(doTrajectory, data.texturePath);
        }

        // Satellites (orbites planétocentristes
        for (AstreData data : satellites) {
            Astre astre  = astres.get(data);
            Astre parent = astres.get(data.parentData);
            if (parent != null) {
                astre.updatePositionAroundAstre(
                    time, parent.position,
                    Config.SCALE_DISTANCE, data.displayDistanceFactor
                );
                astre.renderAstreSansTrajectoire(false, data.texturePath);
            }
        }
    }

    // ===================================================================
    //  API PUBLIQUE
    // ===================================================================

    /** Retourne l'instance d'un astre par son identifiant {@link AstreData}. */
    public Astre getAstre(AstreData data) {
        return astres.get(data);
    }

    /** Efface les trajectoires accumulées pour toutes les planètes. */
    public void resetAllTrajectories() {
        for (AstreData data : planetes) {
            astres.get(data).resetTrajectory();
        }
    }

    /** Affiche dans la console les positions de tous les corps célestes. */
    public void printPositions() {
        System.out.println("=== POSITIONS DES ASTRES ===");
        for (AstreData data : AstreData.values()) {
            Astre a = astres.get(data);
            if (a != null) System.out.println(a);
        }
        System.out.println("============================");
    }
}
