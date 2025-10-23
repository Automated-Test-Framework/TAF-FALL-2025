# 🧭 Java Coding Standards & Best Practices

## 🎯 Objectif

Ce document définit les **bonnes pratiques de codage Java** à respecter
pour garantir une **uniformité**, une **qualité** et une
**maintenabilité** optimale du code au sein du projet.

------------------------------------------------------------------------

## 🧱 Structure du projet

-   Respecter la convention standard Maven :

        src/
         ├── main/
         │    ├── java/...
         │    └── resources/
         └── test/
              ├── java/...
              └── resources/

-   Chaque **module** doit être clairement nommé (`customer-service`,
    `order-service`, etc.).

-   Les **packages** doivent suivre une hiérarchie logique :

        com.company.project.module

    Exemple :

        org.testapi.restassuredtesting.service

------------------------------------------------------------------------

## 🧩 Nommage

  ------------------------------------------------------------------------------
Élément               Convention                  Exemple
  --------------------- --------------------------- ----------------------------
**Classe**            PascalCase                  `UserService`,
`PaymentController`

**Interface**         PascalCase + suffixe        `UserRepository`,
logique                     `Authenticable`

**Méthode**           camelCase                   `findAllUsers()`,
`calculateTotal()`

**Variable**          camelCase                   `totalPrice`, `userCount`

**Constante**         UPPER_SNAKE_CASE            `MAX_RETRY_COUNT`

**Package**           minuscules, sans underscore `restassuredtesting.model`
------------------------------------------------------------------------------

✅ **Éviter :** - `java.*` ou `javax.*` comme préfixes de package. -
Noms génériques (`data1`, `obj`, `test2`).

------------------------------------------------------------------------

## 🧰 Code Style & Formatage

-   Utiliser **4 espaces** pour l'indentation (pas de tabulations).

-   Longueur maximale des lignes : **120 caractères**.

-   Une seule classe publique par fichier.

-   Accolades `{}` toujours sur la **même ligne** que la déclaration :

    ``` java
    if (condition) {
        doSomething();
    } else {
        handleError();
    }
    ```

-   Laisser une **ligne vide** entre les méthodes.

------------------------------------------------------------------------

## 🧠 Bonnes pratiques de développement

-   Respecter les principes **SOLID** et **Clean Code**.

-   Toujours documenter les classes et méthodes publiques avec
    **JavaDoc** :

    ``` java
    /**
     * Calcule le montant total d'une commande.
     * @param order la commande à traiter
     * @return le total en dollars
     */
    public double calculateTotal(Order order) { ... }
    ```

-   Utiliser **Lombok** (`@Getter`, `@Setter`, `@Builder`, etc.) pour
    réduire le code boilerplate.

-   Ne jamais exposer de mot de passe, clé API ou secret dans le code
    source.

-   Toujours gérer les exceptions avec des messages clairs :

    ``` java
    try {
        ...
    } catch (IOException e) {
        log.error("Erreur lors de la lecture du fichier : {}", e.getMessage());
    }
    ```

-   Ne pas ignorer les exceptions (éviter `catch (Exception e) {}`
    vide).

------------------------------------------------------------------------

## 🧪 Tests unitaires (JUnit 5)

-   Un fichier de test pour chaque classe principale.

-   Utiliser **JUnit 5 (Jupiter)**.

-   Nommer les classes de test comme la classe testée, avec le suffixe
    `Test` :

        UserServiceTest

-   Nommer les méthodes de test de manière expressive :

        shouldReturnUser_whenValidId()
        shouldThrowException_whenUserNotFound()

-   Respecter la structure **Arrange / Act / Assert** :

    ``` java
    @Test
    void shouldReturnUser_whenValidId() {
        // Arrange
        User user = new User("Alice");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        User result = userService.findById(1L);

        // Assert
        assertEquals("Alice", result.getName());
    }
    ```

-   Utiliser les annotations modernes :

    ``` java
    @BeforeAll, @BeforeEach, @AfterEach, @AfterAll, @DisplayName, @ParameterizedTest
    ```

-   Éviter toute dépendance externe non mockée (`Mockito`, `MockMvc`,
    `@SpringBootTest` si nécessaire).

------------------------------------------------------------------------

## 🔐 Sécurité & Validation

-   Valider systématiquement les **entrées utilisateur** (`@Valid`,
    `@NotNull`, `@Size`, etc.).
-   Ne jamais **logger** des données sensibles (mot de passe, token,
    carte de crédit).
-   Utiliser **HTTPS** et des **tokens JWT** pour l'authentification.
-   Toujours mettre à jour les dépendances Maven critiques.

------------------------------------------------------------------------

## 🧭 Git & Commit

-   Convention de message de commit :

        [type]: description courte

    Types possibles :

    -   `feat`: nouvelle fonctionnalité
    -   `fix`: correction de bug
    -   `refactor`: amélioration du code
    -   `test`: ajout/mise à jour de tests
    -   `docs`: mise à jour de documentation

    Exemple :

        feat: add validation to login endpoint
        fix: resolve null pointer in PaymentService

------------------------------------------------------------------------

## 🧾 Checklist avant commit

-   [ ] Code compilé sans erreur
-   [ ] Tous les tests unitaires JUnit 5 passent
-   [ ] Pas d'imports inutiles (`Ctrl + Alt + O` sous IntelliJ)
-   [ ] Documentation JavaDoc présente
-   [ ] Méthodes \< 50 lignes
-   [ ] Pas de secret/mot de passe dans le code
-   [ ] Respect du style d'indentation et du nommage
-   [ ] Classes et packages correctement organisés
-   [ ] Utilisation de `log` au lieu de `System.out.println`

------------------------------------------------------------------------

## 🧭 Ressources utiles

-   [Google Java Style
    Guide](https://google.github.io/styleguide/javaguide.html)
-   [Spring Boot Best
    Practices](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#best-practices)
-   [JUnit 5 User
    Guide](https://junit.org/junit5/docs/current/user-guide/)
-   [Effective Java -- Joshua
    Bloch](https://www.oreilly.com/library/view/effective-java/9780134686097/)

------------------------------------------------------------------------

> 🧩 *L'objectif n'est pas seulement d'avoir du code fonctionnel, mais
> du code lisible, maintenable et cohérent à long terme.*
