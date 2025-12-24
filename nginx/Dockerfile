# ---------- 1단계: React 빌드 ----------
FROM node:18-alpine AS build

WORKDIR /app
COPY frontend/package*.json ./
RUN npm install
COPY frontend .
RUN npm run build


# ---------- 2단계: nginx ----------
FROM nginx:alpine

RUN rm /etc/nginx/conf.d/default.conf
COPY nginx/conf.d /etc/nginx/conf.d
COPY --from=build /app/build /usr/share/nginx/html

EXPOSE 80