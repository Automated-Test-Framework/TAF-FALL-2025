# Rapport de Tests Unitaires — testapi-Service Backend

**Projet** : TAF (Test Automation Framework) — Équipe 3  
**Cours** : MGL805, ÉTS, Hiver 2026  
**Date** : 2026-03-11  
**Branche** : `feature/Timeout`  
**Framework** : JUnit 5 (Jupiter) + Mockito + Spring Boot Test  
**Couverture** : JaCoCo 0.8.12

---

## Résumé

| Métrique | Valeur |
|---|---|
| Tests totaux | 87 |
| Réussis | 87 |
| Échoués | 0 |
| Ignorés | 1 (TestAutomationFrameworkApplicationTests — test de contexte Spring sans MongoDB) |
| Couverture lignes (24 classes équipe) | **98%** (433/438) |
| Couverture instructions (classes 100%) | 20/24 classes |
| Couverture branches (classes 100%) | 20/24 classes |

---

## Comment exécuter les tests

```powershell
# Depuis le répertoire racine du projet
.\testapi-Service\run-tests-testapi.ps1

# Ou manuellement
cd testapi-Service
mvn test -pl backend -am
```

Le rapport de couverture JaCoCo est généré dans :  
`testapi-Service/backend/target/site/jacoco/index.html`

---

## Matrice de tests

### 1. Sécurité — JWT (`security.jwt`)

| Classe testée | Fichier de test | Tests | Couverture |
|---|---|---|---|
| `JwtUtils` | `JwtUtilsTest.java` | 13 | Instr: 100%, Branch: 100% |
| `AuthTokenFilter` | `AuthTokenFilterTest.java` | 5 | ↑ inclus |
| `AuthEntryPointJwt` | `AuthEntryPointJwtTest.java` | 2 | ↑ inclus |

**Scénarios couverts :**
- Génération de token JWT et extraction du username
- Validation : token valide, null, vide, malformé, clé incorrecte, expiré
- **Token non signé (UnsupportedJwtException)**
- **Génération de refresh token et validation de sa durée d'expiration**
- **Extraction du username depuis un token expiré (`getUserNameFromExpiredJwtToken`)**
- **Extraction du username depuis un token encore valide via la méthode expired**
- Filtre : Bearer token valide → SecurityContext, pas de header / token invalide / non-Bearer → continue sans auth
- Exception dans le filtre → continue chain sans auth
- Point d'entrée non autorisé → réponse JSON 401 avec status/error/message/path

### 2. Sécurité — OAuth2 (`security.oauth2`)

| Classe testée | Fichier de test | Tests | Couverture |
|---|---|---|---|
| `OAuth2LoginSuccessHandler` | `OAuth2LoginSuccessHandlerTest.java` | 6 | Instr: 100%, Branch: 100% |

**Scénarios couverts :**
- Nouvel utilisateur Google → création + JWT + redirection
- Utilisateur existant par `googleId` → réutilisation sans sauvegarde
- Utilisateur existant par email → liaison du compte Google
- Username en double → ajout de suffixe `_g` + googleId
- Rôle `ROLE_USER` absent en base → création avec rôles vides
- `ObjectMapper` lance une exception → redirection avec `userInfo` vide

### 3. Sécurité — Services (`security.services`)

| Classe testée | Fichier de test | Tests | Couverture |
|---|---|---|---|
| `UserDetailsImpl` | `UserDetailsImplTest.java` | 8 | Instr: 100%, Branch: 100% |
| `UserDetailsServiceImpl` | `UserDetailsServiceImplTest.java` | 2 | ↑ inclus |

**Scénarios couverts :**
- Construction depuis entité `User` (simple et multi-rôles)
- Méthodes booléennes (`isAccountNonExpired`, etc.) retournent `true`
- `equals()` : même ID, ID différent, null, même référence, type différent
- `loadUserByUsername` : trouvé et non trouvé (`UsernameNotFoundException`)

### 4. Sécurité — Configuration (`security`)

| Classe testée | Fichier de test | Tests | Couverture |
|---|---|---|---|
| `WebSecurityConfig` | `WebSecurityConfigTest.java` | 6 | Instr: 100%, Branch: n/a |

**Scénarios couverts :**
- Endpoints publics (`/api/auth/**`, `/api/oauth2/login-url`, Swagger) retournent 200
- Endpoints protégés (`/api/testapi/checkApi`) retournent 401 sans JWT
- **Bean `authenticationJwtTokenFilter()` retourne une instance valide**
- **Bean `authenticationProvider()` retourne un `DaoAuthenticationProvider`**
- **Bean `authenticationManager()` retourne un `AuthenticationManager`**

### 5. Contrôleurs (`controller`)

| Classe testée | Fichier de test | Tests | Couverture |
|---|---|---|---|
| `AuthController` | `AuthControllerTest.java` | 15 | Instr: 100%, Branch: 100% |
| `OAuth2Controller` | `OAuth2ControllerTest.java` | 1 | ↑ inclus |
| `TestApiController` | `TestApiControllerTest.java` | 7 | Instr: 100%, Branch: 100% |
| `ApiTestDefinitionController` | `ApiTestDefinitionControllerTest.java` | 9 | Instr: 100%, Branch: 100% |

**Scénarios couverts :**
- `AuthController` : connexion (JWT + refresh token valide, mauvais credentials, body vide/400), inscription (succès, username dupliqué, email dupliqué, rôle admin, **rôle non-admin ROLE_USER uniquement**), **renouvellement de token (refresh valide → nouvelle paire, refresh expiré → 401, utilisateur inexistant → 401)**, **RuntimeException sur signup et signin**
- `OAuth2Controller` : GET `/api/oauth2/login-url` retourne les infos du provider
- `TestApiController` : construction URI, accessibilité des champs, DTO `TestApiRequest`, **appel HTTP réel vers un HttpServer embarqué** avec vérification status code, **vérification du champ `testApiTimeout`**, **HttpClient utilise connectTimeout/timeout depuis `testApiTimeout`**, **timeout déclenché quand le serveur est trop lent (HttpTimeoutException)**
- `ApiTestDefinitionController` : CRUD complet pour les définitions de tests API persistées dans MongoDB — création, lecture (par utilisateur), mise à jour (avec vérification propriétaire), suppression (avec vérification propriétaire), test 404 pour ID inexistant, test 403 pour accès non autorisé, test liste vide

**Non testés** : `TestSeleniumController`, `GatlingApiController` — contrôleurs d'autres équipes nécessitant des services externes (Selenium, Gatling)

### 6. Entités et DTOs

| Classe testée | Fichier de test | Tests | Couverture |
|---|---|---|---|
| `User`, `Role`, `ERole` | `UserEntityTest.java`, `RoleTest.java` | 9 | Instr: 100%, Branch: 100% |
| `JwtResponse` | `JwtResponseTest.java` | 4 | Instr: 100% |
| `MessageResponse` | `MessageResponseTest.java` | 2 | Instr: 100% |

**Scénarios couverts :**
- `User` : constructeur local, constructeur OAuth2, constructeur par défaut + setters, assignation de rôles
- `ERole` : enum contient les 3 valeurs
- `Role` : **constructeur par défaut, constructeur avec paramètres, getter/setter pour id et name**
- `JwtResponse` : constructeur, setters, type de token par défaut, **setter refreshToken**
- `MessageResponse` : constructeur/getter, setter

---

## Couverture par package

### Classes de l'équipe (24 classes) — 97% couverture lignes

| Package | Instructions | Branches | Note |
|---|---|---|---|
| `security` | 100% | n/a | WebSecurityConfig |
| `security.services` | 100% | 100% | UserDetailsImpl, UserDetailsServiceImpl |
| `security.oauth2` | 100% | 100% | OAuth2LoginSuccessHandler |
| `security.jwt` | 100% | 100% | JwtUtils, AuthTokenFilter, AuthEntryPointJwt |
| `payload.request` | 100% | n/a | LoginRequest, SignupRequest, RefreshTokenRequest, TestApiRequest |
| `payload.response` | 100% | n/a | JwtResponse, MessageResponse |
| `controller` | 100% | 100% | AuthController, TestApiController, ApiTestDefinitionController, OAuth2Controller |
| `entity` | 100% | 100% | User, Role, ERole, ApiTestDefinition |
| `config` | 60% | 25% | MvcConfiguration (inner PathResourceResolver non testé) |

### Autres packages (autres équipes / infrastructure)

| Package | Instructions | Branches | Note |
|---|---|---|---|
| `taf` (main) | 58% | n/a | Classe principale |
| `provider` | 90% | 50% | GatlingJarPathProvider (autre équipe) |
| `config` | 50% | 0% | Configuration Spring |
| `dto` | 25% | n/a | SeleniumCaseDto (autre équipe) |
| `apiCommunication` | 19% | n/a | SeleniumServiceRequester (autre équipe) |
| `service` | 17% | 0% | SeleniumService (autre équipe) |
| `eureka` | 5% | n/a | EurekaClientConfig (infra) |

---

## Packages non testés — justification

| Package | Raison |
|---|---|
| `eureka` | Configuration Eureka Service Discovery — infrastructure, pas de logique métier |
| `apiCommunication` | `SeleniumServiceRequester` — appels HTTP vers service externe Selenium |
| `service` | `SeleniumService` — orchestrateur pour Selenium, nécessite MockServer |
| `dto` | `SeleniumCaseDto` — DTO Lombok d'autre équipe, 0 logique |
| `entity` (partiel) | Entités Lombok avec `@Data` — getter/setter auto-générés contribuent au code non couvert |

---

## Outils et dépendances de test

### 7. Timeout — Configuration et gestion (`controller`, `testapi`)

| Classe testée | Fichier de test | Tests | Couverture |
|---|---|---|---|
| `TestApiController` (timeout) | `TestApiControllerTest.java` | 3 | Instr: 100% |

**Scénarios couverts (Timeout) :**
- Champ `testApiTimeout` existe et est injectable via `@Value("${taf.app.testAPI_timeout:30000}")`
- `HttpClient` utilise `connectTimeout` + `timeout` dérivés de `testApiTimeout`
- Serveur trop lent → `HttpTimeoutException` levée (timeout à 500 ms, serveur dort 5 s)

**Fonctionnalités Timeout (non testées unitairement, validées en intégration Docker) :**
- `TimeoutConfig` (testapi) — configure RestAssured `connection.timeout`, `socket.timeout`, `connection-manager.timeout`
- `/slow?delay=N` endpoint de test — simule un serveur lent
- `SocketTimeoutException` → message d'erreur au lieu de crash
- `checkResponseTime()` — valide le temps de réponse vs seuil (`responseTime` en ms)
- Champs `responseTime` et `expectedHeaders` maintenant transmis par le proxy backend

---

## Outils et dépendances de test

| Outil | Version | Usage |
|---|---|---|
| JUnit Jupiter | 5.12.2 (géré par Spring Boot BOM) | Framework de test |
| Mockito | 5.x (géré par Spring Boot BOM) | Mocking |
| Spring Boot Test | 3.5.10 | `@WebMvcTest`, `MockMvc` |
| Spring Security Test | 6.x | Test des filtres de sécurité |
| JaCoCo | 0.8.12 | Couverture de code |
| JDK | 17 (Temurin 17.0.18+8) | Compilation et exécution |
