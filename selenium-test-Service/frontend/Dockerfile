FROM node:alpine AS builder
WORKDIR /frontend
COPY frontend .

RUN npm install && \
    npm run build

FROM nginx:alpine

ARG USER_ID=1001
ARG GROUP_ID=1001
ARG USER_NAME=nginx-user
ARG GROUP_NAME=nginx-group

# Create group and user with specified IDs
RUN addgroup -g $GROUP_ID -S $GROUP_NAME
RUN adduser -u $USER_ID -S $USER_NAME -G $GROUP_NAME -s /bin/sh

COPY --from=builder /frontend/dist/* /usr/share/nginx/html/
COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf

WORKDIR /app

COPY frontend/nginx.conf .
COPY frontend/envsubst-file.sh .

# Fix line endings and grant execute permissions to the entrypoint script
RUN sed -i 's/\r$//' /app/envsubst-file.sh && chmod +x /app/envsubst-file.sh

# Ensure the cache directory exists and set ownership
RUN mkdir -p /var/cache/nginx
RUN chown -R "$USER_NAME":"$GROUP_NAME" /var/cache/nginx

# Ensure the pid file exists and set ownership
RUN touch /var/run/nginx.pid
RUN chown -R "$USER_NAME":"$GROUP_NAME" /var/run/nginx.pid

# Set ownership of relevant directories/files to the non-root user
RUN chown -R "$USER_NAME":"$GROUP_NAME" /app/nginx.conf
RUN chown -R "$USER_NAME":"$GROUP_NAME" /app/envsubst-file.sh
RUN chown -R "$USER_NAME":"$GROUP_NAME" /etc/nginx/conf.d
RUN chown -R "$USER_NAME":"$GROUP_NAME" /usr/share/nginx/html

# Give write permissions to nginx config directory
RUN chmod -R u+w /etc/nginx/conf.d

USER $USER_NAME

ENTRYPOINT ["/app/envsubst-file.sh"]
