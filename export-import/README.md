# Module Import-Export

## Contexte
Le Module `Import-Export` s'occupe de gérer les transferts de données entre TAF et les autres plateformes/formats
disponibles. C'est un module Spring Boot à l'exterieur de l'application principale de TAF car ce module anticipe la migration de TAF vers une architecture en micro-services.

### Description

Pour le moment (automne 2025), seul la feature d'export de données vers TestRail est implémenté selon les critères suivants :

- Utilisation d'un controller avec un API exposé (voir `controllers/`)
- Utilisation d'une structure de données minimale (voir `models/`).
    - Seul les attributs nécessaires et les relations ont été ajouté en attente d'un format de données unifié à TAF.
- Mise en place de la logique de récupération intelligente des dépendances
    - Les parents d'un objet vont être créé dans TestRail s'ils n'existent pas
- Mise en place des tests unitaires et des tests d'intégration sur l'ensemble des méthodes

## Conception

- L'abstraction des `Exporters` assure l'extensibilité vers d'autres formats d'exportation (voir `utils/`)
- La séparation des responsabilités via l'architecture MVC permet d'ajouter des modules supplémentaires
    - L'importation de données peut être ajoutée et réalisée de manière similaire à l'export.

## Comment lancer le projet?
Il faut avoir Docker et Docker Desktop d'installé sur le poste. Tout le développement et les tests se font via les containers.
Ainsi, il n'est pas obligatoire d'avoir Java d'installé sur le poste, mais ça reste fortement recommandé.

Le projet se lance à l'aide du `docker-compose.yml` qui se trouve au même niveau que ce fichier.

Il faut avoir un fichier .env fonctionnel pour permettre à Spring de se connecter à l'API de TestRail (se reférer
à .env.example pour le format du fichier)

Pour lancer le projet + build les containers :
```
docker compose up --build -d
```

### La route /export

Faire un POST sur :
```
localhost:8080/export
```
Le body peut être complet ou partiel

### Body Complet
```
{
"type": "testrail",
"PROJECT": ["project1"],
"TEST_SUITE": ["suite1", "suite2"],
"TEST_CASE": [],
"TEST_RUN": [],
"TEST_RESULT": []
}
```
### Body Partiel
```
{
"type": "testrail",
"TEST_RESULT": ["result1"]
}
```
## Structure de données de TestRail
La structure de données dans TestRail est similaire à celle de TAF:

Project -> Test Suite -> Section (un créé par défaut) -> Test Case ET Test Run (au même niveau) -> Test Result

## Configuration TestRail

### Creation de compte
TestRail est un service de gestion de tests payant, mais qui offre des 'free trials' d'environ 30 jours. Cette durée devrait être suffisante pour le reste de la session du cours MGL805.

### API et la API key
Par défaut, l'instance TestRail n'accepte pas les appels API, il faut aller dans Admin Settings -> Site settings -> API
-> Enable API

Par la suite, il faut générer un clé API qui sera utilisée (au lieu du mot de passe) pour établir une connexion entre Spring et TestRail: My Settings (pas admin) -> API Keys -> Add Key (cette clé ne s'affiche qu'une seule fois, il faut la prendre en note)