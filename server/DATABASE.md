# Base de datos

## Propósito

MariaDB es la fuente durable de identidad, sesiones, finanzas, agenda y bóveda.
Flyway crea y evoluciona el esquema; Hibernate solo lo valida. Este documento
describe el estado resultante después de V14, no únicamente la migración V1.

## Convenciones

- Motor objetivo de integración: MariaDB 11.8.8.
- IDs: UUID v7 representados como `CHAR(36)`.
- Dinero: `DECIMAL(10,2)` y `BigDecimal`.
- Timestamps técnicos: `TIMESTAMP(6)` normalizados en UTC.
- Fechas civiles: `DATE`.
- Horas civiles de agenda: `DATETIME(6)` más zona IANA e instante UTC.
- Concurrencia: columnas `BIGINT` administradas por `@Version`.
- Nombres SQL en `snake_case` y claves foráneas explícitas.

La aplicación configura `hibernate.jdbc.time_zone=UTC`, Jackson en UTC,
`ddl-auto=validate`, `open-in-view=false`, `baseline-on-migrate=false` y
`validate-on-migrate=true`.

## Relaciones

```text
accounts
  ├──< account_roles
  ├──< auth_sessions
  ├──< categories (solo PERSONAL)
  ├──< movements >── categories (FINANCES)
  ├──< fixed_expenses >── categories (FINANCES)
  ├──< events >── categories (AGENDA)
  └──< passwords
```

Las categorías `SYSTEM` no tienen propietario. Las `PERSONAL` pertenecen a una
cuenta. Los recursos no pueden referenciar una categoría inexistente; la regla
de tipo y visibilidad se aplica además en la aplicación.

## Tablas

### `accounts`

Identidad principal y hashes de autenticación.

| Columna | Tipo | Regla |
|---|---|---|
| `account_id` | `CHAR(36)` | PK, UUID v7 |
| `account_names` | `VARCHAR(50)` | obligatorio |
| `account_lastnames` | `VARCHAR(50)` | obligatorio |
| `account_email` | `VARCHAR(255)` | obligatorio, único |
| `account_phone` | `VARCHAR(20)` | obligatorio, E.164 en API |
| `account_password` | `VARCHAR(255)` | hash Argon2 |
| `account_master_key` | `VARCHAR(255)` | hash Argon2 opcional |
| `account_created_at` | `TIMESTAMP(6)` | instante UTC |
| `account_version` | `BIGINT` | bloqueo optimista |

Nunca contiene contraseñas ni Master Keys en texto plano.

### `account_roles`

Autoridades persistidas que se cargan al emitir un access token.

| Columna | Tipo | Regla |
|---|---|---|
| `account_role_fk_account_id` | `CHAR(36)` | PK parcial, FK a cuenta |
| `account_role_name` | `VARCHAR(30)` | PK parcial, `USER` o `ADMIN` |

La FK usa `ON DELETE CASCADE`. V4 asignó `USER` a todas las cuentas existentes.

### `auth_sessions`

Familias de autenticación durables y refresh tokens rotativos.

| Columna | Tipo | Regla |
|---|---|---|
| `auth_session_id` | `CHAR(36)` | PK, `sid` del JWT |
| `auth_session_fk_account_id` | `CHAR(36)` | FK a cuenta |
| `auth_session_current_refresh_hash` | `CHAR(64)` | SHA-256, único |
| `auth_session_previous_refresh_hash` | `CHAR(64)` | hash anterior temporal |
| `auth_session_previous_rotated_at` | `TIMESTAMP(6)` | instante de rotación |
| `auth_session_refresh_expires_at` | `TIMESTAMP(6)` | expiración del refresh |
| `auth_session_access_jti` | `CHAR(36)` | último access token emitido |
| `auth_session_access_expires_at` | `TIMESTAMP(6)` | expiración de ese access |
| `auth_session_created_at` | `TIMESTAMP(6)` | creación UTC |
| `auth_session_last_seen_at` | `TIMESTAMP(6)` | última rotación/uso |
| `auth_session_revoked_at` | `TIMESTAMP(6)` | nulo mientras esté activa |
| `auth_session_version` | `BIGINT` | control optimista adicional |

Solo se persisten hashes de refresh token. La fila se bloquea durante la
rotación. Las restricciones exigen expiraciones posteriores a la creación y la
FK usa `ON DELETE CASCADE`.

### `categories`

Catálogo compartido y categorías personales.

| Columna | Tipo | Regla |
|---|---|---|
| `category_id` | `CHAR(36)` | PK |
| `category_name` | `VARCHAR(50)` | nombre visible |
| `category_type` | `VARCHAR(20)` | `FINANCES` o `AGENDA` |
| `owner_account_id` | `CHAR(36)` | nulo para `SYSTEM`, FK para `PERSONAL` |
| `category_scope` | `VARCHAR(8)` | `SYSTEM` o `PERSONAL` |
| `normalized_name` | `VARCHAR(150)` | NFKC, espacios normalizados, minúsculas |
| `ownership_discriminator` | `VARCHAR(36)` generado | `SYSTEM` o UUID propietario |
| `category_version` | `BIGINT` | bloqueo optimista |

La unicidad
`(category_scope, ownership_discriminator, category_type, normalized_name)`
evita duplicados globales y personales incluso bajo concurrencia. Los `CHECK`
impiden `SYSTEM` con dueño o `PERSONAL` sin dueño. El tipo y alcance no cambian
por la API.

V3 sembró ocho categorías `FINANCES` y seis `AGENDA`. V6 convirtió las filas
existentes en `SYSTEM` y calculó el nombre normalizado con código Java para
usar exactamente la misma regla Unicode que el dominio.

### `movements`

Ingresos y egresos de una cuenta.

| Columna | Tipo | Regla |
|---|---|---|
| `movement_id` | `CHAR(36)` | PK |
| `movement_type` | `VARCHAR(20)` | `INCOME` o `EXPENSE` |
| `movement_amount` | `DECIMAL(10,2)` | mayor que cero |
| `movement_register_date` | `TIMESTAMP(6)` | registro UTC |
| `movement_description` | `TEXT` | opcional |
| `movement_date` | `DATE` | fecha de negocio |
| `movement_fk_account_id` | `CHAR(36)` | propietario |
| `movement_fk_category_id` | `CHAR(36)` | categoría obligatoria |
| `movement_version` | `BIGINT` | bloqueo optimista |

La API valida máximo ocho dígitos enteros y dos decimales sin redondeo.

### `fixed_expenses`

Definición recurrente y próximo vencimiento.

| Columna | Tipo | Regla |
|---|---|---|
| `fixed_expense_id` | `CHAR(36)` | PK |
| `fixed_expense_name` | `VARCHAR(50)` | obligatorio |
| `fixed_expense_frequency` | `VARCHAR(15)` | frecuencia cerrada |
| `fixed_expense_amount` | `DECIMAL(10,2)` | mayor que cero |
| `fixed_expense_status` | `VARCHAR(15)` | `ACTIVE` o `INACTIVE` |
| `fixed_expense_start_date` | `DATE` | inicio |
| `fixed_expense_next_due_date` | `DATE` | posterior al inicio |
| `fixed_expense_reminder_days` | `INT` | cero o positivo |
| `fixed_expense_fk_account_id` | `CHAR(36)` | propietario |
| `fixed_expense_fk_category_id` | `CHAR(36)` | categoría obligatoria |
| `fixed_expense_version` | `BIGINT` | bloqueo optimista |

Frecuencias admitidas: `WEEKLY`, `BIWEEKLY`, `MONTHLY`, `QUARTERLY`,
`SEMIANNUAL` y `ANNUAL`. El scheduler bloquea los gastos activos vencidos y
avanza la fecha en lotes.

### `events`

Agenda con tiempo civil, zona e instantes normalizados.

| Columna | Tipo | Regla |
|---|---|---|
| `event_id` | `CHAR(36)` | PK |
| `event_title` | `VARCHAR(35)` | obligatorio |
| `event_priority` | `VARCHAR(15)` | `Baja`, `Media` o `Alta` |
| `event_date` | `DATE` | coincide con inicio y fin civiles |
| `event_frequency` | `INT` | `0`, `1`, `7`, `30` o `365` |
| `event_reminder` | `DATETIME(6)` | hora civil anterior al inicio |
| `event_start_hour` | `DATETIME(6)` | hora civil de inicio |
| `event_end_hour` | `DATETIME(6)` | hora civil posterior al inicio |
| `event_zone_id` | `VARCHAR(63)` | zona IANA |
| `event_reminder_instant` | `TIMESTAMP(6)` | recordatorio UTC |
| `event_start_instant` | `TIMESTAMP(6)` | inicio UTC |
| `event_end_instant` | `TIMESTAMP(6)` | fin UTC |
| `event_description` | `TEXT` | opcional |
| `event_status` | `VARCHAR(20)` | estado cerrado |
| `event_fk_account_id` | `CHAR(36)` | propietario |
| `event_fk_category_id` | `CHAR(36)` | categoría obligatoria |
| `event_version` | `BIGINT` | bloqueo optimista |

Estados: `Pendiente`, `Completado`, `Cancelado` y `Pospuesto`. Los `CHECK`
validan el orden en tiempo civil y en UTC. V12 interpretó los eventos
históricos en `America/Bogota`, preservó sus horas locales y calculó instantes.

### `passwords`

Bóveda cifrada; no debe confundirse con `account_password`.

| Columna | Tipo | Regla |
|---|---|---|
| `password_id` | `CHAR(36)` | PK |
| `password_application_name` | `VARCHAR(35)` | único dentro de la cuenta |
| `password_salt` | `VARCHAR(255)` | salt Base64 |
| `password_crypto_version` | `SMALLINT` | `1` o `2` |
| `password_iv` | `BINARY(12)` | IV GCM exacto |
| `password_ciphertext` | `VARBINARY(2048)` | secreto y tag GCM |
| `password_last_change_date` | `DATE` | opcional |
| `passwords_fk_account_id` | `CHAR(36)` | propietario |
| `password_version` | `BIGINT` | bloqueo optimista |

La restricción `uk_passwords_account_application` resuelve carreras que una
comprobación `exists + save` no podría evitar. La versión 2 liga el ciphertext
a `accountId`, `passwordId` y versión mediante AAD.

## Integridad referencial y borrado

Al eliminar una cuenta se borran en cascada:

- roles;
- sesiones de autenticación;
- categorías personales;
- movimientos;
- gastos fijos;
- eventos;
- credenciales.

Las categorías `SYSTEM` sobreviven porque su propietario es nulo. El caso de
uso reautentica la contraseña, revoca el estado de seguridad y ejecuta un
borrado directo; las FK son la garantía final de integridad.

Eliminar una categoría referenciada no elimina sus recursos: MariaDB rechaza
la operación y la API devuelve `409/DATA_INTEGRITY_CONFLICT`.

## Restricciones `CHECK`

V11 y V12 protegen, además de la validación Java:

- tipos de movimiento e importes positivos;
- frecuencia, estado, importe, recordatorio y fechas de gastos;
- prioridad, estado, frecuencia y orden temporal de eventos;
- coherencia entre horas civiles, `event_date` e instantes UTC;
- alcance, propietario y tipo de categorías;
- versión criptográfica;
- nombres de roles;
- expiraciones de sesiones.

La validación duplicada es intencional: la API produce errores legibles y la
base impide datos inválidos por concurrencia, scripts o integraciones directas.

## Índices

Además de PK, FK y únicos, el esquema tiene índices orientados a ownership y
paginación:

| Índice | Columnas principales | Uso |
|---|---|---|
| `idx_auth_sessions_account_active` | cuenta, revocación | sesiones activas |
| `idx_auth_sessions_previous_refresh` | hash anterior | replay de refresh |
| `idx_movements_account_date_id` | cuenta, fecha, UUID | historial financiero |
| `idx_fixed_expenses_account_due_id` | cuenta, vencimiento, UUID | listado y scheduler |
| `idx_events_account_date_id` | cuenta, fecha, UUID | agenda por fecha |
| `idx_events_account_start_instant_id` | cuenta, instante, UUID | orden UTC |
| `idx_categories_owner_type_name` | dueño, tipo, nombre | catálogo accesible |
| `idx_passwords_account_change_id` | cuenta, fecha, UUID | listado de bóveda |

Los repositorios añaden UUID como desempate para paginación determinista.

## Historial Flyway

| Versión | Tipo | Propósito |
|---:|---|---|
| V1 | SQL | Esquema inicial e índices básicos |
| V2 | SQL | Corrige IV de AES-GCM a 12 bytes |
| V3 | SQL | Siembra categorías globales |
| V4 | SQL | Roles y sesiones de autenticación |
| V5 | SQL | Versión criptográfica de credenciales |
| V6 | Java | Ownership y normalización Unicode de categorías |
| V7 | SQL | Versiones de movimientos y gastos |
| V8 | SQL | Versiones de eventos y categorías |
| V9 | SQL | Versiones de cuenta/bóveda y unicidad de aplicación |
| V10 | SQL | Cascadas al eliminar cuentas |
| V11 | SQL | Checks de dominio e índices de consulta |
| V12 | SQL | Zonas e instantes UTC de eventos |
| V13 | SQL | Precisión UTC de creación de cuenta |
| V14 | SQL | Precisión UTC del registro de movimiento |

V6 es una migración Java ubicada en `src/main/java/database/migrations`; las
demás están en `src/main/resources/database/migrations`.

## Política de migración

1. No modificar archivos V1–V14 después de haber sido publicados.
2. Crear la siguiente versión disponible con un propósito atómico.
3. Diseñar backfill antes de imponer `NOT NULL` o `CHECK`.
4. Probar instalación vacía y actualización desde la versión anterior.
5. Ejecutar `./mvnw clean verify` contra MariaDB real.
6. Mantener `baseline-on-migrate=false` salvo una migración operativa
   controlada y documentada.
7. Conservar `ddl-auto=validate`; Hibernate no sustituye Flyway.

## Backup y restauración

Antes de una migración en producción:

```bash
docker compose exec -T mariadb sh -c \
  'exec mariadb-dump -u root -p"$MARIADB_ROOT_PASSWORD" \
  --single-transaction --routines --triggers "$MARIADB_DATABASE"' \
  > adulto_funcional.sql
```

El archivo contiene datos sensibles y debe cifrarse, limitarse por permisos y
retirarse según la política de retención. Una restauración debe ensayarse en un
entorno aislado; un backup no verificado no constituye un plan de recuperación.

No se respalda Redis: su contenido es deliberadamente efímero. Después de un
reinicio, los usuarios vuelven a desbloquear la bóveda y los límites comienzan
una nueva ventana.

## Diagnóstico

```sql
SELECT version, description, type, installed_on, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

Si Flyway detecta checksum distinto, no se debe ejecutar `repair` de manera
automática. Primero hay que comparar el archivo publicado, el historial y el
artefacto desplegado. La guía operativa completa está en
[docs/OPERATIONS.md](./docs/OPERATIONS.md).
