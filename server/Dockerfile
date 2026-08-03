# Etapa 1: Construcción
FROM maven:3.9.13-eclipse-temurin-21-alpine@sha256:194053d8f204a39710e564b49ba4d22188159fd29f073b8da1715aac61503132 AS builder

WORKDIR /app

# Copiar archivos de Maven primero para aprovechar caché
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw

# Resolver solo las dependencias del proyecto. `dependency:go-offline` también
# descarga dependencias transitivas de todos los plugins declarados y hace que
# esta capa sea innecesariamente pesada.
RUN ./mvnw dependency:resolve -B

# Copiar código fuente, ejecutar pruebas unitarias y construir. Las pruebas de
# integración con Testcontainers se ejecutan con `./mvnw verify` fuera del
# build porque necesitan acceso explícito al daemon de Docker.
COPY src ./src
RUN ./mvnw clean package -B \
  -Dtest='!*IntegrationTest,!*HttpIntegrationTest,!AdultoFuncionalServerApplicationTests'

# Etapa 2: Ejecución
FROM eclipse-temurin:21-jre-alpine-3.23@sha256:3f08b13888f595cc49edabea7250ba69499ba25602b267da591720769400e08c

WORKDIR /app
RUN apk add --no-cache wget

# Crear usuario no root para seguridad
RUN addgroup -g 1000 appgroup && \
  adduser -u 1000 -G appgroup -D appuser

# Copiar el JAR desde la etapa de construcción
COPY --from=builder /app/target/*.jar app.jar

# Cambiar a usuario no root
USER appuser

# Puerto expuesto por la aplicación
EXPOSE 8080

# Healthcheck para monitoreo
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Punto de entrada
ENTRYPOINT ["java", "-jar", "app.jar"]
