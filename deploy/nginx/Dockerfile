# syntax=docker/dockerfile:1

# ===== Build Operator UI =====
FROM node:20-alpine AS build-operator
WORKDIR /app
COPY frontend/ui-operator/package*.json ./
RUN npm ci
COPY frontend/ui-operator/ ./
RUN npm run build -- --base=/operator/

# ===== Build Kiosk UI =====
FROM node:20-alpine AS build-kiosk
WORKDIR /app
COPY frontend/ui-kiosk/package*.json ./
RUN npm ci
COPY frontend/ui-kiosk/ ./
RUN npm run build -- --base=/kiosk/

# ===== Nginx runtime =====
FROM nginx:alpine
COPY deploy/nginx/nginx.conf /etc/nginx/conf.d/default.conf
RUN rm -rf /usr/share/nginx/html/*
COPY --from=build-operator /app/dist /usr/share/nginx/html/operator
COPY --from=build-kiosk /app/dist /usr/share/nginx/html/kiosk
