FROM openjdk:17-alpine
WORKDIR /app
COPY app.jar app.jar
EXPOSE 8080
entrypoint: ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
