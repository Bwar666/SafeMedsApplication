FROM openjdk:24-slim

VOLUME /tmp

WORKDIR /app

COPY target/safemeds-backend-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]