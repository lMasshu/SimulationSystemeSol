# Simulateur du Système Solaire 3D 

## Description du projet

Ce projet est un **simulateur 3D interactif du système solaire** développé en Java avec JavaFX. Il reproduit fidèlement les mouvements orbitaux des 8 planètes autour du Soleil en utilisant les véritables paramètres astronomiques et les lois de Kepler.

### Objectifs du projet
-  Visualiser en temps réel les orbites elliptiques des planètes
-  Appliquer les lois de Kepler et les paramètres orbitaux réels
-  Offrir une navigation 3D intuitive dans l'espace
-  Comprendre les échelles et proportions du système solaire
-  Créer un outil pédagogique pour l'astronomie

## Fonctionnalités principales

###  Simulation physique réaliste
- **Orbites elliptiques** avec périhélie et aphélie corrects
- **Inclinaisons orbitales** par rapport au plan de l'écliptique
- **Périodes orbitales** proportionnelles aux véritables durées
- **Résolution de l'équation de Kepler** pour des positions précises

###  Contrôles interactifs (Caméra Simulateur Spatial)
- **Navigation Vol Libre** :
  - Déplacement inertiel (ZQSD)
  - Panning (Clic gauche ou milieu + glisser)
  - Orientation / Tourner la tête (Clic droit + glisser)
  - Altitude (Espace pour monter, Ctrl pour descendre)
  - Vitesse x10 (Maintenir Maj/Shift)
- **Mode Orbite & Focus** :
  - **Clic GAUCHE sur une planète** pour s'y arrimer automatiquement (transition fluide).
  - Touches **1-8** ou **clic** pour verrouiller une cible.
  - **Molette** en orbite pour s'approcher précisément de la surface ou s'éloigner.
  - Appuyer sur une touche de mouvement (ZQSD) pour quitter l'orbite et repartir dans le vide.
- **Réglage du Temps** :
  - `9` et `0` pour ralentir/accélérer le temps de la simulation.
  - `Entrée` pour mettre en pause.
- **Autres** :
  - **R** : Reset caméra
  - **T/G** : Afficher ou cacher les trajectoires

###  Rendu visuel
- **Proportions réalistes** (avec facteurs d'échelle adaptés)
- **Trajectoires dynamiques** semi-transparentes
- **Interface 3D fluide** avec 60 FPS

## Architecture technique

### Structure du code

```
src/
├── SimulationSystemeSolaire.java                 # Classe principale avec interface et contrôles
└── Classes/
    ├── Astre.java          # Représentation d'un corps céleste
    └── OrbitePlanete.java  # Calculs orbitaux et lois de Kepler
```

### Approche de développement

#### 1. **Modélisation physique** 
J'ai commencé par implémenter les **lois de Kepler** dans la classe `OrbitePlanete` :
- Calcul de l'**anomalie moyenne** basée sur le temps
- Résolution de l'**équation de Kepler** par méthode itérative
- Conversion en **coordonnées cartésiennes 3D**
- Application des **transformations orbitales** (inclinaison, nœud ascendant)

#### 2. **Données astronomiques réelles** 
Intégration des paramètres orbitaux officiels pour chaque planète :
- Périhélie et aphélie (distances minimale/maximale au Soleil)
- Période orbitale (durée d'une révolution)
- Inclinaison orbitale (angle par rapport à l'écliptique)
- Longitude du nœud ascendant et argument du périhélie

#### 3. **Interface 3D et Moteur de Caméra Spatiale** 
Développement d'un contrôleur de caméra (CameraController) de type "Simulateur Spatial" :
- **Navigation Libre (Free-Fly)** avec inertie, accélération et glissement spatial fluide.
- **Mouse Picking 3D** permettant de cliquer sur les planètes pour déclencher des actions orbitalles.
- **Mode Focus Orbital** avec des transitions mathématiques (Ease-in-out) pour arrimer la caméra à des astres en mouvement rapide.

#### 4. **Optimisation et rendu** 
- **Animation fluide** avec `AnimationTimer`
- **Gestion mémoire** efficace des objets 3D
- **Mise à jour sélective** des éléments visuels
- **Rendu conditionnel** des trajectoires

## Paramètres physiques utilisés

### Échelles de conversion
- **Distance** : 1 UA = 150 pixels
- **Diamètres** : 1 pixel = 10 000 km
- **Temps** : Facteur d'accélération × 500

### Données orbitales (exemples)
| Planète | Période (années) | Périhélie (UA) | Aphélie (UA) | Inclinaison (°) |
|---------|------------------|----------------|--------------|-----------------|
| Mercure | 0.24            | 0.31           | 0.47         | 7.0            |
| Terre   | 1.00            | 0.98           | 1.02         | 0.0            |
| Mars    | 1.88            | 1.38           | 1.67         | 1.9            |
| Jupiter | 11.86           | 4.95           | 5.45         | 1.3            |

## Installation et utilisation

### Prérequis
- Java 11 ou supérieur
- JavaFX SDK
- IDE compatible (Eclipse, IntelliJ, VS Code)

### Lancement
```bash
javac -cp "path/to/javafx/lib/*" SimulationSystemeSolaire.java
java -cp ".:path/to/javafx/lib/*" --module-path path/to/javafx/lib --add-modules javafx.controls,javafx.fxml SimulationSystemeSolaire
```

### Contrôles

**Mode Vol Libre (Free Fly) :**
- **Z / S** : Avancer / Reculer
- **Q / D** : Gauche / Droite
- **Espace / Ctrl** : Monter / Descendre (absolu)
- **Clic Droit + Glisser** : Tourner la tête librement
- **Clic Gauche / Milieu + Glisser** : Panning (déplacement latéral)
- **Molette** : Vitesse propulsive dans l'axe du regard
- **Shift (Maintenu)** : Multiplicateur x10 de vitesse de navigation

**Mode Focus (Orbite) :**
- **Clic Gauche sur une planète** : Verrouillage et focus sur l'Astre (Mouse Picking)
- **Touches 1 à 8** : S'arrimer directement sur une planète (1=Mercure, 8=Neptune)
- **Clic Droit + Glisser** : Tourner autour de la planète en orbite
- **Molette** : Changer le rayon de l'orbite (zoom précis)
- *Appuyer sur n'importe quelle touche (Z,Q,S,D...) pour briser l'orbite et repasser en vol libre.*

**Simulation :**
- **Entrée** : Pause / Lecture
- **9 / 0** : Ralentir / Accélérer l'écoulement du temps
- **R** : Reset de la caméra (retour à l'origine)
- **T / G** : Afficher / Cacher les trajectoires orbitales

## Défis techniques relevés

### **Précision des calculs orbitaux**
- Implémentation robuste de la résolution de l'équation de Kepler
- Gestion des cas limites (orbites circulaires, inclinaisons nulles)
- Optimisation des calculs trigonométriques

### **Navigation 3D fluide**
- Système de caméra PerspectiveCamera avec transformations composées
- Évitement du "gimbal lock" dans les rotations
- Calculs vectoriels pour les déplacements relatifs

### **Performance en temps réel**
- Optimisation de la boucle d'animation
- Rendu conditionnel des trajectoires
- Gestion efficace des objets JavaFX

### **Échelles et proportions**
- Adaptation des tailles pour une visualisation cohérente
- Facteurs d'échelle différenciés (distances vs diamètres)
- Préservation des rapports astronomiques essentiels

## Améliorations futures possibles

- Intégration d'astéroïdes et comètes, incluant la ceinture de Kuiper.
- Interface utilisateur (UI) avec Overlay des planètes et données orbitales en temps réel.
- Implémentation d'un système d'ombrage (Shadow rendering) dynamique par le Soleil.

---

*Ce projet démontre l'application pratique de la mécanique céleste et des techniques de programmation 3D pour créer un outil éducatif interactif.*