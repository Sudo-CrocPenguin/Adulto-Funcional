# Etapa 1: Construcción
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copiar archivos de Maven primero para aprovechar caché
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Descargar dependencias (esta capa se cachea)
RUN ./mvnw dependency:go-offline -B

# Copiar código fuente y construir
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# Etapa 2: Ejecución
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

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
