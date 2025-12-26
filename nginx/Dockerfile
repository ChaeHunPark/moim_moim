FROM node:18-alpine AS build
WORKDIR /app

COPY frontend/package*.json ./
RUN npm install
COPY frontend .
RUN npm run build


FROM nginx:alpine
RUN rm /etc/nginx/conf.d/default.conf
COPY nginx/conf.d /etc/nginx/conf.d
COPY --from=build /app/build /usr/share/nginx/html

EXPOSE 80
