# operator build
FROM node:20-alpine AS build-operator
WORKDIR /app
COPY frontend/ui-operator/package*.json ./
RUN npm ci
COPY frontend/ui-operator ./
RUN npm run build -- --base=/operator/

# kiosk build
FROM node:20-alpine AS build-kiosk
WORKDIR /app
COPY frontend/ui-kiosk/package*.json ./
RUN npm ci
COPY frontend/ui-kiosk ./
RUN npm run build -- --base=/kiosk/

# nginx
FROM nginx:alpine
COPY springboot/docker/app/nginx.conf /etc/nginx/conf.d/default.conf
RUN rm -rf /usr/share/nginx/html/*
COPY --from=build-operator /app/dist /usr/share/nginx/html/operator
COPY --from=build-kiosk /app/dist /usr/share/nginx/html/kiosk


