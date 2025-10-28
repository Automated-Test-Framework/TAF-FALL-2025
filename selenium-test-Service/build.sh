#!/bin/bash
source .env
REGISTRY_URL=$DOKCER_URL
USERNAME=$DOKCER_USER
PASSWORD=$DOKCER_PASWORD
REGISTRY=$REGISTRY_BASE

docker compose -f docker-compose-build.yml build backend
docker compose -f docker-compose-build.yml build frontend
docker compose -f docker-compose-build.yml build selenium
docker compose -f docker-compose-build.yml build testapi
docker compose -f docker-compose-build.yml build testui
docker compose -f docker-compose-build.yml build testuiteam4-server
docker compose -f docker-compose-build.yml build testuiteam4-client

echo '>>>> PUSH Image API_GATEWAY_VERSION : ' $API_GATEWAY_VERSION
docker login $REGISTRY_URL -u $USERNAME -p $PASSWORD
docker push ${REGISTRY}/lionel-test-team1-backend:${BACKEND_VERSION}
docker push ${REGISTRY}/lionel-test-team1-frontend:${FRONTEND_VERSION}
docker push ${REGISTRY}/lionel-test-team1-selenium:${SELENIUM_VERSION}
docker push ${REGISTRY}/lionel-test-team1-testapi:${TEST_API_VERSION}
docker push ${REGISTRY}/lionel-test-team1-testui:${TEST_UI_VERSION}
docker push ${REGISTRY}/lionel-test-team1-testui-team4-server:${TEST_TEAM4_SERVER_VERSION}
docker push ${REGISTRY}/lionel-test-team1-testui-team4-client:${TEST_TEAM4_CLIENT_VERSION}