# =========================
# STAGE 1: build
# =========================
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

# =========================
# STAGE 2: runtime
# =========================
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/target/rendimento-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
