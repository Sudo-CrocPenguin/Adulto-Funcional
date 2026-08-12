# Seguridad

## Objetivo y alcance

Este documento describe los controles implementados en `server`, los activos
que protegen y los supuestos operativos que deben conservarse al desplegar. No
sustituye una revisión independiente, pruebas de penetración ni la gestión de
secretos de la plataforma.

## Activos protegidos

- contraseña y perfil de cuenta;
- access y refresh tokens;
- sesiones autenticadas y roles;
- Master Key;
- credenciales guardadas en la bóveda;
- movimientos, gastos, eventos y categorías personales;
- secretos de infraestructura;
- trazabilidad sin exposición de datos sensibles.

## Límites de confianza

```text
Internet
   │
   ▼
Proxy TLS / CORS / límites HTTP
   │
   ▼
Spring Security ───► principal autenticado
   │
   ├──► casos de uso ───► MariaDB durable
   └──► estado efímero ─► Redis privado
```

Supuestos de producción:

- TLS termina antes o en el servidor y `APP_COOKIE_SECURE=true`.
- Spring confía en los headers reenviados únicamente porque su puerto directo
  está limitado a loopback y Traefik es el único ingreso público.
- MariaDB y Redis no son accesibles desde Internet.
- Redis exige autenticación y, si cruza una red no confiable, TLS de transporte.
- Los secretos provienen de un gestor o variables protegidas, no del repositorio.
- Los backups de MariaDB se cifran y tienen acceso restringido.

## Contraseñas de cuenta y Master Key

Ambas se almacenan como hash Argon2 usando
`Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()`.

- Registro exige 15–128 caracteres.
- Login acepta hasta 128 para conservar cuentas históricas.
- La Master Key es independiente de la contraseña de cuenta.
- Configurar, cambiar o eliminar información sensible exige reautenticación con
  la contraseña principal según el caso.
- Ni hashes ni secretos aparecen en DTO de salida.

Cuando el email no existe, login compara la contraseña con un hash Argon2
ficticio calculado al construir el caso de uso. El mensaje y el código son los
mismos que para una contraseña incorrecta, reduciendo enumeración por contenido
y tiempo.

## JWT y sesiones

Los access tokens se firman con HS256. Producción exige que `JWT_SECRET` sea
Base64 generado a partir de al menos 32 bytes aleatorios.

Claims emitidos:

| Claim | Uso |
|---|---|
| `sub` | UUID de cuenta |
| `sid` | familia de sesión |
| `jti` | access token individual |
| `email` | identidad visible |
| `roles` | autoridades persistidas |
| `iss` / `aud` | emisor y audiencia esperados |
| `iat` / `exp` | emisión y expiración |

Duraciones predeterminadas:

- access token: 15 minutos;
- refresh token: 30 días;
- ventana de concurrencia de refresh: 5 segundos.

La validación verifica firma, expiración, issuer y audience. Un JWT inválido,
expirado o revocado recibe `401` y `WWW-Authenticate: Bearer`.

### Refresh rotativo

El refresh token es opaco, aleatorio y de un solo uso. MariaDB guarda únicamente
su SHA-256.

1. El repositorio bloquea la fila de sesión.
2. Valida el hash actual y la expiración.
3. Revoca el `jti` vigente hasta su `exp`.
4. Guarda el hash anterior y rota refresh/access.
5. Una repetición concurrente dentro de la ventana devuelve conflicto.
6. Una reutilización posterior se interpreta como replay y revoca la sesión.

Cerrar sesión revoca la familia actual, limpia las cookies y bloquea la bóveda
asociada. El endpoint global revoca todas las familias de la cuenta.

### Revocación efímera

Los `jti` revocados viven en Redis con TTL igual al tiempo restante del access
token. Redis no tiene persistencia en el Compose porque también conserva Master
Keys temporales. Si Redis se pierde, la lista se reinicia; el impacto máximo es
la vida restante de un access token, 15 minutos por defecto. Las sesiones y
refresh hashes siguen en MariaDB.

Si el modelo de riesgo exige revocación que sobreviva a una pérdida total de
Redis, se necesita persistencia separada o comprobar el estado durable de la
sesión en cada request.

## Transporte web y nativo

### Web

- access cookie: `token`, `HttpOnly`, `Path=/`;
- refresh cookie: `refresh_token`, `HttpOnly`,
  `Path=/api/auth/refresh`;
- `SameSite` validado como `Strict`, `Lax` o `None`;
- `SameSite=None` exige `Secure`;
- producción exige siempre `Secure`;
- respuestas de tokens y secretos usan `Cache-Control: no-store`.

### Nativo

Los tokens solo aparecen en el body cuando `X-Client-Type` coincide con señales
pasivas de cliente no-browser. El access se usa como Bearer; el refresh se
envía solo al endpoint de rotación.

La detección de cliente no concede autorización ni sustituye la validación del
token. Su función es decidir el canal de entrega de credenciales.

## CSRF

La API usa `CookieCsrfTokenRepository` con cookie `XSRF-TOKEN` legible por el
cliente y header `X-XSRF-TOKEN`.

La protección se exige cuando:

- el método no es `GET`, `HEAD`, `TRACE` u `OPTIONS`; y
- existe cookie de access o refresh; y
- la petición no fue autenticada por un Bearer válido.

Así, un navegador no puede aprovechar automáticamente las cookies para mutar
estado sin conocer el token CSRF. Los clientes Bearer no dependen de cookies y
quedan exentos. CORS y SameSite son defensas adicionales, no sustitutos.

Un fallo devuelve `403/CSRF_TOKEN_INVALID` con el contrato uniforme.

## CORS

- Orígenes exactos configurados en `CORS_ALLOWED_ORIGINS`.
- No se permite comodín con credenciales.
- Se normaliza el slash final.
- Métodos: `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS`.
- Headers permitidos: contenido, AJAX, tipo de cliente, CSRF y Authorization.
- Headers expuestos: `X-Total-Count`, `X-Trace-Id`.
- Preflight cacheado durante una hora.
- Petición normal o preflight rechazados usan
  `403/CORS_REQUEST_REJECTED` JSON.

## Ownership e IDOR

La identidad autorizante proviene de `AuthenticatedAccount.accountId`, creado
desde `sub`; no se confía en IDs de cuenta enviados en cuerpos.

Los repositorios privados consultan por:

```text
resourceId AND accountId
```

Movimientos, gastos, eventos y credenciales también se eliminan con ambas
condiciones. Una cuenta ajena se rechaza antes de consultar. Un recurso ajeno y
uno inexistente producen `404/RESOURCE_NOT_FOUND`, evitando confirmar su
existencia.

Las categorías accesibles cumplen `SYSTEM OR ownerAccountId = accountId` y el
tipo requerido por el módulo.

## Master Key de sesión

Verificar una Master Key desbloquea únicamente `(accountId, sessionId)`.

En producción:

- clave Redis: `master-key:<accountId>:<sessionId>`;
- TTL predeterminado: una hora;
- valor cifrado con AES-GCM;
- IV aleatorio de 12 bytes;
- clave de cifrado derivada de `MASTER_KEY_SESSION_SECRET` y un contexto fijo;
- Redis sin AOF/RDB.

La operación `find` es atómica desde la perspectiva del caso de uso: retorna la
clave y su expiración o vacío. No existe una secuencia separada
`isVerified + get` vulnerable al vencimiento intermedio.

Cerrar la bóveda, cerrar sesión, revocar sesiones, cambiar la Master Key o
eliminar la cuenta limpia el estado correspondiente.

## Cifrado de credenciales

Formato actual v2:

| Propiedad | Valor |
|---|---|
| Algoritmo | AES-256-GCM |
| Derivación | PBKDF2-HMAC-SHA256 |
| Iteraciones | 600.000 |
| Salt | 16 bytes aleatorios por credencial |
| IV | 12 bytes aleatorios por cifrado |
| Tag | 128 bits |
| Charset | UTF-8 explícito |
| AAD | versión + accountId + credentialId |

El AAD detecta el traslado conjunto de salt, IV y ciphertext a otra fila. Los
fallos distinguen entrada inválida, autenticación GCM fallida, versión no
soportada y fallo interno sin exponer material sensible.

V1, con 100.000 iteraciones y sin AAD, se conserva solo para leer filas
históricas. Crear, actualizar o rotar produce v2.

El secreto de entrada se limita a 2032 bytes UTF-8 para caber, incluido el tag,
en `VARBINARY(2048)`.

## Rate limiting

Los sujetos se anonimizan con SHA-256 antes de construir claves. Las métricas
solo etiquetan política y resultado, evitando alta cardinalidad o PII.

| Política | Límite / ventana | Backoff inicial | Máximo |
|---|---|---:|---:|
| `LOGIN_IP` | 10 / 15 min | 2 s | 2 min |
| `LOGIN_ACCOUNT` | 5 / 15 min | 5 s | 5 min |
| `REGISTER_IP` | 20 / 1 h | 10 s | 10 min |
| `REFRESH_IP` | 30 / 1 min | 2 s | 1 min |
| `MASTER_KEY_SESSION` | 5 / 15 min | 5 s | 5 min |
| `VAULT_CRYPTO_SESSION` | 120 / 1 min | 1 s | 30 s |

En `prod`, Redis ejecuta contador y bloqueo mediante un script Lua atómico. En
`dev/test`, una implementación en memoria conserva la misma semántica. El
rechazo devuelve `429/RATE_LIMIT_EXCEEDED` y `Retry-After`.

## Validación e integridad

- Bean Validation controla obligatoriedad, tamaño, email, E.164, dinero y
  bytes UTF-8.
- `@NoHtml` rechaza HTML en campos visibles; no se aplica a contraseñas porque
  no se renderizan y pueden contener cualquier carácter.
- MariaDB repite invariantes críticas con `CHECK`, FK y únicos.
- `@Version` detecta actualizaciones concurrentes.
- Rotación de refresh y scheduler usan bloqueo pesimista.
- Cuerpos mayores al límite se rechazan con `413/REQUEST_TOO_LARGE`, incluso
  cuando usan transferencia chunked.

La prevención de Stored XSS no libera a los clientes de escapar contenido al
renderizarlo.

## Headers de seguridad

La cadena configura:

- Content-Security-Policy restrictiva;
- `X-Frame-Options: DENY`;
- `X-Content-Type-Options: nosniff`;
- HSTS por un año con subdominios cuando se sirve bajo HTTPS;
- header XSS legacy por compatibilidad;
- headers anti-cache para respuestas sensibles.

La CSP protege las respuestas del backend, pero la política principal de la
aplicación web debe configurarse también en el host del frontend.

## Errores y logs

- Seguridad, MVC y CORS usan el mismo sobre.
- `X-Trace-Id` se valida o genera y se imprime mediante MDC.
- Los `401` anuncian Bearer; los `405` incluyen `Allow`.
- Errores inesperados se registran con stack trace y respuesta pública genérica.
- Nunca registrar passwords, Master Keys, cookies, tokens, ciphertext
  descifrado ni cuerpos de la bóveda.

Los mensajes humanos pueden cambiar. Los clientes deben decidir con `code`.

## Fail-fast de producción

El perfil `prod` no arranca si:

- falta un secreto;
- JWT, secreto de Master Key o contraseña Redis no son Base64 con al menos 32
  bytes aleatorios y diversidad mínima;
- detecta placeholders conocidos;
- `APP_COOKIE_SECURE` es falso;
- `SameSite` es desconocido;
- `SameSite=None` no tiene `Secure`.

Genera cada secreto por separado:

```bash
openssl rand -base64 48
```

No reutilices valores entre JWT, Redis, Master Key y base de datos.

## Dependencias y contenedores

- Las imágenes Maven, JRE, MariaDB y Redis están fijadas por digest.
- El contenedor final ejecuta un usuario no root.
- El build ejecuta pruebas unitarias.
- El perfil Maven `security-scan` ejecuta OWASP Dependency-Check y falla a
  partir de CVSS 7.

```bash
./mvnw -Psecurity-scan verify
```

Un SCA exitoso no demuestra ausencia de vulnerabilidades desconocidas. Debe
formar parte de CI y ejecutarse con una base de datos de vulnerabilidades
actualizada.

Actualmente no existe un workflow activo en `.github/workflows` raíz. El
workflow histórico dentro de `server/.github/workflows` no es ejecutado por
GitHub Actions en este monorepo. Hasta crear CI raíz para el backend,
`clean verify` y SCA son controles manuales de release y su evidencia debe
registrarse. Consulta la [matriz de pruebas](../../docs/TEST_MATRIX.md).

## Checklist de producción

- [ ] TLS válido en 443; redirección HTTP→HTTPS si el puerto 80 está publicado.
- [ ] El puerto directo de Spring Boot está enlazado a loopback y solo Traefik
      recibe tráfico público.
- [ ] `APP_COOKIE_SECURE=true` y SameSite acorde al despliegue.
- [ ] Orígenes CORS exactos, sin comodines.
- [ ] Secretos independientes en un gestor, con rotación documentada.
- [ ] MariaDB y Redis en red privada y con mínimo privilegio.
- [ ] TLS hacia Redis si sale del host/red confiable.
- [ ] Backups cifrados y restauración ensayada.
- [ ] Logs centralizados preservando `traceId`.
- [ ] Alertas sobre 401, 403, 429, 5xx y healthcheck.
- [ ] `./mvnw clean verify` y SCA ejecutados sobre el commit desplegado.
- [ ] Migraciones revisadas y ventana de rollback preparada.
- [ ] Política explícita para la pérdida del estado efímero de Redis.

## Reporte de vulnerabilidades

No publiques secretos, tokens ni datos reales en un issue. El reporte debe
incluir versión, ruta afectada, impacto, pasos mínimos reproducibles y
`traceId` anonimizado. La coordinación privada debe definirse en la plataforma
que hospeda el repositorio.
