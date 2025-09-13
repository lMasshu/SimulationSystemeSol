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

###  Contrôles interactifs
- **Navigation 3D** : rotation avec clic droit + glisser
- **Zoom** : molette de souris pour s'approcher/s'éloigner
- **Déplacement** : touches ZQSD pour naviguer
- **Élévation** : Espace (monter) / Ctrl (descendre)
- **Suivi de planètes** : touches 1-8 pour se positionner derrière chaque planète
- **Reset caméra** : touche R pour revenir à la vue d'ensemble
- **Trajectoires** : touche T pour afficher les traces orbitales

###  Rendu visuel
- **Proportions réalistes** (avec facteurs d'échelle adaptés)
- **Trajectoires dynamiques** semi-transparentes
- **Interface 3D fluide** avec 60 FPS

## Architecture technique

### Structure du code

```
src/
├── App.java                 # Classe principale avec interface et contrôles
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

#### 3. **Interface 3D interactive** 
Développement d'un système de caméra sophistiqué :
- **Navigation libre** dans l'espace 3D
- **Suivi automatique** des planètes avec positionnement optimal
- **Contrôles intuitifs** inspirés des logiciels de modélisation 3D

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
javac -cp "path/to/javafx/lib/*" App.java
java -cp ".:path/to/javafx/lib/*" --module-path path/to/javafx/lib --add-modules javafx.controls,javafx.fxml App
```

### Contrôles
- **Clic droit + glisser** : Rotation de la caméra
- **Molette** : Zoom avant/arrière
- **Z/S** : Avancer/reculer
- **Q/D** : Gauche/droite
- **Espace/Ctrl** : Haut/bas
- **1-8** : Suivre les planètes (1=Mercure, 8=Neptune)
- **R** : Reset caméra
- **T** : Afficher les trajectoires

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

- Ajout des lunes principales (Luna, Titan, Europa...)
- Intégration d'astéroïdes et comètes
- Textures photoréalistes des planètes
- Interface utilisateur avec données orbitales en temps réel
- Mode "poursuite" avec caméra attachée aux planètes
- Contrôles temporels (pause, vitesse variable, date précise)

---

*Ce projet démontre l'application pratique de la mécanique céleste et des techniques de programmation 3D pour créer un outil éducatif interactif.*