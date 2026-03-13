# TAF - Conventions d'écriture

## TypeScript - Framework Angular
- Noms des fonctions et variables en CamelCase : `thisIsAVariable`
- Constantes en MAJUSCULE avec le mot clé `readonly` : `readonly VARIABLE_CONSTANTE: string = "const";`
- Commentaires : `// + phrase en anglais` → `// this is a comment`
- Code typé strictement
- Appels API définis dans les services
- Suivre les standards Angular : [Lien vers le guide de style Angular](https://angular.io/guide/styleguide)

## Java - Framework Spring
- Programmation orientée objet avec classes et interfaces
- Utiliser le modèle MVC proposé par Spring (controller, services, repositories etc)
- Noms des fonctions et variables en CamelCase : `thisIsAVariable`
- Noms des classes, interfaces et énumérations en PascalCase : `ThisIsAClass`
- Commentaires : `// + phrase en anglais` → `// this is a comment`
- Constantes en MAJUSCULE avec le mot clé `final` : `final String VARIABLE_CONSTANTE = "const";`
- Injection de dépendances de Spring, éviter d'utiliser 'new' pour instancier des dépendances
- Suivre les conventions de programmation Java et Spring
**Que ce soit pour le frontend ou le backend : les noms des variables, méthodes, classes, énumérations... doivent être explicites et définir le plus clairement possible leur fonction.**

## GIT Flow
- Message de commit sous la forme : "Action verb + élément concerné" écrit en anglais
- Utiliser en majorité ces action verbs : Add, Fix, Remove, Update, Improve (toujours au présent)
  -> `git commit -m "Add color enumeration"`
- Un commit par changement
- 3 types de branches :
  - Main : branche de la version stable actuellement en production
  - Develop : branche de la version en phase de test qui sert de référence aux branches feature
  - Feature : branche créée pour ajouter une feature au programme. On la nomme : `feature/userstory_id-résumé_feature`
- Fonctionnement : Pour créer une nouvelle feature il faut créer une branche depuis develop, une fois développée, il faut créer une pull request pour pouvoir merger la nouvelle feature sur develop

## Tests unitaires
- Framework : JUnit 5 (Jupiter) + Mockito + Spring Boot Test
- Fichiers de test dans `backend/src/test/java/`, structure miroir du code source
- Nommage des classes : `<ClasseTestée>Test.java` → `JwtUtilsTest.java`
- Nommage des méthodes : `<méthode>_<condition>_<résultatAttendu>` → `signin_badCredentials_throwsException`
- Chaque méthode de test doit avoir un `@DisplayName` en français ou anglais décrivant le scénario
- Utiliser `@WebMvcTest` pour les contrôleurs (évite de charger MongoDB)
- Utiliser `@ExtendWith(MockitoExtension.class)` pour les tests unitaires purs
- Utiliser `@MockitoBean` (Spring 6.2+) au lieu de `@MockBean` (déprécié)
- Lancer les tests : `mvn test -pl backend -am` ou `./run-tests-testapi.ps1`
- Couverture : JaCoCo, rapport dans `backend/target/site/jacoco/index.html`

