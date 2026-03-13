![Logo taf](./logo_taf.png)

# Test Automation Framework — Module TestAPI (Team 2)

TAF est un cadriciel d'automatisation de tests développé à l'ÉTS. Il permet d'exécuter des **tests d'API** (Rest-Assured), des **tests de performance** (Gatling) et des **tests d'interface** (Selenium) à travers une interface web Angular unique, sans écrire de code.

L'utilisateur construit ses cas de test depuis l'interface, les lance en un clic, et visualise les résultats (pass/fail) directement dans le tableau.

## Table des matières

- [Architecture](#architecture)
- [Lancer le projet](#lancer-le-projet)
- [Tests d'API (Rest-Assured)](#tests-dapi-rest-assured) ← fonctionnalité principale
- [Autres modules](#autres-modules-gatling-selenium)
- [Authentification](#authentification)
- [Documentation API (Swagger UI)](#documentation-api-swagger-ui)
- [Variables d'environnement](#variables-denvironnement)
- [Technologies](#technologies)
- [Contribuer](#contribuer)

## Architecture

Le module se compose de 4 microservices orchestrés par Docker Compose :

| Service | Rôle | Port hôte → interne |
|---------|------|---------------------|
| **frontend** | Interface Angular — construction et visualisation des tests | 4300 → 4200 |
| **backend** | API Spring Boot — authentification, routage vers les microservices | 8084 → 8080 |
| **testapi** | Exécution des tests d'API via **Rest-Assured** | 8086 → 8082 |
| **selenium** | Exécution des tests d'interface via **Selenium WebDriver** | 4445 → 4444 |

Les services s'enregistrent auprès d'**Eureka** (registry, port 8761) pour la découverte de services.

### Flux d'un test d'API

```
Frontend Angular        Backend (8084)              TestAPI (8086)            API cible
┌──────────────┐      ┌────────────────────┐      ┌──────────────────┐      ┌──────────┐
│ Tableau de   │─POST→│ TestApiController   │─POST→│ RequestController│─HTTP→│ ex:      │
│ tests        │      │ /api/testapi/       │      │ (Rest-Assured)   │      │ jsonplace│
│              │←JSON─│ checkApi            │←JSON─│                  │←─────│ holder   │
└──────────────┘      └────────────────────┘      └──────────────────┘      └──────────┘
```

Le backend agit comme un proxy transparent : il sérialise la requête JSON et la transmet au microservice testapi. C'est le microservice testapi qui effectue l'appel HTTP réel via Rest-Assured et exécute les assertions.

## Lancer le projet

### Avec Docker (recommandé)

**Prérequis :** Docker et Docker Compose installés.

```powershell
# Démarrage rapide — Team 2 uniquement
.\start-taf-local.ps1 -Mode team2 -Build

# Ou manuellement
docker compose -f docker-compose-local-test.yml up -d --build
```

### Sans Docker (développement local)

```bash
# Backend (port 8080)
cd backend && mvn clean install && mvn spring-boot:run

# TestAPI (port 8082)
cd testapi && mvn clean install && mvn spring-boot:run

# Frontend (port 4200)
cd frontend && npm install && ng serve --open
```

> Les variables d'environnement dans `backend/.env` doivent être configurées (MongoDB, JWT, Eureka, etc.).

### Vérification après démarrage

| Élément | URL | Résultat attendu |
|---------|-----|-------------------|
| Frontend | http://localhost:4300 | Page de connexion |
| Swagger UI | http://localhost:8084/swagger-ui.html | Interface API |
| Eureka | http://localhost:8761 | TEAM2 enregistré |
| Health check | http://localhost:8084/actuator/health | `{"status":"UP"}` |

---

## Tests d'API (Rest-Assured)

C'est la **fonctionnalité principale** du module TestAPI. Elle permet de tester n'importe quelle API REST en spécifiant la requête attendue et les critères de validation.

### Utilisation via l'interface Angular

1. **Se connecter** (JWT ou Google OAuth2)
2. Naviguer vers la page **Tests API**
3. Cliquer sur le bouton **+** pour ajouter un test
4. Remplir le formulaire :
   - **Méthode** — `GET`, `POST`, `PUT`, `DELETE` (dropdown)
   - **URL de l'API** — l'URL complète de l'API à tester
   - **Statut attendu** — le code HTTP attendu (ex: `200`, `404`)
   - **Headers** — paires clé/valeur à envoyer avec la requête (optionnel)
   - **Sortie attendue** — le corps JSON attendu dans la réponse (optionnel)
   - **En-têtes attendus** — les en-têtes attendus dans la réponse (optionnel)
   - **Temps de réponse** — temps maximum acceptable en ms (optionnel)
5. Cliquer **« Lancer les tests »** — tous les tests du tableau s'exécutent en parallèle
6. La colonne **Résultat** affiche **vert** (pass) ou **rouge** (fail) avec un tooltip détaillant les erreurs

**Fonctions supplémentaires :**

- **Export CSV** — télécharge les définitions de tests au format CSV
- **Import CSV** — charge des tests depuis un fichier CSV
- **Supprimer** — supprime un test individuel du tableau

> **Note :** Les résultats sont conservés en mémoire côté frontend uniquement. Un rafraîchissement de la page réinitialise les résultats (mais pas les définitions de tests).

### Requête API — format JSON

Endpoint : `POST /api/testapi/checkApi` (backend, port 8084)

```json
{
  "method": "GET",
  "apiUrl": "https://jsonplaceholder.typicode.com/posts/1",
  "statusCode": 200,
  "input": "",
  "expectedOutput": "{\"userId\": 1, \"id\": 1}",
  "headers": {
    "Accept": "application/json"
  }
}
```

| Champ | Type | Requis | Description |
|-------|------|--------|-------------|
| `method` | `String` | **Oui** | Méthode HTTP : `GET`, `POST`, `PUT`, `DELETE`, `PATCH`, `HEAD`, `OPTIONS` |
| `apiUrl` | `String` | **Oui** | URL complète de l'API cible à tester |
| `statusCode` | `int` | Non | Code HTTP attendu (ex: `200`, `201`, `404`) |
| `input` | `String` | Non | Corps de la requête à envoyer (pour POST/PUT) |
| `expectedOutput` | `String` | Non | Corps JSON attendu dans la réponse |
| `headers` | `Map<String, String>` | Non | En-têtes HTTP à inclure dans la requête |

> Le microservice testapi accepte aussi `expectedHeaders` (en-têtes de réponse attendus) et `responseTime` (temps max en ms), mais ces champs ne sont pas exposés par le backend proxy actuellement.

### Validations exécutées (assertions)

Le microservice testapi exécute **3 assertions actives** via Rest-Assured sur chaque test :

| Assertion | Logique | Message d'erreur si échec |
|-----------|---------|---------------------------|
| **Code de statut** | `code attendu == code reçu` | `❌ Code de statut attendu: 200, reçu: 404` |
| **Corps de la réponse** | Comparaison JSON récursive champ par champ via `JsonComparator` | Retourne `fieldAnswer` avec `true`/`false` par champ |
| **En-têtes de réponse** | Chaque paire clé/valeur de `expectedHeaders` doit exister dans la réponse | `❌ En-tête attendu manquant: Content-Type` |

La validation du **temps de réponse** est implémentée dans le code mais actuellement désactivée (commentée).

**Comparaison JSON (JsonComparator) :** La comparaison du corps de réponse est récursive — elle parcourt l'arbre JSON attendu vs reçu et retourne un objet indiquant `true`/`false` pour chaque champ, y compris les objets et tableaux imbriqués.

### Réponse API — format JSON

```json
{
  "answer": true,
  "statusCode": 200,
  "output": "{\n  \"userId\": 1,\n  \"id\": 1,\n  \"title\": \"...\"\n}",
  "fieldAnswer": {
    "userId": true,
    "id": true
  },
  "messages": []
}
```

| Champ | Type | Description |
|-------|------|-------------|
| `answer` | `boolean` | Résultat global : `true` si toutes les assertions passent |
| `statusCode` | `int` | Code HTTP réellement reçu de l'API cible |
| `output` | `String` | Corps de la réponse reçue (formaté en JSON lisible) |
| `fieldAnswer` | `JsonNode` | Résultat champ par champ : `{"field1": true, "field2": false}` |
| `messages` | `List<String>` | Messages d'erreur détaillés avec préfixe `❌` |

**En cas d'erreur de connexion** (API cible injoignable) :

```json
{
  "answer": false,
  "statusCode": -1,
  "output": null,
  "fieldAnswer": null,
  "messages": ["❌ Impossible de joindre l'API cible à https://..."]
}
```

### Exemple complet avec curl

```bash
# 1. Se connecter pour obtenir un token JWT
TOKEN=$(curl -s -X POST http://localhost:8084/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{"username":"user1","password":"password123"}' | jq -r '.accessToken')

# 2. Lancer un test d'API
curl -X POST http://localhost:8084/api/testapi/checkApi \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "method": "GET",
    "apiUrl": "https://jsonplaceholder.typicode.com/posts/1",
    "statusCode": 200,
    "expectedOutput": "{\"userId\": 1, \"id\": 1}"
  }'
```

---

## Autres modules (Gatling, Selenium)

Le backend intègre également des endpoints pour les **tests de performance** (Gatling, `POST /api/gatling/runSimulation`) et les **tests d'interface** (Selenium, `POST /api/testselenium`). Ces modules sont développés et maintenus par d'autres équipes (Projet #4 et Projet #2 respectivement). Consultez leur documentation pour plus de détails.

---

## Authentification

Tous les endpoints de test (`/api/testapi/**`, etc.) nécessitent un token JWT.

### JWT (classique)

```bash
# Inscription
curl -X POST http://localhost:8084/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"fullName":"user1","username":"user1","email":"user1@example.com","role":["user"],"password":"password123"}'

# Connexion → retourne un accessToken
curl -X POST http://localhost:8084/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{"username":"user1","password":"password123"}'
```

Utilisez le token dans l'en-tête : `Authorization: Bearer <accessToken>`

### Google OAuth2

Connexion via compte Google disponible sur la page de login du frontend. Nécessite les variables d'environnement `GOOGLE_CLIENT_ID` et `GOOGLE_CLIENT_SECRET` configurées avant le démarrage Docker :

```powershell
$env:GOOGLE_CLIENT_ID = "votre-client-id.apps.googleusercontent.com"
$env:GOOGLE_CLIENT_SECRET = "votre-client-secret"
docker compose -f docker-compose-local-test.yml up -d --build backend-team2
```

> Sans ces variables, le login Google ne fonctionne pas, mais le reste de l'application (JWT, tests d'API, Gatling, Selenium) fonctionne normalement.

### Endpoints publics

`POST /api/auth/signup`, `POST /api/auth/signin`, `GET /api/oauth2/login-url`, `/swagger-ui/**`, `/api-docs/**`, `/actuator/**`

---

## Documentation API (Swagger UI)

Accessible à : [http://localhost:8084/swagger-ui.html](http://localhost:8084/swagger-ui.html)

1. Connectez-vous via `POST /api/auth/signin` — copiez le `accessToken`
2. Cliquez sur **Authorize** (🔒) → entrez `Bearer <token>` → validez
3. Tous les endpoints sont accessibles avec des exemples pré-remplis

---

## Variables d'environnement

| Variable | Description | Valeur par défaut |
|----------|-------------|-------------------|
| `DB_URI` | URI de connexion MongoDB | — |
| `DB_NAME` | Nom de la base de données | — |
| `DB_AUTH` | Base d'authentification MongoDB | `admin` |
| `JWT_SECRET` | Clé secrète pour la signature JWT (HS512) | — |
| `JWT_EXPIRES` | Durée de validité du token JWT (ms) | `86400000` (24h) |
| `GOOGLE_CLIENT_ID` | ID client Google OAuth2 | `placeholder` |
| `GOOGLE_CLIENT_SECRET` | Secret client Google OAuth2 | `placeholder` |
| `OAUTH2_FRONTEND_REDIRECT_URL` | URL de redirection frontend après OAuth2 | `http://localhost:4200` |
| `EUREKA_HOST` | Hôte du serveur Eureka | — |
| `EUREKA_PORT` | Port du serveur Eureka | `8761` |
| `TEST_API_SERVICE_URL` | URL du microservice TestAPI | — |
| `TEST_API_SERVICE_PORT` | Port du microservice TestAPI | `8082` |
| `SELENIUM_URL` | URL du microservice Selenium | — |
| `SELENIUM_PORT` | Port du microservice Selenium | `4444` |

## Technologies

| Composant | Version | Rôle |
|-----------|---------|------|
| Java | 17 | Langage backend |
| Spring Boot | 3.5.10 | Framework backend |
| **Rest-Assured** | **5.5.0** | **Exécution des tests d'API** |
| Angular | 14 | Framework frontend |
| MongoDB | latest | Stockage utilisateurs/rôles |
| Gatling | — | Tests de performance |
| Selenium Java | 4.27.0 | Tests d'interface |
| Maven | 3.9+ | Build backend |
| jjwt | 0.9.1 | Tokens JWT |
| springdoc-openapi | 2.6.0 | Swagger UI |

## Tests unitaires

Le backend dispose d'une suite de **61 tests unitaires** avec couverture JaCoCo.

```powershell
# One-click : depuis la racine du projet
.\run-tests-testapi.ps1

# Ou manuellement
cd testapi-Service
mvn test -pl backend -am
```

| Métrique | Valeur |
|---|---|
| Tests totaux | 61 |
| Réussis | 60 |
| Ignorés | 1 (contextLoads — nécessite MongoDB) |
| Couverture instructions | 61% |
| Modules couverts | Sécurité (JWT, OAuth2, filtres), contrôleurs, entités, DTOs |

Le rapport de couverture est dans `backend/target/site/jacoco/index.html`.  
Le détail complet des tests est dans [TEST-REPORT.md](./TEST-REPORT.md).

## Contribuer

La démarche pour contribuer est disponible dans [CONTRIBUTING.md](./CONTRIBUTING.md).
Les conventions de code (JS/TS et Java) sont dans [CONVENTIONS.md](./documentation/CONVENTIONS.md).

