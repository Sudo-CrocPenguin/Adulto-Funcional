# Operación y despliegue

## Propósito

Esta guía explica cómo configurar, construir, desplegar, observar, respaldar y
recuperar el backend. Los comandos parten desde la carpeta `server`.

## Perfiles

| Perfil | MariaDB | Estado efímero | Cookies | Uso |
|---|---|---|---|---|
| `dev` | obligatorio | memoria con TTL | HTTP permitido | desarrollo local |
| `test` | Testcontainers | memoria con TTL | configuración de prueba | suite |
| `prod` | obligatorio | Redis | `Secure` obligatorio | despliegue |

No se soporta arrancar sin perfil: faltan datasource y una implementación de
los puertos de estado efímero.

## Configuración base

La configuración común aplica:

- puerto `8080` y bind `127.0.0.1` por defecto;
- Jackson e Hibernate en UTC;
- `ddl-auto=validate`;
- Flyway habilitado y validado;
- `open-in-view=false`;
- multipart deshabilitado;
- headers máximos de 16 KiB;
- cuerpo máximo de 1 MiB;
- job de gastos fijos habilitado;
- repositorios Redis de Spring Data deshabilitados porque Redis se usa mediante
  adaptadores explícitos.

## Variables

### Aplicación y red

| Variable | Predeterminado | Uso |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | — | `dev`, `test` o `prod` |
| `SPRING_APPLICATION_NAME` | `adulto-funcional-server` | nombre de logs/métricas |
| `SERVER_PORT` | `8080` | puerto interno |
| `SERVER_ADDRESS` | `127.0.0.1` | bind local; Compose usa `0.0.0.0` dentro del contenedor |
| `SERVER_HOST_ADDRESS` | `127.0.0.1` | bind publicado por Compose |
| `SERVER_HOST_PORT` | `8080` | puerto publicado por Compose |
| `API_PUBLIC_HOST` | — | hostname HTTPS usado por el overlay de Coolify |
| `APP_HTTP_MAX_REQUEST_BODY_SIZE` | `1MB` | límite del filtro HTTP al ejecutar la app directamente |

El Compose actual conserva el límite HTTP predeterminado de 1 MiB.

### MariaDB y Flyway

| Variable | Obligatoria | Uso |
|---|---|---|
| `MARIADB_ROOT_PASSWORD` | Compose | administración del contenedor |
| `MARIADB_DATABASE` | Compose | base creada al inicializar volumen |
| `MARIADB_USER` | Compose | usuario de aplicación |
| `MARIADB_PASSWORD` | Compose | contraseña de aplicación |
| `SPRING_DATASOURCE_URL` | app directa | JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | app directa | usuario JDBC |
| `SPRING_DATASOURCE_PASSWORD` | app directa | contraseña JDBC |
| `SPRING_FLYWAY_ENABLED` | no | `true` |
| `SPRING_FLYWAY_BASELINE_ON_MIGRATE` | no | `false`; no habilitar rutinariamente |
| `SPRING_FLYWAY_VALIDATE_ON_MIGRATE` | no | `true` |

### Redis

| Variable | Predeterminado | Uso |
|---|---|---|
| `REDIS_HOST` | `redis` en prod | host |
| `REDIS_PORT` | `6379` | puerto |
| `REDIS_PASSWORD` | obligatorio en prod | autenticación y fail-fast |

`REDIS_PASSWORD` debe ser Base64 con al menos 32 bytes aleatorios. El Compose
desactiva RDB y AOF.

### JWT y sesiones

| Variable | Predeterminado | Uso |
|---|---:|---|
| `JWT_SECRET` | obligatorio | firma HS256; Base64 aleatorio |
| `JWT_ACCESS_EXPIRATION` | `900000` ms | access token; app directa |
| `JWT_EXPIRATION` | `900000` ms | alias usado por Compose |
| `JWT_ISSUER` | `adulto-funcional-server` | issuer esperado |
| `JWT_AUDIENCE` | `adulto-funcional-clients` | audiencia esperada |
| `REFRESH_TOKEN_EXPIRATION` | `2592000000` ms | 30 días |
| `REFRESH_REPLAY_WINDOW_SECONDS` | `5` | concurrencia tolerada |
| `REFRESH_TOKEN_BYTES` | `32` | entropía del token opaco |

Rotar `JWT_SECRET` invalida inmediatamente todos los access tokens, pero no
borra las familias de refresh persistidas. Planifica la rotación junto con la
experiencia de reautenticación de clientes.

### Master Key

| Variable | Predeterminado | Uso |
|---|---:|---|
| `MASTER_KEY_SESSION_SECRET` | obligatorio en prod | cifra valores temporales Redis |
| `MASTER_KEY_SESSION_TTL` | `3600000` ms | una hora |

Rotar el secreto de sesión vuelve indescifrables los desbloqueos presentes. La
acción segura es vaciar las claves `master-key:*` y pedir verificación otra vez;
no afecta los ciphertext durables de la bóveda.

### Web

| Variable | Obligatoria | Uso |
|---|---|---|
| `CORS_ALLOWED_ORIGINS` | sí | lista de orígenes exactos |
| `APP_COOKIE_SECURE` | sí | obligatorio `true` en prod |
| `APP_COOKIE_SAME_SITE` | sí | `Strict`, `Lax` o `None` |

Para varios orígenes usa la sintaxis de lista aceptada por Spring, por ejemplo:

```env
CORS_ALLOWED_ORIGINS=https://app.example.com,https://admin.example.com
```

### Scheduler

| Variable | Predeterminado | Uso |
|---|---:|---|
| `APP_DEFAULT_TIME_ZONE` | `America/Bogota` | eventos que omiten zona |
| `APP_JOBS_ENABLED` | `true` | habilita jobs |
| `APP_FIXED_EXPENSE_CRON` | `0 5 * * * *` | segundo 0, minuto 5 de cada hora |
| `APP_FIXED_EXPENSE_BATCH_SIZE` | `100` | filas por lote |
| `APP_FIXED_EXPENSE_MAX_BATCHES` | `10` | límite por ejecución |

El cron siempre se evalúa en UTC. En despliegue multiinstancia, el bloqueo de
filas evita doble avance, pero cada instancia intenta ejecutar el job. Para
evitar trabajo duplicado o esperas, habilita el job en una sola instancia o
introduce un coordinador distribuido.

## Desarrollo local

1. Inicia o prepara MariaDB.
2. Copia la plantilla:

   ```bash
   cp src/main/resources/application-dev.yml.example \
      src/main/resources/application-dev.yml
   ```

3. Configura JDBC, JWT, CORS y cookies.
4. Arranca:

   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```

5. Verifica:

   ```bash
   curl -i http://localhost:8080/actuator/health
   ```

`application-dev.yml` está ignorado y no debe commitearse.

## Docker Compose local

### Preparación

```bash
cp .env.example .env
openssl rand -base64 48
```

Ejecuta el comando de OpenSSL por separado para JWT, secreto de Master Key y
Redis. No copies el mismo resultado en varios campos.

### Construcción y arranque

```bash
docker compose config
docker compose up -d --build
docker compose ps
```

El build multi-stage:

1. resuelve dependencias Maven;
2. compila 269 fuentes;
3. ejecuta pruebas unitarias que no requieren Docker;
4. empaqueta el JAR;
5. copia el artefacto a una JRE mínima;
6. ejecuta como `appuser`, no root.

MariaDB y Redis esperan healthcheck antes de iniciar la aplicación.

### Logs

```bash
docker compose logs -f --tail=200 app
docker compose logs -f mariadb
docker compose logs -f redis
```

Busca el `traceId` devuelto por la API:

```bash
docker compose logs app | rg '<trace-id>'
```

### Estado y shell

```bash
docker compose ps
docker compose exec app sh
docker compose exec mariadb mariadb -u "$MARIADB_USER" \
  -p"$MARIADB_PASSWORD" "$MARIADB_DATABASE"
```

No expongas comandos con contraseñas en historiales compartidos. Para operación
real prefiere archivos de credenciales protegidos o secretos de la plataforma.

## Coolify

El overlay añade la red externa y la etiqueta de puerto de Traefik:

```bash
docker compose \
  -f docker-compose.yml \
  -f docker-compose.coolify.yml \
  config

docker compose \
  -f docker-compose.yml \
  -f docker-compose.coolify.yml \
  up -d --build
```

La red debe existir:

```bash
docker network inspect "${COOLIFY_NETWORK:-coolify}"
```

Configura HTTPS en el proxy. No publiques MariaDB ni Redis. El proxy debe
preservar `Origin`, cookies, headers CSRF y `X-Trace-Id`.

En `server1`, el puerto 443 es público y el 80 no está disponible. El resolver
`letsencrypt` de Traefik debe habilitar el desafío TLS-ALPN:

```yaml
- '--certificatesresolvers.letsencrypt.acme.tlschallenge=true'
```

El `.env` productivo conserva:

```dotenv
SERVER_HOST_ADDRESS=127.0.0.1
SERVER_HOST_PORT=8090
COOLIFY_NETWORK=coolify
API_PUBLIC_HOST=api-adulto-funcional.38-225-48-28.sslip.io
```

Arranca siempre el despliegue público con ambos archivos:

```bash
docker compose \
  -f docker-compose.yml \
  -f docker-compose.coolify.yml \
  up -d --build
```

Comprueba que solo `app` pertenezca a las redes `internal` y `coolify`;
MariaDB y Redis deben permanecer únicamente en `internal`.

## Build y pruebas

```bash
# Regresión completa; necesita Docker
./mvnw clean verify

# Mismo descubrimiento de Surefire; también incluye integraciones
./mvnw test

# Solo pruebas sin Testcontainers, como en el Dockerfile
./mvnw test \
  -Dtest='!*IntegrationTest,!*HttpIntegrationTest,!AdultoFuncionalServerApplicationTests'

# Artefacto
./mvnw clean package

# SCA con OWASP Dependency-Check
./mvnw -Psecurity-scan verify

# Validación de whitespace Git
git diff --check
```

La referencia auditada el 11 de agosto de 2026 es 137 pruebas, 136 exitosas y
1 fallo. `ResourceOwnershipHttpIntegrationTest` usa el evento fijo
`2026-08-10`; al quedar en el pasado, la API responde `400` y la prueba esperaba
`201`. Es un defecto del fixture temporal, no una razón para relajar la regla de
dominio. La release permanece bloqueada hasta corregir la prueba.

El repositorio no tiene actualmente un workflow de backend activo en la
carpeta `.github/workflows` raíz. El archivo histórico
`server/.github/workflows/ci-workflow.yml` no es descubierto por GitHub Actions
desde un monorepo y su configuración tampoco representa todos los secretos
actuales. Por tanto, regresión y SCA son controles manuales obligatorios hasta
crear un workflow raíz específico.

El primer SCA puede tardar por la descarga de datos. Un fallo de red no debe
reinterpretarse como ausencia de vulnerabilidades; repite desde un entorno con
acceso y caché controlada.

## Despliegue seguro

Secuencia recomendada:

1. Fijar el commit o tag a desplegar.
2. Ejecutar `./mvnw clean verify` y SCA.
3. Revisar nuevas migraciones y su backfill.
4. Crear y verificar backup.
5. Generar secretos en el gestor de la plataforma.
6. Validar el Compose combinado con `docker compose config`.
7. Construir la imagen del commit exacto.
8. Desplegar una instancia y observar migración/healthcheck.
9. Probar login, refresh, CSRF y un recurso privado.
10. Escalar si corresponde y vigilar 5xx/429/latencia.

No uses `-DskipTests` para preparar el artefacto de una release.

## Flyway

Al arranque:

1. conecta con MariaDB;
2. valida checksums y orden;
3. aplica versiones pendientes;
4. Hibernate valida el resultado.

Comandos manuales:

```bash
SPRING_DATASOURCE_URL=jdbc:mariadb://localhost:3306/adulto_funcional \
SPRING_DATASOURCE_USERNAME=usuario \
SPRING_DATASOURCE_PASSWORD=secreto \
SPRING_FLYWAY_LOCATIONS=filesystem:src/main/resources/database/migrations \
./mvnw flyway:info
```

La migración V6 es Java y vive en `src/main/java/database/migrations`; para
validar el conjunto exactamente como la aplicación, la prueba de contexto es
la referencia principal.

Flyway 11.7.2 advierte que MariaDB 11.8 es posterior a su versión probada
declarada. Las pruebas ejecutan V1–V14 sobre MariaDB 11.8.8; la advertencia debe
seguirse en actualizaciones, no silenciarse sin verificar compatibilidad.

## Backup

Ejemplo desde Compose:

```bash
docker compose exec -T mariadb sh -c \
  'exec mariadb-dump -u root -p"$MARIADB_ROOT_PASSWORD" \
  --single-transaction --routines --triggers "$MARIADB_DATABASE"' \
  > adulto_funcional-$(date +%F).sql
```

El dump contiene PII, hashes y ciphertext. Aunque las credenciales estén
cifradas, el archivo debe cifrarse, tener permisos mínimos y retención
definida.

Verifica checksum y compresión antes de restaurar. La guía del homelab incluye
un [ensayo aislado completo](./HOMELAB_DEPLOYMENT.md#ensayo-de-restauración-aislada).
Después de importar, comprueba:

```sql
SELECT version, description, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

También se deben contar tablas principales, consultar una muestra no sensible
y comprobar que ninguna fila de `flyway_schema_history` tenga `success=false`.
Una copia nunca restaurada es un archivo no verificado, no una garantía de
recuperación.

## Rollback

Flyway Community no genera automáticamente una reversión. Un rollback seguro
depende del cambio:

- Código compatible con el esquema nuevo: volver a la imagen anterior.
- Migración destructiva o incompatible: restaurar backup en una base nueva y
  cambiar el datasource.
- Secreto JWT rotado: restaurar el secreto anterior solo si sigue protegido;
  de lo contrario, aceptar reautenticación global.
- Secreto de Master Key de sesión: vaciar sesiones efímeras y volver a
  verificar; no restaurar valores Redis antiguos con una clave nueva.

Nunca edites una migración aplicada para intentar volver atrás.

## Observabilidad

### Healthcheck

```bash
curl -fsS http://localhost:8080/actuator/health
```

Actuator expone únicamente health en la configuración actual.

### Logs

El patrón incluye:

```text
[traceId=<valor>]
```

El servidor devuelve `X-Trace-Id` y el mismo valor en errores. Los proxies y
agregadores no deben eliminarlo.

### Métricas

El rate limiter registra `security.rate_limit.attempts` con etiquetas
`policy` y `outcome`. Actuator no publica todavía un endpoint Prometheus; para
scraping externo se requiere configurar la exportación y exposición de forma
explícita.

## Rotación de secretos

| Secreto | Efecto de rotar | Coordinación |
|---|---|---|
| JWT | access tokens dejan de validar | desplegar todas las instancias juntas |
| Master Key de sesión | desbloqueos Redis dejan de descifrar | limpiar `master-key:*` |
| Redis | la app pierde conexión hasta coincidir | actualizar servidor y clientes Redis |
| MariaDB app | conexiones nuevas fallan hasta coincidir | rotar usuario y datasource |
| MariaDB root | no afecta al pool de app | actualizar operación/backups |

No registres los valores durante la rotación.

## Troubleshooting

### `REDIS_PASSWORD debe ser Base64`

El fail-fast rechazó un placeholder o una contraseña no aleatoria. Genera un
valor nuevo con `openssl rand -base64 48`, actualiza Redis y aplicación en la
misma operación y recrea ambos servicios.

### Login funciona por curl pero no en navegador HTTP

`APP_COOKIE_SECURE=true` impide enviar cookies por HTTP. Usa HTTPS o el perfil
`dev` con `Secure=false`. No desactives Secure en producción.

### `Invalid CORS request`

Comprueba esquema, host y puerto exactos en `CORS_ALLOWED_ORIGINS`. No incluyas
rutas. El servidor normaliza slash final, pero no cambia HTTP por HTTPS.

### El certificado público no se emite

Comprueba que DNS resuelva a la IP pública, el puerto 443 llegue a Traefik, el
router use `tls.certresolver=letsencrypt` y el resolver tenga
`acme.tlschallenge=true`. El desafío HTTP no funciona si el puerto 80 no es
alcanzable.

### `CSRF_TOKEN_INVALID`

Solicita `/api/auth/csrf`, conserva `XSRF-TOKEN` y cookies, y reenvía el valor
en `X-XSRF-TOKEN`. Tras un cambio de sesión solicita un token nuevo.

### Flyway checksum mismatch

Detén el despliegue. Compara migraciones del artefacto con el historial. No
ejecutes `repair` hasta demostrar que el cambio de checksum es legítimo y no
alteró SQL aplicado.

### Puerto 8080 ocupado

```bash
ss -ltn '( sport = :8080 )'
SERVER_HOST_PORT=8081 docker compose up -d
```

### La red Coolify no existe

Crea o selecciona la red administrada por la plataforma y configura
`COOLIFY_NETWORK`. El Compose base no necesita esa red.

### Advertencia de Mockito sobre agente dinámico

La suite actual puede mostrar una advertencia de auto-attach de Mockito/Byte
Buddy bajo Java 21. No representa un fallo de prueba, pero debe resolverse antes
de adoptar un JDK que deshabilite agentes dinámicos por defecto.

## Apagado

```bash
docker compose down
```

No uses `down -v` salvo que la eliminación irreversible del volumen MariaDB
sea el objetivo explícito y exista un backup verificado.
