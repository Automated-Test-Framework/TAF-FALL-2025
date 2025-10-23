# 🧪 API de Tests RestAssured

Une plateforme légère de **gestion et d’exécution de tests API** développée avec **Spring Boot** et **RestAssured**.  
Elle permet de définir des **plans de test**, des **scénarios de test** et des **cas de test**, puis de les exécuter dynamiquement via des points d’accès REST.  
Les résultats produits contiennent les **codes d’état**, les **temps de réponse** et les **indicateurs de réussite/échec**.

---

## 🚀 Fonctionnalités

✅ Gestion des entités de test :
- **Plans de test** → Regroupement de scénarios liés
- **Scénarios de test** → Groupes logiques de cas (ex. “Flux d’authentification”)
- **Cas de test** → Requêtes REST individuelles (méthode, endpoint, corps, statut attendu)

✅ Exécution automatisée :
- Utilise **RestAssured** pour envoyer les requêtes HTTP
- Supporte `GET`, `POST`, `PUT`, `DELETE`
- Compare les statuts attendus et réels

✅ Données de démonstration intégrées :
- 4 plans de test par défaut : Auth, Utilisateur, Commande, Régression complète
- Exécution immédiate via `/api/testplans/1/run`

✅ Documentation interactive :
- Interface **Swagger / OpenAPI 3.0** intégrée
- Regroupement des endpoints par catégorie (Plans, Scénarios, Cas)

---

## 🧩 Architecture Générale

### Vue d’ensemble
```
Utilisateur → TestPlanController → TestRunnerService → RestAssured → Résultats
```

### Relations entre objets
```
TestPlan (1) → TestScenario (n)
TestScenario (1) → TestCase (n)
TestCase (1) → TestResult (1)
```

### Structure des packages
```
restAssuredTesting/
 ├── model/         → Entités (TestPlan, TestScenario, TestCase, Results)
 ├── service/       → Logique & exécution (TestPlanService, TestRunnerService)
 ├── requests/      → Contrôleurs REST (Plan, Scénario, Cas)
 ├── config/        → Configuration Swagger/OpenAPI
 └── DemoDataInitializer.java
```

---

## ⚙️ Installation et Exécution

### Prérequis
- Java 17+
- Maven 3.8+
- Spring Boot 2.7+ (ou 3.x)
- SonarQube local (optionnel)
- Docker (optionnel)

### Cloner le projet
```bash
git clone https://github.com/<votre-org>/restassured-testing-api.git
cd restassured-testing-api
```

### Lancer l’application
```bash
mvn clean spring-boot:run
```

> ⚙️ L’application démarre sur le port **8082**  
> Accès : [http://localhost:8082](http://localhost:8082)

---

## 🌐 Swagger / OpenAPI

- **Swagger UI** : [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)
- **Spécification JSON** : [http://localhost:8082/v3/api-docs](http://localhost:8082/v3/api-docs)

| Contrôleur | Tag | Endpoint |
|-------------|------|-----------|
| `TestPlanController` | 🧩 Plans de test | `/api/testplans` |
| `TestScenarioController` | 🧠 Scénarios de test | `/api/testscenarios` |
| `TestCaseController` | ⚙️ Cas de test | `/api/testcases` |

---

## 🧠 Exemple d’exécution

```bash
curl -X POST http://localhost:8082/api/testplans/1/run
```

**Résultat :**
```json
{
  "planName": "Plan de Régression Complète",
  "totalScenarios": 4,
  "totalCases": 18,
  "passed": 17,
  "failed": 1
}
```

---

## ⏱️ Timeout Global

Les requêtes sont limitées à **10 s** :
```java
RestAssured.config = RestAssuredConfig.config().httpClient(
    HttpClientConfig.httpClientConfig()
        .setParam("http.connection.timeout", 10000)
        .setParam("http.socket.timeout", 10000)
        .setParam("http.connection-manager.timeout", 10000)
);
```

✅ Garantit que les API lentes ne bloquent pas l’exécution.

---

## 🔍 Checkstyle – Vérification de Code

**Checkstyle** valide la cohérence du style et des conventions Java.

### Commandes Maven
```bash
mvn checkstyle:check
```
ou pour exécuter en phase `verify` automatiquement :
```bash
mvn clean verify
```

### Emplacement du rapport
```
target/site/checkstyle.html
```

### Règles principales
| Catégorie | Exemples de vérifications |
|------------|---------------------------|
| **Nom de classe** | Commence par une majuscule |
| **Nom de méthode** | Commence par une minuscule |
| **Commentaires** | Javadoc obligatoire sur classes et méthodes publiques |
| **Formatage** | Indentation 4 espaces, pas de tabulations |
| **Complexité** | Méthodes < 30 lignes, faible complexité cyclomatique |

---

## ✅ Checklist des Bonnes Pratiques

| Domaine | Bonnes pratiques recommandées |
|----------|-------------------------------|
| **Structure du code** | 1 classe = 1 responsabilité, pas de logique métier dans les contrôleurs |
| **Tests unitaires** | Minimum 80 % de couverture (JaCoCo) |
| **Logs** | Utiliser SLF4J (`log.info()`, `log.error()`) |
| **Erreurs** | Gérer les exceptions dans un `@ControllerAdvice` |
| **Conventions** | Respect du style Java & règles Checkstyle |
| **Documentation** | Swagger/OpenAPI à jour |
| **CI/CD** | Intégration SonarQube et build Maven automatisé |

---

## 🧭 Analyse SonarQube Locale

### 1️⃣ Télécharger SonarQube
👉 [https://www.sonarsource.com/products/sonarqube/downloads/](https://www.sonarsource.com/products/sonarqube/downloads/)

Dézipper et lancer :
```bash
C:\sonarqube\bin\windows-x86-64\StartSonar.bat
```
puis ouvrir : [http://localhost:9000](http://localhost:9000)

### 2️⃣ Créer un jeton
1. Connectez-vous (`admin / admin`)
2. Changez le mot de passe
3. Allez dans : **My Account > Security**
4. Générez un token (ex. `testapi-token`)

### 3️⃣ Lancer l’analyse Maven
```bash
mvn clean verify sonar:sonar -Dsonar.login=VOTRE_TOKEN
```

### 4️⃣ Consulter les résultats
[http://localhost:9000/projects](http://localhost:9000/projects)

---

## 📊 Rapports & Qualité

| Outil | Fichier / URL | Description |
|--------|----------------|-------------|
| **JaCoCo** | `target/site/jacoco/index.html` | Rapport de couverture de tests |
| **Checkstyle** | `target/site/checkstyle.html` | Vérification des règles de style |
| **SonarQube** | [http://localhost:9000](http://localhost:9000) | Analyse globale du code (bugs, smells, duplications) |

---

## 📈 Améliorations Futures
- [ ] Persistance des résultats (H2 / PostgreSQL)
- [ ] Assertions JSONBody
- [ ] Support des headers / tokens dynamiques
- [ ] Interface graphique des résultats
- [ ] Pipeline CI/CD avec analyse qualité automatisée
