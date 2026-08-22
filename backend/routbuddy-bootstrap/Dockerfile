# Stage 1: Build Modular Monolith with Maven
FROM maven:3.9.8-eclipse-temurin-21-alpine AS builder

WORKDIR /app
COPY pom.xml .
COPY routbuddy-common/pom.xml routbuddy-common/
COPY routbuddy-auth/pom.xml routbuddy-auth/
COPY routbuddy-users/pom.xml routbuddy-users/
COPY routbuddy-drivers/pom.xml routbuddy-drivers/
COPY routbuddy-vehicles/pom.xml routbuddy-vehicles/
COPY routbuddy-routes/pom.xml routbuddy-routes/
COPY routbuddy-trips/pom.xml routbuddy-trips/
COPY routbuddy-bookings/pom.xml routbuddy-bookings/
COPY routbuddy-payments/pom.xml routbuddy-payments/
COPY routbuddy-subscriptions/pom.xml routbuddy-subscriptions/
COPY routbuddy-matching/pom.xml routbuddy-matching/
COPY routbuddy-locations/pom.xml routbuddy-locations/
COPY routbuddy-notifications/pom.xml routbuddy-notifications/
COPY routbuddy-ratings/pom.xml routbuddy-ratings/
COPY routbuddy-safety/pom.xml routbuddy-safety/
COPY routbuddy-complaints/pom.xml routbuddy-complaints/
COPY routbuddy-analytics/pom.xml routbuddy-analytics/
COPY routbuddy-admin/pom.xml routbuddy-admin/
COPY routbuddy-bootstrap/pom.xml routbuddy-bootstrap/

# Copy all source codes
COPY . .

RUN mvn clean package -DskipTests

# Stage 2: Runtime Image
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
RUN addgroup -S routbuddy && adduser -S routbuddy -G routbuddy
USER routbuddy

COPY --from=builder /app/routbuddy-bootstrap/target/routbuddy-bootstrap-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
