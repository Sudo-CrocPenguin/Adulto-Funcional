# Adulto Funcional Server

Backend de Adulto Funcional construido con Java 21 y Spring Boot 3.5.13. Es un
monolito modular orientado a dominio que concentra cuentas, autenticación,
finanzas personales, agenda y una bóveda de credenciales cifradas.

## Qué es, para qué sirve y cómo funciona

**Qué es:** una API REST con persistencia en MariaDB, estado efímero de
seguridad en Redis, migraciones Flyway y contratos JSON uniformes.

**Para qué sirve:** permite que cada cuenta administre de forma aislada su
perfil, movimientos, gastos recurrentes, eventos, categorías personales y
credenciales protegidas por una Master Key independiente.

**Cómo funciona:** cada petición atraviesa CORS, trazabilidad, límite de cuerpo,
autenticación JWT y CSRF cuando corresponde. Los controladores delegan en casos
de uso; estos aplican reglas de dominio y acceden a persistencia mediante
puertos. Los adaptadores JPA limitan los recursos privados por la combinación
`resourceId + accountId` y Flyway mantiene el esquema reproducible.

```text
Cliente web o nativo
        │
        ▼
CORS → traceId → límite HTTP → JWT/CSRF → Controller
                                            │
                                            ▼
                                      Caso de uso
                                            │
                                            ▼
                                      Dominio + puerto
                                            │
                              ┌─────────────┴─────────────┐
                              ▼                           ▼
                       Adaptador JPA                 Redis efímero
                              │                  (revocación, límites,
                              ▼                    Master Key de sesión)
                         MariaDB 11.8
```

## Capacidades actuales

- Cuentas con perfil, actualización parcial, unicidad de email, control
  optimista y eliminación reautenticada.
- Login y registro con Argon2, JWT de corta duración, refresh token rotativo,
  sesiones persistidas, revocación actual o global y detección de replay.
- Autenticación web mediante cookies `HttpOnly` y autenticación nativa mediante
  `Authorization: Bearer`.
- Protección CSRF para operaciones autenticadas por cookie, CORS con orígenes
  explícitos y respuestas de seguridad uniformes.
- Movimientos y gastos fijos con importes `DECIMAL(10,2)`, categorías
  obligatorias, filtros y paginación ejecutados en SQL.
- Categorías `SYSTEM` compartidas y categorías `PERSONAL` aisladas por cuenta,
  con nombre normalizado y tipo inmutable.
- Eventos con invariantes completas, zona IANA, tiempo civil e instantes UTC.
- Bóveda con Master Key aislada por sesión, AES-256-GCM, PBKDF2-HMAC-SHA256,
  formato criptográfico versionado y AAD ligado a cuenta y credencial.
- Rate limiting distribuido en producción para autenticación y operaciones
  criptográficas.
- Errores estables con `code`, `fieldErrors` y `traceId`; correlación del mismo
  identificador en logs.
- Scheduler transaccional que avanza los vencimientos de gastos fijos activos
  en lotes bloqueados.
- IDs UUID v7, timestamps técnicos UTC, `Clock` inyectado y bloqueo optimista
  con `@Version`.

## Estado verificable

La referencia de esta entrega es:

| Indicador | Resultado |
|---|---:|
| Fuentes Java | 269 |
| Clases de prueba | 52 |
| Endpoints REST propios | 41 |
| Migraciones Flyway | 14 |
| Entidades con `@Version` | 7 |
| Regresión `./mvnw clean verify` | 137 pruebas, 0 fallos |
| Base de integración | MariaDB 11.8.8 mediante Testcontainers |

El healthcheck público está en `GET /actuator/health`.

## Módulos

| Módulo | Responsabilidad |
|---|---|
| `account` | Perfil, ownership, actualización y eliminación de cuenta |
| `auth` | Registro, login, roles, sesiones, refresh y revocación |
| `finances` | Movimientos, categorías y gastos fijos |
| `agenda` | Eventos, reglas temporales y zonas horarias |
| `security` | Master Key y bóveda de credenciales |
| `shared` | Errores, paginación, validación, rate limiting y observabilidad |
| `config` | Beans, cookies, JWT, CORS, CSRF y cadena de seguridad |

La arquitectura detallada está en [ARCHITECTURE.md](./ARCHITECTURE.md).

## Stack

| Tecnología | Uso |
|---|---|
| Java 21 | Lenguaje y runtime |
| Spring Boot 3.5.13 | API, inyección, configuración y Actuator |
| Spring Security | JWT, autorización, CORS, CSRF y headers |
| Spring Data JPA / Hibernate | Persistencia y control optimista |
| MariaDB 11.8.8 | Datos durables |
| Flyway | Evolución y validación del esquema |
| Redis 7.4.7 | Estado de seguridad efímero y distribuido |
| Argon2 | Hash de contraseña de cuenta y Master Key |
| AES-256-GCM | Cifrado autenticado de credenciales |
| Testcontainers | Integración contra MariaDB real |
| Maven Wrapper 3.9 | Build reproducible |
| Docker Compose | Ejecución local y despliegue |

Las imágenes de Docker están fijadas por versión y digest.

## Requisitos

- JDK 21 para ejecución local.
- Docker con Compose v2 para el stack completo y las pruebas de integración.
- No es obligatorio instalar Maven: el repositorio incluye `./mvnw`.
- Puertos libres: `8080` para la API. MariaDB y Redis no se publican al host en
  el Compose base.

## Inicio rápido con Docker

El Compose usa el perfil `prod`; por ello valida secretos y cookies al arrancar.

```bash
cp .env.example .env
```

Completa `.env`. Genera **por separado** `JWT_SECRET`,
`MASTER_KEY_SESSION_SECRET` y `REDIS_PASSWORD`:

```bash
openssl rand -base64 48
```

Usa también contraseñas diferentes y fuertes para MariaDB. Después ejecuta:

```bash
docker compose up -d --build
docker compose ps
curl http://localhost:8080/actuator/health
```

Respuesta esperada:

```json
{"status":"UP"}
```

El perfil `prod` obliga a `APP_COOKIE_SECURE=true`. Para autenticar un navegador
se necesita HTTPS —normalmente terminado por un proxy inverso—. El acceso HTTP
directo a `localhost:8080` es adecuado para el healthcheck; para desarrollo web
sin TLS usa el perfil `dev`.

Para detener sin borrar datos:

```bash
docker compose down
```

`docker compose down -v` elimina el volumen de MariaDB y sus datos; no debe
usarse como comando rutinario.

## Desarrollo local

1. Crea la configuración local:

   ```bash
   cp src/main/resources/application-dev.yml.example \
      src/main/resources/application-dev.yml
   ```

2. Configura MariaDB y un secreto JWT de al menos 32 bytes. Mantén:

   ```yaml
   APP_COOKIE_SECURE: false
   APP_COOKIE_SAME_SITE: Lax
   CORS_ALLOWED_ORIGINS: http://localhost:5173
   ```

3. Inicia la aplicación:

   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```

La API queda disponible en http://localhost:8080. El perfil `dev` utiliza
implementaciones en memoria con TTL para revocación, rate limiting y sesiones
de Master Key; los datos de negocio continúan en MariaDB.

Ejecutar sin perfil no es un modo soportado porque no define datasource ni
adaptadores de estado efímero.

## Autenticación de clientes

### Navegador

1. Solicita `GET /api/auth/csrf` y conserva cookies.
2. Envía el token recibido en el header `X-XSRF-TOKEN` para operaciones no
   seguras autenticadas por cookie.
3. `login` y `register` crean las cookies `token` y `refresh_token` como
   `HttpOnly`; los tokens no aparecen en el body.
4. `POST /api/auth/refresh` rota el refresh token.
5. `POST /api/auth/logout` o `DELETE /api/auth/sessions/current` revoca la
   sesión actual.

### Cliente nativo

El cliente debe enviar `X-Client-Type: mobile` o `desktop` y una señal pasiva
compatible (por ejemplo, un User-Agent nativo). Login, registro y refresh
devuelven access y refresh token en el body. Las peticiones protegidas usan:

```http
Authorization: Bearer <access-token>
```

Un Bearer válido no requiere CSRF. Los detalles y ejemplos completos están en
[docs/API_REFERENCE.md](./docs/API_REFERENCE.md).

## Respuestas HTTP

Una operación exitosa usa el sobre histórico:

```json
{
  "status": 200,
  "message": "Operación exitosa",
  "data": {}
}
```

Los listados añaden `page` sin cambiar `data`:

```json
{
  "status": 200,
  "message": "Movimientos listados exitosamente",
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 42,
    "totalPages": 3,
    "hasNext": true,
    "hasPrevious": false
  },
  "data": []
}
```

Un error usa código estable y trazabilidad:

```json
{
  "status": 400,
  "code": "VALIDATION_FAILED",
  "message": "La solicitud contiene datos inválidos",
  "fieldErrors": [],
  "traceId": "4c1f5a5e1fc34e0e",
  "data": null
}
```

Consulta [docs/API_ERROR_CONTRACT.md](./docs/API_ERROR_CONTRACT.md) para la
matriz completa.

## Paginación, tiempo y dinero

- `page` inicia en cero, `size` vale 20 por defecto y admite máximo 100.
- `sortDirection` acepta `ASC` o `DESC`; cada endpoint limita `sortBy` a una
  lista segura.
- Filtros, orden, conteo y límites se ejecutan en SQL. El UUID actúa como
  desempate determinista.
- Fechas de negocio usan `LocalDate`; timestamps técnicos usan `Instant` UTC.
- Los eventos conservan zona IANA y horas civiles, además de sus instantes UTC.
- Los importes deben ser positivos y caber sin redondeo en `DECIMAL(10,2)`:
  máximo ocho enteros y dos decimales.

## Master Key y bóveda

La contraseña de la cuenta autentica a la persona; la Master Key autoriza la
bóveda. Son secretos diferentes.

1. La cuenta configura o verifica su Master Key.
2. El servidor almacena solo su hash Argon2 en MariaDB.
3. Durante el desbloqueo, Redis recibe una copia cifrada con un secreto de
   sesión y TTL; la clave Redis incluye `accountId + sessionId`.
4. Cada credencial usa salt e IV propios. El formato actual aplica
   PBKDF2-HMAC-SHA256 con 600.000 iteraciones, AES-256-GCM y AAD.
5. Cambiar la Master Key recifra la bóveda dentro de una transacción.
6. Cerrar o revocar la sesión elimina su desbloqueo.

Los listados nunca incluyen la contraseña descifrada. Solo
`GET /api/security/passwords/{id}` la devuelve y la respuesta lleva
`Cache-Control: no-store`.

## Pruebas y calidad

```bash
# Compilación, unitarias e integración con Testcontainers
./mvnw clean verify

# Una clase concreta
./mvnw test -Dtest=EventTest

# Árbol de dependencias
./mvnw dependency:tree

# SCA; requiere red y descarga inicial de la base de vulnerabilidades
./mvnw -Psecurity-scan verify

# Javadoc temporal en server/doc/apidocs
./mvnw javadoc:javadoc
```

El build de Docker ejecuta las pruebas unitarias. Las pruebas que requieren
Testcontainers se ejecutan fuera del build porque necesitan acceso al daemon de
Docker.

## Migraciones

Flyway valida y aplica V1–V14 al iniciar. Hibernate usa `ddl-auto=validate`: no
crea ni modifica el esquema. Nunca edites una migración ya aplicada; añade una
nueva versión.

```bash
SPRING_DATASOURCE_URL=jdbc:mariadb://localhost:3306/adulto_funcional \
SPRING_DATASOURCE_USERNAME=usuario \
SPRING_DATASOURCE_PASSWORD=secreto \
SPRING_FLYWAY_LOCATIONS=filesystem:src/main/resources/database/migrations \
./mvnw flyway:validate
```

El esquema, restricciones e índices están descritos en
[DATABASE.md](./DATABASE.md).

## Despliegue

- `docker-compose.yml` es la base local: publica solo `127.0.0.1:8080` por
  defecto y mantiene MariaDB/Redis en una red interna.
- `docker-compose.coolify.yml` añade la red externa y etiquetas necesarias para
  el proxy de Coolify:

  ```bash
  docker compose -f docker-compose.yml \
    -f docker-compose.coolify.yml up -d --build
  ```

- Producción falla rápido ante secretos de ejemplo, secretos criptográficos que
  no sean Base64 aleatorio o cookies sin `Secure`.
- `baseline-on-migrate` está desactivado para no aceptar silenciosamente una
  base no vacía sin historial.

La guía de operación, backups, rotación y diagnóstico está en
[docs/OPERATIONS.md](./docs/OPERATIONS.md).

## Alcance operativo conocido

- La recurrencia y el recordatorio de eventos se validan y almacenan, pero el
  backend no integra todavía un canal externo de notificaciones.
- El scheduler de gastos fijos avanza `nextDueDate`; no crea movimientos de
  cobro automáticamente.
- Redis se ejecuta sin AOF/RDB porque contiene estado de seguridad efímero. Una
  pérdida de Redis bloquea las bóvedas y reinicia límites; también elimina la
  lista temporal de JWT revocados, cuyo riesgo queda acotado por los access
  tokens de 15 minutos. Las sesiones y refresh hashes permanecen en MariaDB.
- Flyway 11.7.2 valida correctamente las migraciones usadas, pero emite una
  advertencia porque MariaDB 11.8 es posterior a su matriz probada declarada.

## Documentación

| Documento | Contenido |
|---|---|
| [ARCHITECTURE.md](./ARCHITECTURE.md) | Módulos, capas, flujos y decisiones técnicas |
| [DATABASE.md](./DATABASE.md) | Tablas, relaciones, restricciones, índices y migraciones |
| [docs/API_REFERENCE.md](./docs/API_REFERENCE.md) | Los 41 endpoints, DTO, filtros y ejemplos |
| [docs/API_ERROR_CONTRACT.md](./docs/API_ERROR_CONTRACT.md) | Sobre y catálogo estable de errores |
| [docs/SECURITY.md](./docs/SECURITY.md) | Modelo de amenazas y controles implementados |
| [docs/OPERATIONS.md](./docs/OPERATIONS.md) | Configuración, despliegue, backups y troubleshooting |
| [docs/RESOURCE_OWNERSHIP.md](./docs/RESOURCE_OWNERSHIP.md) | Patrón obligatorio de aislamiento por cuenta |
| [docs/decisions/README.md](./docs/decisions/README.md) | Índice de ADR y estado de implementación |

## Licencia

Distribuido bajo la licencia MIT. Consulta [LICENSE](./LICENSE).
