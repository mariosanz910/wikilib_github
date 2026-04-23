# ===== FASE 1: BUILD =====
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copiamos pom primero para aprovechar cache
COPY pom.xml .

# Descarga dependencias
RUN mvn dependency:go-offline

# Copiamos el código
COPY src ./src

# Construimos el JAR
RUN mvn clean package -DskipTests


# ===== FASE 2: RUNTIME =====
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copiamos el jar generado
COPY --from=build /app/target/*.jar app.jar

# Puerto típico de Spring Boot
EXPOSE 8085

# Ejecutar la app
ENTRYPOINT ["java", "-jar", "app.jar"]
