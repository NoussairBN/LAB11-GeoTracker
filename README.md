# GeoTracker — Android Location & Server Sync App

<div align="center">

![GeoTracker Banner](https://img.shields.io/badge/GeoTracker-System%20Dashboard-00E5FF?style=for-the-badge&logo=android&logoColor=white)
![Language](https://img.shields.io/badge/Language-Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Backend](https://img.shields.io/badge/Backend-PHP%20%2B%20MySQL-777BB4?style=for-the-badge&logo=php&logoColor=white)
![Network](https://img.shields.io/badge/Network-Volley%201.2.1-48B983?style=for-the-badge)
![SDK](https://img.shields.io/badge/Min%20SDK-API%2024-3DDC84?style=for-the-badge&logo=android)
![Status](https://img.shields.io/badge/Status-Fonctionnel%20✅-00E5FF?style=for-the-badge)

**Application Android de géolocalisation connectée à un backend distant**
*TP 11 — Localisation d'un Smartphone | Bouanani Noussair*

</div>

---

## Table des matières

- [Présentation](#-présentation)
- [Fonctionnalités démontrées](#-fonctionnalités-démontrées)
- [Architecture du projet](#-architecture-du-projet)
- [Technologies utilisées](#-technologies-utilisées)
- [Installation Backend](#-installation-backend)
- [Installation Android](#-installation-android)
- [Structure du projet](#-structure-du-projet)
- [API Reference](#-api-reference)
- [Tests réalisés](#-tests-réalisés)
- [Démonstration](#-démonstration)
- [Concepts techniques](#-concepts-techniques)
- [Questions de compréhension](#-questions-de-compréhension)
- [Auteur](#-auteur)

---

## Présentation

**GeoTracker** est une application Android développée dans le cadre du TP 11 du cours de *Programmation Mobile avec Java*. Elle démontre la communication complète entre un appareil Android et un backend distant pour la géolocalisation en temps réel :

```
┌─────────────────────┐        HTTP POST / JSON       ┌─────────────────────┐
│   Android (Java)    │ ─────────────────────────────► │   PHP + MySQL       │
│                     │          Volley 1.2.1          │                     │
│  • LocationManager  │                                │  • createPosition   │
│  • GPS Provider     │   latitude, longitude,         │  • PositionService  │
│  • Permissions GPS  │   date_position, imei          │  • table position   │
└─────────────────────┘                                └─────────────────────┘
```

---

## Fonctionnalités démontrées

| # | Fonctionnalité | Résultat |
|---|---|---|
| 1 | **Détection GPS automatique** | Signal détecté → coordonnées affichées en temps réel |
| 2 | **Envoi automatique** | Chaque nouvelle position envoyée automatiquement au serveur |
| 3 | **Envoi manuel** | Bouton pour forcer l'envoi immédiat |
| 4 | **Statut temps réel** | Point vert / orange / rouge selon l'état du GPS |
| 5 | **Confirmation serveur** | Réponse JSON du PHP affichée dans l'interface |

---

## Architecture du projet

```
LAB11-GeoTracker/
│
├── app/                              ← Application Android (Java)
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/.../MainActivity.java
│       └── res/  (layout, drawable, values)
│
├── php-server/localisation/         ← Backend PHP (XAMPP)
│   ├── createPosition.php
│   ├── init_db.sql
│   ├── classe/Position.php
│   ├── connexion/Connexion.php
│   ├── dao/IDao.php
│   └── service/PositionService.php
│
└── docs/media/
    ├── demo_screenshot.png
    └── demo_video.mp4
```

---

## Technologies utilisées

### Côté Android

| Technologie | Version | Rôle |
|---|---|---|
| Java | 11 | Langage principal |
| Android SDK | min 24 / target 36 | API Android |
| Volley | 1.2.1 | Requêtes HTTP POST asynchrones |
| LocationManager | natif Android | Accès au GPS système |
| TelephonyManager | natif Android | Récupération de l'IMEI |
| ActivityResultLauncher | natif Android | Gestion moderne des permissions |
| Material Design 3 | 1.14.0 | Composants UI |

### Côté Backend

| Technologie | Rôle |
|---|---|
| PHP 7.4+ | Langage serveur |
| PDO | Accès base de données sécurisé |
| MySQL / MariaDB | Stockage des positions GPS |
| JSON | Format d'échange de données |
| XAMPP | Serveur local (Apache + MySQL) |

---

## Installation Backend

**1. Déployer le backend**
```
php-server/localisation/  →  C:\xampp\htdocs\localisation\
```

**2. Initialiser la base de données**
```
phpMyAdmin → SQL → importer init_db.sql
```

**3. Vérifier l'endpoint**
```
GET http://192.168.100.246/localisation/createPosition.php
→ {"status":"error","message":"Méthode non autorisée."}  ✅ Normal
```

---

## Installation Android

**1. Ouvrir le projet**
```
Android Studio → File → Open → LAB11-GeoTracker/
```

**2. Configurer l'IP dans `MainActivity.java` ligne 47**
```
Appareil physique  → http://192.168.100.246/localisation/createPosition.php
Émulateur          → http://10.0.2.2/localisation/createPosition.php
```

**3. Run ▶** sur l'appareil cible — accepter les permissions GPS.

---

## API Reference

### `POST /localisation/createPosition.php`

**Paramètres attendus (x-www-form-urlencoded)**

| Paramètre | Type | Exemple |
|---|---|---|
| `latitude` | float | `48.8566` |
| `longitude` | float | `2.3522` |
| `date_position` | datetime | `2025-09-01 08:00:00` |
| `imei` | string | `352099001761481` |

**Réponses possibles**

| Code | Signification |
|---|---|
| `201 Created` | Position enregistrée avec succès |
| `400 Bad Request` | Paramètres manquants ou invalides |
| `405 Method Not Allowed` | Méthode GET utilisée au lieu de POST |
| `500 Internal Server Error` | Erreur d'insertion MySQL |

### Structure de la table `position`

| Colonne | Type | Description |
|---|---|---|
| `id` | INT AUTO_INCREMENT | Clé primaire |
| `latitude` | DOUBLE | Latitude GPS |
| `longitude` | DOUBLE | Longitude GPS |
| `date_position` | DATETIME | Horodatage de la mesure |
| `imei` | VARCHAR(50) | Identifiant unique du terminal |
| `created_at` | TIMESTAMP | Date d'insertion en BDD |

---

## Tests réalisés

### ① Vérification de l'endpoint (navigateur)

Accès direct en GET à l'URL du script PHP → retourne `Méthode non autorisée`.
Cela confirme que le fichier PHP est bien déployé et accessible sur le réseau.

### ② Test Postman — envoi HTTP POST

Simulation d'une requête Android via Postman avec les 4 paramètres GPS.

**Requête envoyée**
```
POST http://192.168.100.246/localisation/createPosition.php
Content-Type: x-www-form-urlencoded

latitude      = 48.8566
longitude     = 2.3522
date_position = 2025-09-01 08:00:00
imei          = TEST-001
```

**Réponse reçue — HTTP 201**
```json
{
  "status": "success",
  "message": "Position enregistrée.",
  "data": {
    "latitude": 48.8566,
    "longitude": 2.3522,
    "date_position": "2025-09-01 08:00:00",
    "imei": "TEST-001"
  }
}
```

### ③ Vérification phpMyAdmin

Après le test Postman, vérification directe en SQL que la ligne est bien insérée dans la table `position` avec les bonnes valeurs.

### ④ Test depuis l'application Android (émulateur)

L'émulateur **MobSF AVD API 30** a été utilisé avec une position GPS simulée via les **Extended Controls** d'Android Studio (coordonnées de Mountain View, CA — position par défaut de l'émulateur Google).

**Résultats observés :**

| Champ | Valeur |
|---|---|
| Latitude | `37.421998 °` |
| Longitude | `-122.084000 °` |
| Altitude | `5.0 m` |
| Précision | `± 5 m` |
| Provider | `GPS` |
| Horodatage | `2026-06-06 13:23:14` |
| Statut serveur | `✅ Position enregistrée.` |

La ligne a bien été insérée en base de données avec `imei = unknown` (comportement normal sur émulateur).

---

## Démonstration

### Capture d'écran — Application en fonctionnement

![GeoTracker Screenshot](docs/media/demo_screenshot.png)

---

### Vidéo de démonstration

<video src="docs/media/demo_video.mp4" controls width="100%">
  Votre navigateur ne supporte pas la lecture vidéo.
</video>

---

## Concepts techniques

### LocationManager et providers GPS
Android expose le matériel GPS via un service système. L'application choisit entre **GPS_PROVIDER** (précision maximale, consommateur de batterie) et **NETWORK_PROVIDER** (plus rapide, moins précis). La fréquence de mise à jour est contrôlée par un intervalle de temps minimum et un déplacement minimum en mètres.

### Gestion des permissions runtime
Depuis Android 6.0 (API 23), les permissions sensibles comme `ACCESS_FINE_LOCATION` doivent être demandées à l'exécution. L'API moderne `ActivityResultLauncher` remplace l'ancienne méthode `onRequestPermissionsResult()` pour gérer la réponse de l'utilisateur.

### Volley — communication asynchrone
Volley est une bibliothèque Android qui gère les requêtes réseau en arrière-plan et retourne les résultats sur le thread principal. Android interdit les appels réseau sur le thread UI — Volley gère cette contrainte automatiquement via une file d'attente interne.

### Architecture PHP en couches
Le backend suit une architecture en couches : **classe métier** (Position), **interface DAO** (IDao), **service** (PositionService), **connexion** (Connexion PDO) et **script d'entrée** (createPosition.php). Cette séparation facilite la maintenance et les tests.

### Sécurité — requêtes préparées PDO
Les paramètres reçus par POST ne sont jamais concaténés directement dans les requêtes SQL. PDO utilise des paramètres liés (`:latitude`, `:longitude`...) qui empêchent les injections SQL.

### usesCleartextTraffic
Le Manifest déclare `android:usesCleartextTraffic="true"` pour autoriser les connexions HTTP non chiffrées vers le serveur XAMPP local. En environnement de production, cette option doit être retirée au profit de HTTPS.

---

## ❓ Questions de compréhension

| # | Question |
|---|---|
| 1 | Quel est le rôle du `LocationManager` dans Android ? |
| 2 | Quelle différence entre `GPS_PROVIDER` et `NETWORK_PROVIDER` ? |
| 3 | Pourquoi faut-il demander `ACCESS_FINE_LOCATION` à l'exécution ? |
| 4 | Quel est le rôle de `minTime` et `minDistance` dans `requestLocationUpdates()` ? |
| 5 | Pourquoi utiliser Volley plutôt qu'un `HttpURLConnection` direct ? |
| 6 | Quel est le rôle de `usesCleartextTraffic` dans le Manifest ? |
| 7 | Pourquoi stocker `latitude` et `longitude` en `DOUBLE` et non `FLOAT` ? |
| 8 | Quel est l'intérêt des requêtes préparées PDO côté PHP ? |
| 9 | Pourquoi l'IMEI est-il `unknown` sur un émulateur Android ? |
| 10 | Comment fonctionne `onLocationChanged()` ? Sur quel thread est-il appelé ? |

---

## Extensions possibles

- [ ] Affichage sur carte (Google Maps API ou OpenStreetMap / Leaflet)
- [ ] Historique des positions dans l'app
- [ ] Mode suivi continu (Foreground Service + notification)
- [ ] Geofencing — alerte si sortie d'une zone
- [ ] Export CSV des positions
- [ ] Authentification du terminal (token JWT)
- [ ] Tableau de bord web admin (PHP + Leaflet.js)

---

## 👤 Auteur

**Bouanani Noussair**
*Cours de Programmation Mobile — Android avec Java*
*TP 11 — Localisation d'un Smartphone avec GPS et envoi vers un serveur distant*

---

<div align="center">

![Made with Java](https://img.shields.io/badge/Made%20with-Java-ED8B00?style=flat-square&logo=java)
![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android)
![PHP](https://img.shields.io/badge/Backend-PHP%20%2B%20MySQL-777BB4?style=flat-square&logo=php)
![GPS](https://img.shields.io/badge/GPS-LocationManager-00E5FF?style=flat-square)

*Bouanani Noussair — 2026*

</div>
