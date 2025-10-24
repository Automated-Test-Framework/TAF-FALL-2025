# 🧪 TEST-API — Microservice de Tests RestAssured

Une plateforme modulaire et légère de **gestion et d’exécution de tests API** développée en **Java 17 / Spring Boot / RestAssured**.  
Elle permet de définir des **plans de test**, des **scénarios** et des **cas de test** exécutables dynamiquement.  
Les résultats contiennent les **codes d’état HTTP**, **temps de réponse**, et **indicateurs de réussite/échec**.  
Le service peut être utilisé en **microservice autonome** ou intégré à une architecture distribuée.

---

## ⚙️ Configuration & Port

L’application est **configurée comme un microservice Spring Boot**.

### Port :

```yaml
server:
  port: 8082
```

➡️ Vous pouvez modifier ce port sans impacter le fonctionnement.  
Dans ce cas, n’oubliez pas d’ajuster le port exposé dans le **Dockerfile** :

```dockerfile
EXPOSE 8082
```

et, si nécessaire, dans les appels RestAssured :
```java
RestAssured.baseURI = "http://localhost:8082";
```

---

## 🧱 Installation & Build

### 🖥️ Sur votre machine (Maven)
**Prérequis :**
- Java 17+
- Maven 3.8+ installé et accessible (`mvn -v`)

#### 🔨 Build & Installation
```bash
mvn clean install
```

#### 🚀 Exécution
```bash
mvn spring-boot:run
```

L’application sera disponible à :
👉 [http://localhost:8082](http://localhost:8082)

---

## 🐳 Docker

### 🧩 Construction de l’image
```bash
docker build -t testapi-service .
```

### ▶️ Lancement du conteneur
```bash
docker run -d -p 8082:8082 testapi-service
```

✅ Le service est maintenant disponible sur  
[http://localhost:8082](http://localhost:8082)

---

## 🌐 Documentation Swagger / OpenAPI

### URLs :
- **Swagger UI** → [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)
- **Spécification OpenAPI JSON** → [http://localhost:8082/v3/api-docs](http://localhost:8082/v3/api-docs)

| Contrôleur | Tag | Endpoint |
|-------------|------|-----------|
| `TestPlanController` | 🧩 Plans de test | `/api/testplans` |
| `TestScenarioController` | 🧠 Scénarios de test | `/api/testscenarios` |
| `TestCaseController` | ⚙️ Cas de test | `/api/testcases` |

---

## 🧩 Architecture

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

### Packages
```
restAssuredTesting/
 ├── model/         → Entités de test (Plan, Scénario, Cas, Résultats)
 ├── service/       → Logique métier et exécution (TestRunnerService)
 ├── requests/      → Contrôleurs REST (TestPlanController, etc.)
 ├── config/        → SwaggerConfig, OpenAPIConfig
 └── DemoDataInitializer.java
```

---

## 🧠 Exemple d’exécution

### Commande
```bash
curl -X POST http://localhost:8082/api/testplans/1/run
```

### Réponse JSON
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

## ⏱️ Timeout Global (RestAssured)

Les requêtes API sont limitées à **10 secondes** pour éviter tout blocage :

```java
RestAssured.config = RestAssuredConfig.config().httpClient(
    HttpClientConfig.httpClientConfig()
        .setParam("http.connection.timeout", 10000)
        .setParam("http.socket.timeout", 10000)
        .setParam("http.connection-manager.timeout", 10000)
);
```

Si une requête dépasse ce délai, le test est marqué comme :
```json
{
  "testCaseName": "POST /orders",
  "passed": false,
  "message": "ÉCHEC - Timeout ou erreur réseau : Read timed out"
}
```

---

## 🔍 Vérification de Code — Checkstyle

**Checkstyle** garantit la conformité du style et des conventions Java.

### Commandes Maven
```bash
mvn checkstyle:check
```
ou :
```bash
mvn clean verify
```

### Rapport généré
```
target/site/checkstyle.html
```

---

## 📊 Couverture et Qualité — JaCoCo & SonarQube

| Outil | Commande | Résultat |
|--------|-----------|-----------|
| **JaCoCo** | `mvn verify` | Rapport : `target/site/jacoco/index.html` |
| **SonarQube** | `mvn sonar:sonar -Dsonar.login=<TOKEN>` | Dashboard : [http://localhost:9000](http://localhost:9000) |

### Exemple d’analyse locale SonarQube :
1. Lancez SonarQube :
   ```bash
   C:\sonarqube\bin\windows-x86-64\StartSonar.bat
   ```
2. Générez un token dans **My Account → Security**
3. Exécutez :
   ```bash
   mvn clean verify sonar:sonar -Dsonar.login=YOUR_TOKEN
   ```

---

## ✅ Checklist de Bonnes Pratiques

| Domaine | Bonnes pratiques |
|----------|------------------|
| **Structure** | 1 classe = 1 responsabilité |
| **Tests unitaires** | 80 % de couverture min |
| **Logs** | Utiliser SLF4J (`log.info()`, `log.error()`) |
| **Exceptions** | Centralisées via `@ControllerAdvice` |
| **Conventions** | Checkstyle actif |
| **Documentation** | Swagger / OpenAPI à jour |
| **CI/CD** | JaCoCo + Sonar intégrés |

---

## 📈 Évolutions futures

- [ ] Stockage persistant des résultats (PostgreSQL)
- [ ] Ajout d’assertions sur le corps JSON
- [ ] Authentification dynamique par scénario
- [ ] UI de visualisation des résultats
- [ ] Pipeline CI/CD avec analyse automatisée
