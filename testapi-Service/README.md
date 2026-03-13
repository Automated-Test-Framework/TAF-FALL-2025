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
| Mongo Express | http://localhost:8881 | Interface admin MongoDB |

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
   - **Corps de la requête (input)** — le corps JSON à envoyer (pour POST/PUT, optionnel)
   - **Sortie attendue** — le corps JSON attendu dans la réponse (optionnel)
   - **En-têtes attendus** — les en-têtes attendus dans la réponse (optionnel)
   - **Temps de réponse attendu (ms)** — temps maximum acceptable en ms (optionnel)
5. Cliquer **« Lancer les tests »** — tous les tests du tableau s'exécutent en parallèle
6. La colonne **TDR attendu (ms)** affiche la valeur seuil définie par l'utilisateur ; la colonne **TDR (ms)** affiche le temps réel mesuré (vert si ≤ seuil, rouge si dépassé)
7. La colonne **Résultat** affiche **vert** (pass) ou **rouge** (fail) avec un tooltip détaillant les erreurs

**Fonctions supplémentaires :**

- **Modifier** — modifie un test existant en ouvrant le formulaire pré-rempli
- **Export CSV** — télécharge les définitions de tests au format CSV
- **Import CSV** — charge des tests depuis un fichier CSV
- **Supprimer** — supprime un test individuel du tableau
- **Mode Gherkin** — éditeur BDD permettant d'écrire les tests en syntaxe Gherkin (voir ci-dessous)

> **Note :** Les résultats d’exécution sont conservés en mémoire côté frontend uniquement. Un rafraîchissement de la page réinitialise les résultats, mais les **définitions de tests sont persistées dans MongoDB** et rechargées automatiquement au login.

### Mode Gherkin (BDD)

Le **Mode Gherkin** offre une alternative au formulaire tableau pour définir les tests d'API. Il permet d'écrire les tests en syntaxe [Gherkin](https://cucumber.io/docs/gherkin/) (Given/When/Then), le format standard BDD utilisé par Cucumber.

#### Accès

1. Cliquer sur le bouton **« Mode Gherkin »** en haut de la page Tests API
2. L'éditeur Gherkin remplace le tableau — avec coloration syntaxique en temps réel
3. Écrire les scénarios ou charger un fichier `.feature`
4. Cliquer **« Appliquer »** pour convertir les scénarios en tests dans le tableau

#### Syntaxe supportée

```gherkin
Feature: API Tests

  Scenario: Vérifier que GET retourne les bonnes données
    Given the API method is "GET"
    And the API URL is "https://jsonplaceholder.typicode.com/posts/1"
    And the expected status code is 200
    And the header "Accept" is "application/json"
    When I execute the test
    Then the test should pass

  Scenario: Créer un post via POST
    Given the API method is "POST"
    And the API URL is "https://jsonplaceholder.typicode.com/posts"
    And the expected status code is 201
    And the header "Content-Type" is "application/json"
    And the input is '{"title": "foo", "body": "bar", "userId": 1}'
    When I execute the test
    Then the test should pass
```

| Étape | Pattern | Exemple |
|-------|---------|---------|
| Méthode HTTP | `the API method is "METHOD"` | `Given the API method is "POST"` |
| URL de l'API | `the API URL is "URL"` | `And the API URL is "https://..."` |
| Code statut | `the expected status code is CODE` | `And the expected status code is 200` |
| En-tête requête | `the header "KEY" is "VALUE"` | `And the header "Accept" is "application/json"` |
| Corps requête | `the input is 'JSON'` | `And the input is '{"key": "val"}'` |
| Sortie attendue | `the expected output is 'JSON'` | `And the expected output is '{"id": 1}'` |
| En-tête réponse | `the expected header "KEY" is "VALUE"` | `And the expected header "Content-Type" is "..."` |
| Temps de réponse | `the response time is MS` | `And the response time is 5000` |

Chaque `Scenario` devient un test dans le tableau. La conversion est **bidirectionnelle** : les tests existants dans le tableau sont automatiquement convertis en Gherkin lorsqu'on entre en mode Gherkin.

#### Fonctions de l'éditeur

- **Exemple** — charge un template Gherkin pré-rempli
- **Importer** — charge un fichier `.feature` depuis le disque
- **Exporter** — télécharge le contenu actuel en fichier `.feature`
- **Aperçu** — affiche un panneau latéral montrant les tests détectés en temps réel
- **Appliquer** — convertit les scénarios en tests et retourne au mode tableau

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

| `responseTime` | `int` | Non | Temps de réponse maximum acceptable (ms). Si l'API répond plus lentement, le test échoue |
| `expectedHeaders` | `Map<String, String>` | Non | En-têtes HTTP attendus dans la réponse de l'API cible |

> Le backend proxy transmet tous les champs au microservice testapi, y compris `responseTime` et `expectedHeaders`.

### Validations exécutées (assertions)

Le microservice testapi exécute **3 assertions actives** via Rest-Assured sur chaque test :

| Assertion | Logique | Message d'erreur si échec |
|-----------|---------|---------------------------|
| **Code de statut** | `code attendu == code reçu` | `❌ Code de statut attendu: 200, reçu: 404` |
| **Corps de la réponse** | Comparaison JSON récursive champ par champ via `JsonComparator` | Retourne `fieldAnswer` avec `true`/`false` par champ |
| **En-têtes de réponse** | Chaque paire clé/valeur de `expectedHeaders` doit exister dans la réponse | `❌ En-tête attendu manquant: Content-Type` |
| **Temps de réponse** | `temps réel ≤ responseTime` (si `responseTime > 0`) | `❌ Temps de réponse trop long : 3026 ms (max: 1000 ms)` |

### Gestion du timeout

Le système gère les timeouts à deux niveaux :

| Niveau | Configuration | Défaut | Description |
|--------|---------------|--------|-------------|
| **Backend → Testapi** | `taf.app.testAPI_timeout` | 30 000 ms | Timeout HttpClient du proxy backend vers le microservice testapi |
| **Testapi → API cible** | `timeout.connection`, `timeout.socket` | 10 000 ms | Timeout RestAssured vers l'API cible testée |

Si l'API cible ne répond pas dans le délai (`SocketTimeoutException`), le test retourne un message d'erreur explicite au lieu de crasher.

Un endpoint `/slow?delay=N` est disponible sur le testapi (port 8086) pour tester le comportement de timeout. Le paramètre `delay` accepte une valeur entre 0 et 120 000 ms ; toute valeur hors de cette plage retourne `400 Bad Request`.

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

### Persistance des définitions de tests (MongoDB)

Les définitions de tests sont **persistées automatiquement** dans MongoDB via l'API `ApiTestDefinitionController`. Chaque utilisateur ne voit que ses propres tests (isolation par JWT username).

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `GET` | `/api/testapi/definitions` | Liste les définitions de l'utilisateur connecté |
| `POST` | `/api/testapi/definitions` | Crée une nouvelle définition |
| `PUT` | `/api/testapi/definitions/{id}` | Met à jour une définition (propriétaire uniquement) |
| `DELETE` | `/api/testapi/definitions/{id}` | Supprime une définition (propriétaire uniquement) |

Le frontend appelle ces endpoints automatiquement lors de l'ajout, la modification ou la suppression de tests. Au chargement de la page, les définitions sauvegardées sont rechargées depuis MongoDB.

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

# Connexion → retourne un accessToken + refreshToken
curl -X POST http://localhost:8084/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{"username":"user1","password":"password123"}'
```

La réponse contient un `accessToken` (durée : 30 min) et un `refreshToken` (durée : 7 jours).

Utilisez le token dans l'en-tête : `Authorization: Bearer <accessToken>`

### Renouvellement de token (Refresh Token)

Lorsque l'`accessToken` expire, utilisez le `refreshToken` pour obtenir une nouvelle paire de tokens sans ressaisir les identifiants :

```bash
curl -X POST http://localhost:8084/api/auth/refresh-token \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<votre-refresh-token>"}'
```

La réponse retourne un nouveau `accessToken` et un nouveau `refreshToken` (rotation). Le frontend effectue ce renouvellement automatiquement via l'intercepteur HTTP lorsqu'une réponse 401 est reçue.

### Google OAuth2

Connexion via compte Google disponible sur la page de login du frontend. Nécessite les variables d'environnement `GOOGLE_CLIENT_ID` et `GOOGLE_CLIENT_SECRET` configurées avant le démarrage Docker :

```powershell
$env:GOOGLE_CLIENT_ID = "votre-client-id.apps.googleusercontent.com"
$env:GOOGLE_CLIENT_SECRET = "votre-client-secret"
docker compose -f docker-compose-local-test.yml up -d --build backend-team2
```

> Sans ces variables, le login Google ne fonctionne pas, mais le reste de l'application (JWT, tests d'API, Gatling, Selenium) fonctionne normalement.

### Endpoints publics

`POST /api/auth/signup`, `POST /api/auth/signin`, `POST /api/auth/refresh-token`, `GET /api/oauth2/login-url`, `/swagger-ui/**`, `/api-docs/**`, `/actuator/**`

---

## Documentation API (Swagger UI)

Accessible à : [http://localhost:8084/swagger-ui.html](http://localhost:8084/swagger-ui.html)

1. Connectez-vous via `POST /api/auth/signin` — copiez le `accessToken`
2. Cliquez sur **Authorize** (🔒) → entrez `Bearer <token>` → validez
3. Tous les endpoints sont accessibles avec des exemples pré-remplis
4. Si le token expire (30 min), utilisez `POST /api/auth/refresh-token` pour en obtenir un nouveau

---

## Variables d'environnement

| Variable | Description | Valeur par défaut |
|----------|-------------|-------------------|
| `DB_URI` | URI de connexion MongoDB | — |
| `DB_NAME` | Nom de la base de données | — |
| `DB_AUTH` | Base d'authentification MongoDB | `admin` |
| `JWT_SECRET` | Clé secrète pour la signature JWT (HS512) | — |
| `JWT_EXPIRES` | Durée de validité de l'access token JWT (ms) | `1800000` (30 min) |
| `JWT_REFRESH_EXPIRES` | Durée de validité du refresh token JWT (ms) | `604800000` (7 jours) |
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
| Angular | 13.3 | Framework frontend |
| MongoDB | latest | Stockage utilisateurs/rôles |
| Gatling | — | Tests de performance |
| Selenium Java | 4.27.0 | Tests d'interface |
| Maven | 3.9+ | Build backend |
| jjwt | 0.9.1 | Tokens JWT |
| springdoc-openapi | 2.6.0 | Swagger UI |

## Tests unitaires

Le backend dispose d'une suite de **87 tests unitaires** avec couverture JaCoCo.

```powershell
# One-click : depuis la racine du projet
.\run-tests-testapi.ps1

# Ou manuellement
cd testapi-Service
mvn test -pl backend "-P !with-frontend"
```

| Métrique | Valeur |
|---|---|
| Tests totaux | 87 |
| Réussis | 87 |
| Échoués | 0 |
| Ignorés | 1 |
| Couverture lignes (classes équipe) | **98%** (433/438) |
| Modules couverts | Sécurité (JWT, OAuth2, filtres), contrôleurs, entités, DTOs, persistance MongoDB, **timeout** |

Le rapport de couverture est dans `backend/target/site/jacoco/index.html`.  
Le détail complet des tests est dans [TEST-REPORT.md](./TEST-REPORT.md).

## Contribuer

La démarche pour contribuer est disponible dans [CONTRIBUTING.md](./CONTRIBUTING.md).
Les conventions de code (JS/TS et Java) sont dans [CONVENTIONS.md](./documentation/CONVENTIONS.md).

