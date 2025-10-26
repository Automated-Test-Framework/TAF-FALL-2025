#!/bin/bash
source .env
REGISTRY_URL=$DOKCER_URL
USERNAME=$DOKCER_USER
PASSWORD=$DOKCER_PASWORD
REGISTRY=$REGISTRY_BASE

docker compose build backend
docker compose build frontend
docker compose build selenium
docker compose build testapi
docker compose build testui
docker compose build testuiteam4-server
docker compose build testuiteam4-client


docker login $REGISTRY_URL -u $USERNAME -p $PASSWORD
docker push ${REGISTRY}/lionel-test-team3-backend:${BACKEND_VERSION}
docker push ${REGISTRY}/lionel-test-team3-frontend:${FRONTEND_VERSION}
docker push ${REGISTRY}/lionel-test-team3-selenium:${SELENIUM_VERSION}
docker push ${REGISTRY}/lionel-test-team3-testapi:${TEST_API_VERSION}
docker push ${REGISTRY}/lionel-test-team3-testuiteam4-server:${TEST_API_VERSION}
docker push ${REGISTRY}/lionel-test-team3-testuiteam4-client:${TEST_API_VERSION}

