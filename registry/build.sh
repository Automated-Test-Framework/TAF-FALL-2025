#!/bin/bash
source .env
REGISTRY_URL=$DOKCER_URL
USERNAME=$DOKCER_USER
PASSWORD=$DOKCER_PASWORD
REGISTRY=$REGISTRY_BASE

rm -rf ./build

bash gradlew bootJar

docker compose -f docker-compose-build.yml build

echo '>>>> PUSH Image REGISTRY_VERSION : ' REGISTRY_VERSION
docker login $REGISTRY_URL -u $USERNAME -p $PASSWORD
docker push ${REGISTRY_BASE}/lionel-test-registry:${REGISTRY_VERSION}