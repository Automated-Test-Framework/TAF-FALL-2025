#!/bin/sh

envsubst '$$BACKEND_URL $$AUTH_URL $$USER_URL' < /app/nginx.conf > /etc/nginx/conf.d/default.conf

exec nginx -g 'daemon off;'
