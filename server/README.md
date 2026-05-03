# Adulto Funcional Server

Backend construido con **Spring Boot 3.5.13** y **Java 21** que implementa una arquitectura limpia (Clean Architecture) para la gestión financiera personal, agenda de eventos y almacenamiento seguro de contraseñas.

## Características principales

- **Gestión financiera**: Movimientos (ingresos/egresos), gastos fijos recurrentes y categorías personalizadas
- **Agenda personal**: Eventos con prioridades, recordatorios, estados y recurrencia configurable
- **Gestor de contraseñas**: Almacenamiento seguro con encriptación AES-256 protegido por Master Key
- **Autenticación segura**: JWT + Argon2 para contraseñas y acceso al gestor de contraseñas
- **Identificadores únicos**: UUID v7 (ordenable temporalmente) para todas las entidades
- **Migraciones controladas**: Flyway para versionado de base de datos

## Stack tecnológico

| Tecnología           | Versión | Propósito                                          |
| -------------------- | ------- | -------------------------------------------------- |
| Java                 | 21      | Lenguaje base                                      |
| Spring Boot          | 3.5.13  | Framework principal                                |
| Spring Data JPA      | -       | Persistencia ORM                                   |
| Spring Security      | -       | Autenticación y autorización                       |
| MariaDB              | 11.8    | Base de datos relacional                           |
| Flyway               | -       | Migraciones de base de datos                       |
| Lombok               | -       | Reducción de boilerplate                           |
| JWT                  | -       | Autenticación stateless                            |
| Argon2               | -       | Hash de contraseñas                                |
| AES-256              | -       | Encriptación de contraseñas en gestor de seguridad |
| Testcontainers       | -       | Pruebas de integración                             |
| Spring Boot Actuator | -       | Health checks para Docker                          |
| Maven                | 3.9     | Gestión de dependencias                            |

## Arquitectura

El proyecto sigue los principios de **Clean Architecture** organizados en las siguientes capas:

```
org.adultofuncional.main
├── account/            # Módulo de cuentas de usuario
│   ├── domain/         # Modelo de dominio y puertos
│   ├── application/    # Casos de uso y DTOs
│   └── infrastructure/ # Adaptadores (JPA, REST)
├── auth/               # Módulo de autenticación (JWT)
├── config/             # Configuraciones de Spring (beans, jackson, security)
│   ├── beans/          # Configuración de beans de Spring
│   ├── jackson/        # Configuración de serialización JSON
│   └── security/       # Configuración de Spring Security
├── finances/           # Módulo financiero (movimientos, gastos, categorías)
├── agenda/             # Módulo de agenda (eventos)
├── security/           # Gestor de contraseñas con Master Key
└── shared/             # Componentes transversales
    ├── constants/      # Constantes globales del sistema
    ├── exception/      # Jerarquía de excepciones y GlobalExceptionHandler
    ├── response/       # Formato estándar de respuestas API (ApiResponse)
    └── util/           # Clases de utilidad general
```

Para una documentación técnica detallada de la arquitectura, consulta [ARCHITECTURE.md](./ARCHITECTURE.md).

## Estructura de la base de datos

El esquema se gestiona mediante Flyway (`src/main/resources/database/migrations/`):

- **accounts** - Cuentas de usuario (UUID v7, email único, hash Argon2)
- **categories** - Categorías para clasificar (soporta soft delete)
- **movements** - Movimientos financieros (ingresos/egresos)
- **fixed_expenses** - Gastos fijos recurrentes
- **events** - Eventos de agenda con recordatorios
- **passwords** - Contraseñas encriptadas con AES-256

Todas las tablas usan `CHAR(36)` para UUID v7 y relaciones con llaves foráneas con eliminación en cascada desde `accounts`.

Para la documentación detallada del esquema, columnas, índices y notas de seguridad, consulta [DATABASE.md](./DATABASE.md).

## Requisitos previos

- Java 21 JDK
- Maven 3.9+
- Docker y Docker Compose (para despliegue en contenedores)
- MariaDB 11.8+ (para ejecución local sin Docker)

## Ejecución local

### 1. Clonar el repositorio

```bash
git clone <repository-url>
cd adulto-funcional-server
```

### 2. Configurar variables de entorno

Crea un archivo `.env` en la raíz del proyecto (no se incluye en el repositorio):

```env
# Configuración de Spring
SPRING_APPLICATION_NAME=adulto-funcional
SERVER_PORT=8080
SERVER_ADDRESS=0.0.0.0

# Base de datos
SPRING_DATASOURCE_URL=jdbc:mariadb://localhost:3306/adulto_funcional
SPRING_DATASOURCE_USERNAME=afs_user
SPRING_DATASOURCE_PASSWORD=tu_password
MARIADB_ROOT_PASSWORD=tu_root_password
MARIADB_DATABASE=adulto_funcional
MARIADB_USER=afs_user
MARIADB_PASSWORD=tu_password

# JPA
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
SPRING_JPA_SHOW_SQL=false
SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT=org.hibernate.dialect.MariaDBDialect

# Flyway
SPRING_FLYWAY_ENABLED=true
SPRING_FLYWAY_LOCATIONS=classpath:database/migrations
SPRING_FLYWAY_BASELINE_ON_MIGRATE=true
SPRING_FLYWAY_VALIDATE_ON_MIGRATE=true

# JWT
JWT_SECRET=tu_clave_secreta_jwt_muy_segura
JWT_EXPIRATION=3600000

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000

# HttpOnly Cookie
COOKIE_SECURE=false # true en producción con HTTPS
APP_COOKIE_SECURE=false # true en producción
APP_COOKIE_SAME_SITE=None # Lax en producción
```

O utilizar la plantilla del proyecto en lugar de crear el archivo manualmente

```bash
cp .env.example .env
```

**Nota**: Spring Boot NO lee automáticamente archivos `.env`. Para ejecución local sin Docker, debes exportar las variables de entorno manualmente antes de iniciar la aplicación:

```bash
# Exportar todas las variables del .env al entorno actual
export $(cat .env | xargs)

# Verificar que se cargaron (opcional)
echo $MARIADB_PASSWORD
echo $JWT_SECRET
```

### 3. Ejecutar con Maven

```bash
# Compilar el proyecto
./mvnw clean package -DskipTests

# Ejecutar la aplicación
java -jar target/adulto-funcional-server-0.0.1-SNAPSHOT.jar
```

O directamente con Spring Boot Maven plugin:

```bash
./mvnw spring-boot:run
```

## Testing

El proyecto utiliza **JUnit 5**, **Spring Boot Test**, **Mockito** y **Testcontainers** para pruebas automatizadas.

### Tipos de tests

**1. Test de arranque (`AdultoFuncionalServerApplicationTests`)**

- Levanta un contenedor Docker con **MariaDB 11.8** usando Testcontainers
- Configura dinámicamente las propiedades del DataSource vía `@DynamicPropertySource`
- Verifica que el contexto de Spring Boot se carga sin errores
- Flyway se deshabilita para un arranque rápido

**2. Tests de integración web**

- Usa `@WebMvcTest` para cargar solo la capa web (controladores, validación)
- Simula los casos de uso con Mockito (`@MockitoBean`)
- Deshabilita la seguridad con `TestSecurityConfig` (`@Import`)
- Verifica respuestas HTTP, códigos de estado y validación de DTOs

### Comandos de test

```bash
# Ejecutar todos los tests
./mvnw test

# Ejecutar tests con reporte detallado
./mvnw test -Dtest.verbose=true

# Ejecutar un test específico
./mvnw test -Dtest=AdultoFuncionalServerApplicationTests

# Ejecutar tests de un paquete específico
./mvnw test -Dtest=org.adultofuncional.main.account.infrastructure.controller.*

# Verificar y ejecutar tests de integración (Testcontainers requiere Docker)
./mvnw verify

# Ejecutar tests saltando la compilación
./mvnw surefire:test
```

**Nota**: Los tests que usan Testcontainers requieren Docker instalado y en ejecución.

## Ejecución con Docker

### 1. Construir y levantar los servicios

```bash
# Construir la imagen y levantar los contenedores
docker-compose up -d --build

# Ver logs de la aplicación
docker-compose logs -f app

# Ver logs de la base de datos
docker-compose logs -f mariadb
```

### 2. Verificar que los servicios estén saludables

```bash
# Health check de la aplicación
curl http://localhost:8080/actuator/health

# Health check de MariaDB
docker-compose ps
```

### 3. Detener los servicios

```bash
docker-compose down

# Para eliminar también los volúmenes (¡borra los datos!)
docker-compose down -v
```

## Comandos útiles de Maven

```bash
# Limpiar y compilar
./mvnw clean compile

# Ejecutar pruebas
./mvnw test

# Ejecutar pruebas de integración con Testcontainers
./mvnw verify

# Empaquetar sin pruebas
./mvnw clean package -DskipTests

# Ejecutar con perfil específico
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Ver dependencias
./mvnw dependency:tree

# Actualizar versiones de dependencias
./mvnw versions:display-dependency-updates

# Ejecutar migraciones de Flyway manualmente
./mvnw flyway:migrate
```

## Comandos útiles de Docker

```bash
# Construir la imagen manualmente
docker build -t adulto-funcional-server .

# Ejecutar contenedor de la aplicación
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mariadb://localhost:3306/adulto_funcional \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=password \
  -e JWT_SECRET=secret \
  -e JWT_EXPIRATION=3600000 \
  -e CORS_ALLOWED_ORIGINS=http://localhost:3000 \
  -e COOKIE_SECURE=false \
  -e APP_COOKIE_SECURE=false \
  -e APP_COOKIE_SAME_SITE=None \
  adulto-funcional-server

# Entrar al contenedor de la aplicación
docker-compose exec app sh

# Entrar al contenedor de MariaDB
docker-compose exec mariadb mariadb -u root -p

# Ver logs en tiempo real
docker-compose logs -f --tail=100

# Reiniciar un servicio específico
docker-compose restart app
```

## API Endpoints

### Cuentas (`/api/account`)

- `GET /api/account/{id}` - Obtener datos de una cuenta (requiere autenticación + ownership)
- `PATCH /api/account/{id}` - Actualizar datos de una cuenta (requiere autenticación + ownership)
- `DELETE /api/account/{id}` - Eliminar una cuenta (endpoint existe, lógica pendiente — retorna 204 sin ejecutar delete)

### Autenticación (`/api/auth`)

- `POST /api/auth/login` - Iniciar sesión (JWT en HttpOnly cookie)
- `POST /api/auth/register` - Registrar usuario (JWT en HttpOnly cookie)
- `POST /api/auth/logout` - Cerrar sesión (limpia cookie)

### Health Check

- `GET /actuator/health` - Estado de la aplicación (público, usado por Docker)

## Formato de respuesta estándar

Todas las respuestas de la API siguen el formato `ApiResponse<T>`:

```json
{
  "status": 200,
  "message": "Operación exitosa",
  "data": { ... }
}
```

## Manejo de errores

El sistema usa una jerarquía de excepciones centralizada:

| Excepción               | HTTP Status | Descripción                              |
| ----------------------- | ----------- | ---------------------------------------- |
| `BusinessException`     | 400         | Errores de negocio generales             |
| `NotFoundException`     | 404         | Recurso no encontrado                    |
| `UnauthorizedException` | 401         | Credenciales incorrectas                 |
| `ConflictException`     | 409         | Conflicto de datos (ej. email duplicado) |
| `ForbiddenException`    | 403         | Acceso denegado (Master Key requerida)   |

## Documentación Javadoc

El código fuente está documentado con Javadoc siguiendo las convenciones estándar. Para generar la documentación:

```bash
./mvnw javadoc:javadoc
# O con el plugin de JDK
javadoc -d docs -sourcepath src/main/java -subpackages org.adultofuncional.main
```

Los archivos `package-info.java` documentan la arquitectura y propósito de cada paquete.

## Autores

- Jeronimo Ospina
- Miguel Angel Blandon Montes
- Lydis Jaraba
- Daniel Salazar
- Juan Sebastian Rios

## Licencia

Este proyecto está bajo licencia propietaria. Todos los derechos reservados.

## Estado del proyecto

En desarrollo activo. Estado por módulo:

| Módulo             | Estado     | Detalle                                                          |
| ------------------ | ---------- | ---------------------------------------------------------------- |
| Autenticación      | Completado | Login, registro, logout con JWT en HttpOnly cookie               |
| Cuentas            | Parcial    | GET y PATCH funcionales; DELETE pendiente de implementar lógica  |
| Financiero         | Pendiente  | Solo entidades JPA definidas (Category, Movement, FixedExpenses) |
| Agenda             | Pendiente  | Solo entidad JPA definida (Event)                                |
| Gestor contraseñas | Pendiente  | Solo entidad JPA definida (Password); AES-256 sin implementar    |

**Próximos pasos**: Implementar `DeleteAccountUseCase`, módulo financiero, módulo de agenda y servicio de encriptación AES-256 para el gestor de contraseñas.
