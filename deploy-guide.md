🚀 Guide de déploiement du projet
🧩 Prérequis

Avant de commencer le déploiement, assurez-vous d’avoir :

Une instance AWS EC2 fonctionnelle (par exemple, une instance Amazon Linux 2 ou Ubuntu).

Une clé d’accès SSH (.pem) associée à votre instance.

Docker et Docker Compose installés sur la machine distante.

Les fichiers du projet disponibles sur votre machine locale (le dossier appelé lionel dans cet exemple).

🔐 Étape 1 : Transférer les fichiers vers l’instance AWS

Sur votre machine locale, exécutez la commande suivante pour copier le dossier du projet vers votre instance AWS :

scp -i "your-key.pem" -r /chemin/vers/le/dossier/lionel ec2-user@<adresse-ip-aws>:/home/ec2-user/


Remplacez your-key.pem par le nom de votre clé privée.

Remplacez /chemin/vers/le/dossier/lionel par le chemin réel de votre dossier.

Remplacez <adresse-ip-aws> par l’adresse IP publique de votre instance AWS.

🖥️ Étape 2 : Se connecter à l’instance AWS

Une fois le transfert terminé, connectez-vous à votre instance EC2 avec SSH :

ssh -i "your-key.pem" ec2-user@<adresse-ip-aws>

🐳 Étape 3 : Installer Docker et Docker Compose

Sur l’instance AWS, installez Docker et Docker Compose si ce n’est pas déjà fait.

Installation de Docker
sudo yum update -y
sudo yum install docker -y
sudo service docker start
sudo usermod -aG docker ec2-user


Astuce : Déconnectez-vous puis reconnectez-vous pour que le groupe docker soit pris en compte.

Installation de Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
docker-compose version

🧱 Étape 4 : Lancer les services Docker

Accédez au dossier du projet copié sur la machine AWS :

cd ~/lionel


Pour chaque dossier contenant un fichier docker-compose.yml, exécutez la commande suivante :

docker-compose up -d


Cette commande démarre les conteneurs en arrière-plan.

🩺 Étape 5 : Vérifier le bon fonctionnement des conteneurs

Pour vérifier que vos conteneurs sont bien lancés, utilisez :

docker ps


Vous devriez voir la liste des conteneurs en cours d’exécution avec leurs noms, ports et statuts.

✅ Étape 6 : (Optionnel) Vérifier les logs

Si un service ne démarre pas correctement, affichez les logs avec :

docker-compose logs -f

🎯 Déploiement terminé !

Votre projet est maintenant déployé et fonctionnel sur votre instance AWS.
Vous pouvez accéder à vos services via l’adresse IP publique de l’instance.