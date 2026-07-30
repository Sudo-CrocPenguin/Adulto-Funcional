# Adulto Funcional

Aplicación cliente-servidor para organizar la vida personal: finanzas, agenda, cuentas de usuario y gestor de contraseñas cifrado. El repositorio está organizado como un monorepo con backend REST, base de cliente web y aplicación móvil.

## Descripción

Adulto Funcional centraliza tareas cotidianas de gestión personal en una API segura y modular. El backend implementa autenticación, manejo de cuentas, registro de ingresos y egresos, gastos fijos recurrentes, eventos de agenda y almacenamiento de credenciales protegidas con Master Key.

El objetivo del proyecto es ofrecer una base mantenible para clientes web y móviles, separando reglas de negocio, casos de uso e infraestructura mediante Clean Architecture.

## Características

- Autenticación con JWT y hash de contraseñas con Argon2.
- Gestión de cuentas de usuario con validación de ownership.
- Finanzas personales: movimientos, categorías y gastos fijos.
- Agenda personal: eventos con prioridad, estado, recordatorios y recurrencia.
- Gestor de contraseñas con cifrado AES-256 y verificación de Master Key.
- Persistencia relacional con MariaDB y migraciones versionadas con Flyway.
- Sesiones de Master Key en memoria para desarrollo y Redis para producción.
- Respuestas API estandarizadas y manejo global de excepciones.
- Validación anti-XSS en campos de entrada mediante anotación `@NoHtml`.
- Identificadores UUID v7 para entidades principales.

## Stack

| Capa | Tecnologías |
| --- | --- |
| Backend | Java 21, Spring Boot 3, Spring Web, Spring Security, Spring Data JPA |
| Seguridad | JWT, Argon2, AES-256, cookies HttpOnly, validación anti-XSS |
| Base de datos | MariaDB 11.8, Flyway |
| Sesiones | Redis en producción, almacenamiento en memoria en desarrollo |
| Testing | JUnit 5, Spring Boot Test, Mockito, Testcontainers |
| Cliente web | React, TypeScript, Vite |
| Cliente móvil | Expo, React Native, Expo Router, TypeScript |
| DevOps | Docker, Docker Compose, Maven Wrapper |

## Estructura

```text
.
├── server/   # API REST Spring Boot y documentación técnica
├── web/      # Base/documentación del cliente web React + Vite
└── movil/    # Aplicación móvil Expo + React Native
```

### Backend

El backend sigue Clean Architecture:

```text
org.adultofuncional.main
├── account/    # Cuentas de usuario
├── auth/       # Login, registro, logout y emisión de JWT
├── finances/   # Movimientos, categorías y gastos fijos
├── agenda/     # Eventos y recordatorios
├── security/   # Gestor de contraseñas y Master Key
├── config/     # Beans, filtros JWT y configuración de seguridad
└── shared/     # Respuestas, excepciones y utilidades transversales
```

Cada módulo separa dominio, casos de uso e infraestructura para mantener las reglas de negocio independientes de Spring, JPA y HTTP.

## API principal

La API expone sus rutas bajo `/api`:

| Módulo | Endpoints |
| --- | --- |
| Autenticación | `POST /api/auth/register`, `POST /api/auth/login`, `POST /api/auth/logout` |
| Cuenta | `GET /api/account/{id}`, `PATCH /api/account/{id}`, `DELETE /api/account/{id}` |
| Finanzas | CRUD de `/api/finances/movements`, `/api/finances/categories` y `/api/finances/fixed-expenses` |
| Agenda | CRUD de `/api/agenda/events` |
| Contraseñas | `POST /api/security/passwords/master-key/verify` y CRUD de `/api/security/passwords` |

Las respuestas se devuelven con una estructura común:

```json
{
  "status": 200,
  "message": "Operación exitosa",
  "data": {}
}
```

## Requisitos

- Java 21.
- Docker y Docker Compose.
- MariaDB 11.8 o superior si se ejecuta sin contenedores.
- Node.js y npm para el cliente móvil.

## Ejecución del backend en desarrollo

```bash
cd server
cp src/main/resources/application-dev.yml.example src/main/resources/application-dev.yml
```

Edita `src/main/resources/application-dev.yml` con la URL de MariaDB, usuario, contraseña y secreto JWT.

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Por defecto la API queda disponible en:

```text
http://localhost:8080
```

## Ejecución con Docker

```bash
cd server
cp .env.example .env
```

Completa las variables de `.env`. El `docker-compose.yml` usa una red externa llamada `coolify`; si no existe en tu entorno local, créala antes de levantar los servicios:

```bash
docker network create coolify
docker compose up -d --build
```

Servicios incluidos:

- `app`: API Spring Boot.
- `mariadb`: base de datos relacional.
- `redis`: almacenamiento de sesiones de Master Key en producción.

## Cliente móvil

```bash
cd movil
npm install
npm start
```

Scripts disponibles:

- `npm run android`
- `npm run ios`
- `npm run web`

## Testing

```bash
cd server
./mvnw test
```

Algunas pruebas usan Testcontainers, por lo que requieren Docker en ejecución.

## Documentación técnica

- [Arquitectura del backend](server/ARCHITECTURE.md)
- [Esquema de base de datos](server/DATABASE.md)
- [README del servidor](server/README.md)
- [README del cliente web](web/README.md)

## Seguridad

- Las contraseñas de acceso se almacenan con hash Argon2.
- Las credenciales del gestor se cifran con AES-256.
- La Master Key no se persiste como texto plano en la base de datos.
- En producción, Redis mantiene sesiones temporales de Master Key con TTL.
- Los clientes web reciben el JWT por cookie `HttpOnly`; clientes nativos pueden recibirlo también en el cuerpo de la respuesta.
- La API aplica validaciones de entrada y defensas contra XSS almacenado.

## Estado del proyecto

El backend contiene la implementación principal y documentación técnica completa. El cliente móvil usa Expo Router como base de desarrollo y el cliente web está planteado para consumir la API REST de finanzas, agenda y seguridad.

## Licencia

Este proyecto incluye licencias en los módulos `server` y `web`. Revisa los archivos `LICENSE` correspondientes antes de reutilizar o distribuir el código.
