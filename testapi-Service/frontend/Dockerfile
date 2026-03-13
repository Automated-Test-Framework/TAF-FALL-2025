FROM node:alpine AS builder
WORKDIR /frontend

# Copy package files first — npm install cached when dependencies don't change
COPY frontend/package.json frontend/package-lock.json* ./
RUN npm install

# Copy source code and build
COPY frontend/ .
RUN npm run build

FROM nginx:alpine

ARG USER_ID=1001
ARG GROUP_ID=1001
ARG USER_NAME=nginx-user
ARG GROUP_NAME=nginx-group

# Create user and set up all directories/permissions in one layer
RUN addgroup -g $GROUP_ID -S $GROUP_NAME && \
    adduser -u $USER_ID -S $USER_NAME -G $GROUP_NAME -s /bin/sh && \
    mkdir -p /var/cache/nginx /app && \
    touch /var/run/nginx.pid && \
    chown -R "$USER_NAME":"$GROUP_NAME" /var/cache/nginx /var/run/nginx.pid /app /etc/nginx/conf.d

COPY --from=builder /frontend/dist/* /usr/share/nginx/html/
COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf
COPY frontend/nginx.conf /app/nginx.conf
COPY frontend/envsubst-file.sh /app/envsubst-file.sh

RUN sed -i 's/\r$//' /app/envsubst-file.sh && chmod +x /app/envsubst-file.sh && \
    chown -R "$USER_NAME":"$GROUP_NAME" /app /etc/nginx/conf.d

USER $USER_NAME

ENTRYPOINT ["/app/envsubst-file.sh"]
