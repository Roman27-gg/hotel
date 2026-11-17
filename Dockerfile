FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
COPY src src
RUN ./mvnw package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends libxext6 libx11-6 libxrender1 libxtst6 libxi6 xvfb x11-utils && rm -rf /var/lib/apt/lists/*
COPY --from=builder /app/target/hotel-0.0.1-SNAPSHOT.jar app.jar
COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh
ENV DB_URL=jdbc:postgresql://db:5432/hotel
ENV DB_USER=hotel
ENV DB_PASSWORD=hotel
ENV spring.main.web-application-type=none
ENV DISPLAY=:99
ENTRYPOINT ["/docker-entrypoint.sh"]
