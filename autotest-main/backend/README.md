# Autotest: Génération des Cas de Tests Automatiques

Autotest simplifie le processus de géneration des cas de tests avec l'intégration d'un modèle LLM. Dans notre cas nous utilisons Gemini 2.5 Flash. Les prochains lignes explique nos répertoires et dossiers sous notre root, "autotest-main". 

## 1. Backend

Le répertoires est composé de le dossier gemini.py, server_mistral.py, server.py et notre requirements.txt.

## Gemini.py:

Class EndpointInfo -> Il définit la structure des endpoints API 

Class ApiAnalysis -> Il définit la structure des contrôleurs API

Il continue en créant les modèles de prompts pour les deux: 

1. 'generate_basic_test'
2. 'enhance_test'

def generate_basic_test -> Le prompt est correctement converti en chaîne JSON pour être envoyé au LLM 

def enhance_tst -> Même fonctionnement que def generate_basic_test, mais appliqué au prompt destiné à améliorer le cas de test "de base" retourné par le LLM.

def generate_restassured_test -> Prendre toutes les fonctions dont nous avons parlé précédemment et donner un ordre précis pour la génération du meilleur cas de test.

## server_mistral.py

Une classe indépendante qui utilise le Mistral LLM et FastAPI pour générer les cas de test.

## server.py

Une classe indépendante qui utilis une Java API code et Flask pour générer les cas de tests RestAssured.

## requirements.txt

Définir tous les paquets externes de Python qui sont obligatorie pour exécuter Autotest

## Pour plus d'information, voir: ca.etsmtl.taf










