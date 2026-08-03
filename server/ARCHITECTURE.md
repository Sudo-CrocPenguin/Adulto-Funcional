# Arquitectura del backend

## Propósito

Este documento explica qué arquitectura usa `server`, por qué existe cada
capa y cómo fluye una operación desde HTTP hasta MariaDB o Redis. La fuente de
verdad ejecutable sigue siendo el código y las migraciones.

## Estilo arquitectónico

El backend es un **monolito modular por dominio**, inspirado en Clean
Architecture, DDD y puertos/adaptadores. Se despliega como un único proceso,
pero separa las responsabilidades en módulos de negocio.

No se presenta como Clean Architecture estricta: `agenda` reutiliza el modelo
de categorías de `finances`, autenticación integra servicios de Spring
Security y algunos mapeadores viven en infraestructura. Estas dependencias son
explícitas y no convierten los módulos en microservicios.

Principios aplicados:

- El dominio contiene invariantes y no depende de controladores ni entidades
  JPA.
- La aplicación orquesta casos de uso, transacciones y puertos.
- Infraestructura adapta HTTP, JPA, Redis y criptografía.
- La identidad de cuenta siempre se obtiene del principal autenticado.
- Los recursos privados se consultan y mutan dentro del límite de la cuenta.
- El esquema cambia únicamente mediante migraciones Flyway incrementales.

## Vista de contexto

```text
┌──────────────────┐       HTTPS/JSON       ┌────────────────────────┐
│ Web / móvil / CLI├───────────────────────►│ Adulto Funcional Server│
└──────────────────┘                        └───────────┬────────────┘
                                                      │
                               ┌──────────────────────┴───────────────────┐
                               │                                          │
                               ▼                                          ▼
                     ┌──────────────────┐                        ┌─────────────────┐
                     │ MariaDB 11.8     │                        │ Redis 7.4       │
                     │ datos durables   │                        │ estado efímero  │
                     └──────────────────┘                        └─────────────────┘
```

MariaDB almacena cuentas, roles, sesiones, recursos y material cifrado. Redis
mantiene revocaciones de access token, rate limiting y desbloqueos temporales
de Master Key; no es la fuente durable de los datos de negocio.

## Organización del código

```text
src/main/java
├── database/migrations/              # Migración Java V6
└── org/adultofuncional/main
    ├── account/
    ├── agenda/
    ├── auth/
    ├── config/
    ├── finances/
    ├── security/
    └── shared/
```

Los módulos de negocio conservan, cuando aplica, esta estructura:

```text
<módulo>/
├── domain/
│   ├── model/       # Entidades y reglas
│   ├── enums/       # Vocabulario cerrado
│   ├── repository/  # Puertos de persistencia
│   └── service/     # Puertos de capacidades de dominio
├── application/
│   ├── dto/         # Contratos de entrada y salida
│   ├── service/     # Orquestación reutilizable
│   └── usecase/     # Casos de uso transaccionales
└── infrastructure/
    ├── controller/  # Adaptadores REST
    ├── persistence/ # Entidades, Spring Data y mapeadores
    ├── repository/  # Implementación de puertos
    ├── scheduling/  # Jobs
    └── service/     # Redis, cifrado u otros adaptadores
```

## Capas y reglas de dependencia

### Dominio

Contiene el estado y las invariantes que deben cumplirse sin importar el canal
de entrada o la tecnología de persistencia.

Ejemplos:

- `Movement` exige importe positivo, categoría y propietario.
- `FixedExpense` valida frecuencia, estado, fechas y avance recurrente.
- `Event` valida prioridad, estado, frecuencia, orden temporal, zona e
  instantes.
- `Category` modela alcance `SYSTEM` o `PERSONAL` y tipo inmutable.
- `AuthSession` controla rotación, expiración, replay y revocación.
- `Password` conserva versión de fila y versión criptográfica.

Los repositorios del dominio son interfaces. No exponen `JpaRepository`,
`Pageable`, entidades JPA ni detalles SQL.

### Aplicación

Implementa las operaciones del sistema:

- obtiene la cuenta desde un identificador ya autorizado;
- valida existencia, estado y acceso a categorías;
- abre transacciones;
- coordina varios puertos;
- traduce el resultado a DTO;
- aplica paginación segura mediante `PageQuery` y `PageResult`.

La capa de aplicación puede depender de Spring para transacciones, inyección y
seguridad, pero no expone entidades de persistencia en su contrato HTTP.

### Infraestructura

Conecta el núcleo con tecnologías externas:

- controladores y filtros Servlet;
- repositorios Spring Data JPA;
- entidades Hibernate;
- Redis mediante `StringRedisTemplate`;
- AES/PBKDF2 mediante JCA;
- scheduler de Spring;
- MariaDB y Flyway.

Los adaptadores JPA traducen modelos de dominio a entidades. Las consultas de
recursos privados incorporan `accountId` en la sentencia, en lugar de cargar
una fila global y autorizar después.

### Shared y config

`shared` contiene conceptos transversales sin pertenencia clara a un solo
módulo:

- errores y respuestas;
- ownership;
- normalización;
- paginación;
- rate limiting;
- validaciones;
- trazabilidad;
- política temporal.

`config` construye beans e integra Spring Security. No es un módulo de negocio.

## Responsabilidad por módulo

| Módulo | Agregados o conceptos | Dependencias relevantes |
|---|---|---|
| `account` | `Account` | Autenticación para reautenticar y revocar al eliminar |
| `auth` | `AuthSession`, `AccountRole` | Cuenta, JWT, Redis y Master Key de sesión |
| `finances` | `Movement`, `FixedExpense`, `Category` | Cuenta y scheduler |
| `agenda` | `Event` | Categorías accesibles de tipo `AGENDA` |
| `security` | `Password`, Master Key | Cuenta, sesión autenticada, Redis y criptografía |
| `shared` | Contratos transversales | Consumido por todos los módulos |

Las categorías viven en `finances` porque nacieron como clasificación
financiera. `agenda` reutiliza ese catálogo con el tipo `AGENDA`. Esta es una
dependencia modular consciente; cualquier extracción futura debe introducir un
puerto de catálogo antes de mover clases.

## Flujo de una petición protegida

```text
1. RequestBodySizeFilter limita el cuerpo a 1 MiB por defecto.
2. HttpTraceIdFilter acepta o genera X-Trace-Id y lo coloca en MDC.
3. ApiCorsProcessor valida origen y preflight.
4. JwtAuthenticationFilter busca Bearer y luego cookie token.
5. CookieAuthenticatedCsrfMatcher exige CSRF si una cookie autentica una
   operación no segura; Bearer válido queda exento.
6. Spring Security exige autenticación según la ruta.
7. Controller extrae AuthenticatedAccount y valida el path de cuenta si aplica.
8. Caso de uso ejecuta reglas y transacción.
9. Repositorio filtra por accountId y traduce dominio/JPA.
10. ApiResponse conserva el contrato uniforme.
```

El orden concreto se verifica en las pruebas de contrato de seguridad.

## Autenticación y sesiones

La cadena HTTP es stateless respecto de `HttpSession`, pero la autenticación
no es puramente stateless: cada login crea una familia durable en
`auth_sessions`.

```text
Login válido
   │
   ├─ crea sessionId
   ├─ persiste hash del refresh token
   ├─ emite access JWT con sub, sid, jti, email, roles, iss, aud, iat y exp
   └─ entrega cookies web o tokens en body nativo

Refresh
   │
   ├─ bloquea la fila de sesión
   ├─ compara el hash actual
   ├─ revoca el jti anterior hasta su exp
   ├─ rota refresh y access
   └─ detecta concurrencia o replay del token anterior
```

Los roles se cargan desde `account_roles` al emitir cada access token. El
registro crea `USER`; `ADMIN` solo puede asignarse mediante un proceso
administrativo fuera de la API pública actual.

## Master Key y criptografía

La bóveda tiene dos niveles:

1. MariaDB conserva el hash Argon2 de la Master Key, nunca su texto plano.
2. Después de verificarla, el adaptador de sesión guarda temporalmente una
   copia cifrada, indexada por cuenta y sesión autenticada.

En producción el valor temporal usa Redis con TTL. Su payload se cifra con
AES-GCM y una clave derivada de `MASTER_KEY_SESSION_SECRET`.

Las credenciales usan un formato versionado:

| Versión | KDF | Cifrado | Contexto |
|---|---|---|---|
| v1 histórica | PBKDF2-SHA256, 100.000 | AES-256-GCM | Sin AAD |
| v2 actual | PBKDF2-SHA256, 600.000 | AES-256-GCM | AAD con versión, cuenta y credencial |

Cada fila tiene salt de 16 bytes, IV de 12 bytes, tag GCM de 128 bits y
ciphertext de máximo 2048 bytes. La rotación de Master Key descifra y recifra
toda la bóveda en una transacción; un fallo revierte todos los cambios.

## Ownership

El patrón obligatorio para recursos privados es:

```text
principal.accountId
        │
        └── repository.findByIdAndAccountId(resourceId, accountId)
```

Las eliminaciones usan sentencias acotadas por ambos IDs. Un recurso ajeno y
uno inexistente responden igual: `404/RESOURCE_NOT_FOUND`.

Las categorías son la excepción deliberada al modelo privado simple:

```text
accesible = scope == SYSTEM OR ownerAccountId == principal.accountId
```

Además se exige el tipo correcto: `FINANCES` para movimientos y gastos;
`AGENDA` para eventos.

Consulta [docs/RESOURCE_OWNERSHIP.md](./docs/RESOURCE_OWNERSHIP.md).

## Consistencia y concurrencia

- Siete entidades usan `@Version`: cuenta, sesión, categoría, movimiento,
  gasto fijo, evento y credencial.
- Una versión obsoleta produce `409/CONCURRENT_MODIFICATION`.
- Email, nombre normalizado de categoría y aplicación por cuenta tienen
  restricciones únicas en MariaDB.
- La rotación de refresh y el avance de gastos fijos usan bloqueo pesimista.
- Las operaciones criptográficas de rotación son transaccionales.
- Las FK con `ON DELETE CASCADE` garantizan limpieza al borrar una cuenta sin
  materializar colecciones completas.

## Paginación y consultas

Los listados construyen un `PageQuery` independiente de Spring Data. La capa de
aplicación valida página, tamaño, dirección y campo permitido; infraestructura
lo traduce a `Pageable` y añade el UUID como desempate.

Las categorías de una página se cargan en lote para evitar N+1. Los filtros se
aplican en SQL, no sobre colecciones completas en memoria.

## Política temporal

- `Clock.systemUTC()` se registra como bean y puede sustituirse en pruebas.
- Timestamps técnicos usan `Instant` y Hibernate opera en UTC.
- Fechas de movimientos y vencimientos usan `LocalDate`.
- Eventos conservan `LocalDateTime` civil, zona IANA e `Instant` normalizado.
- Datos históricos de eventos sin zona se interpretaron como
  `America/Bogota` durante V12.
- El cron de gastos fijos se evalúa en UTC.

## Scheduler de gastos fijos

`FixedExpenseSchedule` ejecuta `AdvanceFixedExpenseDueDatesUseCase` según
`APP_FIXED_EXPENSE_CRON`.

El caso de uso:

1. toma la fecha actual desde `Clock`;
2. selecciona un lote de gastos `ACTIVE` vencidos;
3. bloquea las filas;
4. avanza cada `nextDueDate` hasta quedar posterior al corte;
5. repite hasta `max-batches`.

El job no genera movimientos ni envía notificaciones.

## Errores y observabilidad

`GlobalExceptionHandler`, Spring Security y CORS usan el mismo constructor de
errores. Cada fallo público incluye código estable y `traceId`. El filtro de
trazabilidad coloca el valor en MDC y el patrón de logging lo imprime.

Los logs no deben incluir contraseñas, Master Keys, access/refresh tokens ni
cuerpos de la bóveda.

## Configuración y perfiles

| Perfil | Estado efímero | Uso |
|---|---|---|
| `dev` | Memoria con TTL | Desarrollo local |
| `test` | Memoria con TTL | Pruebas automatizadas |
| `prod` | Redis | Despliegue multiinstancia |

La configuración común desactiva Open Session in View, multipart y
`baseline-on-migrate`; limita headers y cuerpos; fija Jackson/Hibernate en UTC.

## Pruebas arquitectónicas

La suite cubre:

- dominio y casos de uso sin infraestructura;
- contratos HTTP de JWT, CORS, CSRF, errores y rate limiting;
- ownership por cuenta;
- paginación SQL;
- control optimista;
- migraciones y restricciones reales en MariaDB 11.8;
- Redis para Master Key y revocación;
- manipulación de ciphertext, Unicode y rotación transaccional.

Testcontainers comparte una instancia MariaDB por suite para validar el mismo
motor usado en despliegue.

## Reglas para extender el sistema

1. Añadir invariantes al dominio antes de duplicarlas en controladores.
2. Declarar puertos en dominio y adaptadores en infraestructura.
3. Obtener `accountId` del principal, nunca del body o query string.
4. Consultar recursos privados por ID y propietario en una sola sentencia.
5. Paginar en SQL y permitir únicamente campos de orden explícitos.
6. Usar `Clock`, `Instant`, `LocalDate` y zona IANA según la semántica.
7. Añadir una migración nueva; no editar migraciones aplicadas.
8. Traducir fallos a códigos del catálogo público.
9. Cubrir camino feliz, ownership, validación y concurrencia.
10. Actualizar README, referencia API, base de datos y ADR afectados.

## Decisiones

Las decisiones aceptadas y su estado de implementación están en
[docs/decisions/README.md](./docs/decisions/README.md). Los ADR explican el
porqué; este documento describe la arquitectura resultante.
