```mermaid
classDiagram
    direction TB

%% ======================== MODELS ===========================
    class TestPlan {
        +Long id
        +String name
        +String description
        +List~TestScenario~ scenarios
    }

    class TestScenario {
        +Long id
        +String name
        +String description
        +List~TestCase~ testCases
    }

    class TestCase {
        +Long id
        +String name
        +String description
        +String method
        +String endpoint
        +String body
        +int expectedStatus
    }

%% ======================== RESULTS ===========================
    class TestPlanResult {
        +String planName
        +int totalScenarios
        +int totalCases
        +int passed
        +int failed
        +List~TestScenarioResult~ scenarioResults
    }

    class TestScenarioResult {
        +String scenarioName
        +int totalCases
        +int passed
        +int failed
        +List~TestResult~ caseResults
    }

    class TestResult {
        +String testCaseName
        +String method
        +String endpoint
        +int expectedStatus
        +int actualStatus
        +boolean passed
        +long responseTimeMs
    }

%% ======================== SERVICES ===========================
    class TestPlanService {
        +findAll()
        +findById(id)
        +save(plan)
        +update(id, plan)
        +deleteById(id)
    }

    class TestScenarioService {
        +findAll()
        +findById(id)
        +save(scenario)
        +update(id, scenario)
        +deleteById(id)
    }

    class TestCaseSe
```

```mermaid
sequenceDiagram
    participant U as 🧑‍💻 Utilisateur / Frontend
    participant C as TestPlanController
    participant S as TestPlanService
    participant R as TestRunnerService
    participant SC as TestScenario
    participant TC as TestCase
    participant RA as RestAssured
    participant RES as TestPlanResult

    U->>C: POST /api/testplans/{id}/run
    C->>S: findById(id)
    S-->>C: retourne TestPlan
    C->>R: runTestPlan(plan)

    loop Pour chaque TestScenario dans TestPlan
        R->>SC: runScenario(scenario)

        loop Pour chaque TestCase dans TestScenario
            R->>TC: runCase(testCase)
            TC->>RA: envoie requête HTTP (method, endpoint, body)
            RA-->>TC: Response (statusCode, time, body)
            TC-->>R: TestResult (expectedStatus, actualStatus, passed)
        end

        SC-->>R: TestScenarioResult (cas réussis/échoués)
    end

    R-->>C: TestPlanResult (résumé global)
    C-->>U: JSON complet (planName, passed, failed, scenarioResults)

```
| Étape                             | Description                                                                                      |
| --------------------------------- | ------------------------------------------------------------------------------------------------ |
| **1. Requête utilisateur**        | L’utilisateur (via Swagger, Postman ou UI) appelle `POST /api/testplans/1/run`.                  |
| **2. Récupération du plan**       | `TestPlanController` utilise `TestPlanService` pour charger le plan depuis la mémoire (ou base). |
| **3. Exécution du plan**          | Le contrôleur transmet le `TestPlan` à `TestRunnerService.runTestPlan()`.                        |
| **4. Boucle sur les scénarios**   | `TestRunnerService` parcourt chaque `TestScenario` du plan.                                      |
| **5. Boucle sur les cas de test** | Pour chaque `TestCase`, il exécute `runCase()`.                                                  |
| **6. Exécution HTTP réelle**      | `RestAssured` envoie la requête REST (ex: `POST /auth/login`) et renvoie le `Response`.          |
| **7. Comparaison et validation**  | `TestRunnerService` compare `expectedStatus` et `actualStatus` → crée un `TestResult`.           |
| **8. Construction du rapport**    | Après tous les cas → création d’un `TestScenarioResult`, puis d’un `TestPlanResult`.             |
| **9. Retour du résultat**         | `TestPlanController` renvoie au client un JSON hiérarchique des résultats.                       |

```mermaid
sequenceDiagram
    participant C as TestPlanController
    participant R as TestRunnerService
    participant S as TestScenario
    participant TC as TestCase
    participant RA as RestAssured
    participant TR as TestResult
    participant SR as TestScenarioResult

    C->>R: runScenario(scenario)
    activate R

    loop Pour chaque TestCase dans le scénario
        R->>TC: runCase(testCase)
        activate TC

        TC->>RA: exécute requête HTTP<br/>(method, endpoint, body)
        RA-->>TC: Response (statusCode, body, time)
        deactivate TC

        R->>TR: créer TestResult
        note right of TR: Compare expectedStatus<br/>et actualStatus<br/>→ définit passed=true/false
        R->>SR: ajouter TestResult
    end

    R-->>C: TestScenarioResult (passed, failed, totalCases)
    deactivate R

```
| Étape                               | Description                                                                                            |
| ----------------------------------- | ------------------------------------------------------------------------------------------------------ |
| **1. Appel `runScenario()`**        | Le contrôleur ou `TestRunnerService` appelle la méthode pour exécuter un seul scénario.                |
| **2. Boucle sur les cas de test**   | Pour chaque `TestCase` contenu dans le scénario…                                                       |
| **3. Envoi de la requête HTTP**     | `RestAssured` construit la requête (`method`, `endpoint`, `body`) et l’envoie vers l’API cible.        |
| **4. Réception de la réponse**      | `RestAssured` retourne un objet `Response` (code, corps, temps d’exécution).                           |
| **5. Validation**                   | Le service compare le `expectedStatus` et le `actualStatus`, calcule `passed` (true/false).            |
| **6. Construction du `TestResult`** | Chaque résultat individuel est enregistré.                                                             |
| **7. Agrégation**                   | Les résultats sont regroupés dans un `TestScenarioResult` contenant le nombre de tests passés/échoués. |
| **8. Retour au contrôleur**         | `runScenario()` retourne le résultat complet du scénario.                                              |


```mermaid
sequenceDiagram
    participant R as TestRunnerService
    participant TC as TestCase
    participant RA as RestAssured
    participant RESP as Response
    participant TR as TestResult

    activate R
    R->>TC: runCase(testCase)
    activate TC

    note over TC: Prépare requête HTTP<br/>avec method, endpoint, body

    TC->>RA: envoyer requête HTTP
    activate RA
    RA-->>RESP: Response (statusCode, body, time)
    deactivate RA

    TC-->>R: retourne Response
    deactivate TC

    R->>TR: créer TestResult (expected vs actual)
    note right of TR: Compare expectedStatus<br/>avec response.statusCode
    deactivate R


```

```mermaid
sequenceDiagram
    participant U as 🧑‍💻 Utilisateur / Frontend
    participant C as TestPlanController
    participant PS as TestPlanService
    participant RS as TestRunnerService
    participant SC as TestScenario
    participant TC as TestCase
    participant RA as RestAssured
    participant TR as TestResult
    participant SR as TestScenarioResult
    participant PR as TestPlanResult

    %% --- Niveau 1 : Plan ---
    U->>C: POST /api/testplans/{id}/run
    activate C
    C->>PS: findById(id)
    activate PS
    PS-->>C: retourne TestPlan
    deactivate PS

    C->>RS: runTestPlan(plan)
    activate RS

    %% --- Niveau 2 : Scénario ---
    loop pour chaque TestScenario dans le plan
        RS->>SC: runScenario(scenario)
        activate SC

        %% --- Niveau 3 : Cas de test ---
        loop pour chaque TestCase dans le scénario
            SC->>TC: runCase(testCase)
            activate TC
            note over TC: Prépare la requête (method, endpoint, body)
            TC->>RA: exécute requête HTTP via RestAssured
            activate RA
            RA-->>TC: Response (statusCode, body, time)
            deactivate RA
            TC-->>SC: retourne Response
            deactivate TC

            SC->>TR: créer TestResult<br/>(expected vs actual)
            note right of TR: Compare expectedStatus / actualStatus
            SC->>SR: ajoute TestResult
        end

        SC-->>RS: TestScenarioResult (passed, failed, totalCases)
        deactivate SC
    end

    %% --- Synthèse des résultats ---
    RS->>PR: assembler tous les TestScenarioResult
    RS-->>C: retourne TestPlanResult (global)
    deactivate RS

    %% --- Retour utilisateur ---
    C-->>U: JSON TestPlanResult<br/>(planName, passed, failed, scenarioResults)
    deactivate C

```

| Niveau             | Élément                                        | Description                                                                              |
| ------------------ | ---------------------------------------------- | ---------------------------------------------------------------------------------------- |
| **1. Plan**        | `TestPlanController` → `TestPlanService`       | Le plan de test est récupéré en mémoire ou BD.                                           |
| **2. Scénarios**   | `TestRunnerService` boucle sur chaque scénario | Chaque scénario représente une suite fonctionnelle (Auth, User, etc.).                   |
| **3. Cas de test** | `RestAssured` exécute chaque requête API       | Chaque `TestCase` définit une requête REST, un code attendu, et produit un `TestResult`. |
| **4. Résultats**   | `TestRunnerService` regroupe les résultats     | Agrégation → `TestScenarioResult` → `TestPlanResult`.                                    |
| **5. Sortie**      | `Controller` renvoie un JSON                   | Résumé complet du plan exécuté, scénarios, cas, et succès/échecs.                        |


💡 What’s new
Change	Description
Static block	Runs once when the class loads; sets RestAssured’s global configuration.
Timeouts	http.connection.timeout, http.socket.timeout, http.connection-manager.timeout all set to 10 000 ms (10 s).
Applies globally	Every RestAssured request (GET, POST, etc.) uses these limits automatically.
Independent of CORS	This is a real network timeout — unrelated to @CrossOrigin(maxAge=3600).