# Rapport de Tests Unitaires — testapi-Service Backend

**Projet** : TAF (Test Automation Framework) — Équipe 3  
**Cours** : MGL805, ÉTS, Hiver 2026  
**Date** : 2026-02-14  
**Branche** : `feature/UnitTest`  
**Framework** : JUnit 5 (Jupiter) + Mockito + Spring Boot Test  
**Couverture** : JaCoCo 0.8.12

---

## Résumé

| Métrique | Valeur |
|---|---|
| Tests totaux | 68 |
| Réussis | 67 |
| Échoués | 0 |
| Ignorés | 1 (`contextLoads` — nécessite MongoDB) |
| Couverture instructions (global) | **61%** |
| Couverture branches (global) | **33%** |

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
| `JwtUtils` | `JwtUtilsTest.java` | 12 | Instr: 97%, Branch: 100% |
| `AuthTokenFilter` | `AuthTokenFilterTest.java` | 5 | ↑ inclus |
| `AuthEntryPointJwt` | `AuthEntryPointJwtTest.java` | 2 | ↑ inclus |

**Scénarios couverts :**
- Génération de token JWT et extraction du username
- Validation : token valide, null, vide, malformé, clé incorrecte, expiré
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
| `WebSecurityConfig` | `WebSecurityConfigTest.java` | 4 | Instr: 100%, Branch: n/a |

**Scénarios couverts :**
- Endpoints publics (`/api/auth/**`, `/api/test/all`, Swagger) retournent 200
- Endpoints protégés (`/api/test/user`, `/api/test/admin`) retournent 401

### 5. Contrôleurs (`controller`)

| Classe testée | Fichier de test | Tests | Couverture |
|---|---|---|---|
| `AuthController` | `AuthControllerTest.java` | 10 | Instr: 90%, Branch: 80% |
| `OAuth2Controller` | `OAuth2ControllerTest.java` | 1 | ↑ inclus |
| `TestController` | `TestControllerTest.java` | 3 | ↑ inclus |
| `TestApiController` | `TestApiControllerTest.java` | 3 | ↑ inclus |

**Scénarios couverts :**
- `AuthController` : connexion (JWT + refresh token valide, mauvais credentials, body vide/400), inscription (succès, username dupliqué, email dupliqué, rôle admin), **renouvellement de token (refresh valide → nouvelle paire, refresh expiré → 401, utilisateur inexistant → 401)**
- `OAuth2Controller` : GET `/api/oauth2/login-url` retourne les infos du provider
- `TestController` : GET `/all`, `/user`, `/admin` retournent le bon contenu
- `TestApiController` : construction URI, accessibilité des champs, DTO `TestApiRequest`

**Non testés** : `TestSeleniumController`, `GatlingApiController` — contrôleurs d'autres équipes nécessitant des services externes (Selenium, Gatling)

### 6. Entités et DTOs

| Classe testée | Fichier de test | Tests | Couverture |
|---|---|---|---|
| `User`, `Role`, `ERole` | `UserEntityTest.java` | 6 | Instr: 23% (Lombok génère beaucoup de code) |
| `JwtResponse` | `JwtResponseTest.java` | 3 | Instr: 100% |
| `MessageResponse` | `MessageResponseTest.java` | 2 | Instr: 100% |

**Scénarios couverts :**
- `User` : constructeur local, constructeur OAuth2, constructeur par défaut + setters, assignation de rôles
- `ERole` : enum contient les 3 valeurs
- `Role` : getter/setter
- `JwtResponse` : constructeur, setters, type de token par défaut
- `MessageResponse` : constructeur/getter, setter

---

## Couverture par package

| Package | Instructions | Branches | Note |
|---|---|---|---|
| `security` | 100% | n/a | WebSecurityConfig |
| `security.services` | 100% | 100% | UserDetailsImpl, UserDetailsServiceImpl |
| `security.oauth2` | 100% | 100% | OAuth2LoginSuccessHandler |
| `security.jwt` | 97% | 100% | JwtUtils, AuthTokenFilter, AuthEntryPointJwt |
| `payload.request` | 100% | n/a | LoginRequest, SignupRequest |
| `payload.response` | 100% | n/a | JwtResponse, MessageResponse |
| `controller` | 90% | 80% | 4/6 contrôleurs testés |
| `taf` (main) | 58% | n/a | Classe principale |
| `provider` | 90% | 50% | GatlingJarPathProvider (autre équipe) |
| `config` | 50% | 0% | Configuration Spring |
| `entity` | 23% | 0% | Lombok génère getter/setter non appelés |
| `dto` | 25% | n/a | SeleniumCaseDto (autre équipe) |
| `apiCommunication` | 19% | n/a | SeleniumServiceRequester (autre équipe) |
| `service` | 17% | 0% | SeleniumService (autre équipe) |
| `eureka` | 5% | n/a | EurekaClientConfig (infra) |
| **Total** | **61%** | **33%** |  |

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

| Outil | Version | Usage |
|---|---|---|
| JUnit Jupiter | 5.12.2 (géré par Spring Boot BOM) | Framework de test |
| Mockito | 5.x (géré par Spring Boot BOM) | Mocking |
| Spring Boot Test | 3.5.10 | `@WebMvcTest`, `MockMvc` |
| Spring Security Test | 6.x | Test des filtres de sécurité |
| JaCoCo | 0.8.12 | Couverture de code |
| JDK | 17 (Temurin 17.0.18+8) | Compilation et exécution |
