# ADR 0002: Sesiones de autenticación y revocación

- Estado: aceptada; transporte nativo reemplazado parcialmente por ADR 0007
- Implementación: completa salvo la regla de detección sustituida
- Fecha: 2026-07-31
- Alcance: módulos `auth`, `security` y configuración de seguridad
- Reemplazada parcialmente por: [ADR 0007](0007-native-client-detection.md)

## Contexto

El access token actual identifica la cuenta, pero no una sesión concreta. No
incluye `jti`, dura 24 horas y continúa siendo válido después de logout. El
desbloqueo de Master Key se asocia únicamente con `accountId`, por lo que se
comparte entre navegadores y dispositivos de la misma cuenta.

Se necesita conservar autenticación stateless para las solicitudes comunes,
pero también poder rotar credenciales, cerrar una sesión, detectar reutilización
de refresh tokens y aislar secretos temporales por dispositivo.

## Decisión

Cada login crea una sesión autenticada con un `sessionId` UUID v7. El access
token será un JWT firmado que incluirá como mínimo:

- `sub`: identificador estable de la cuenta.
- `sid`: identificador de la sesión.
- `jti`: identificador único del access token.
- `iat` y `exp`: emisión y expiración.
- `roles`: autoridades concedidas por el servidor.
- `iss` y `aud`: emisor y audiencia configurados.

El access token tendrá una duración predeterminada de 15 minutos, configurable
por ambiente. Cada sesión tendrá un refresh token opaco, aleatorio y de un solo
uso, con una duración predeterminada de 30 días. Solo se persistirá un hash del
refresh token.

Cada renovación rotará el refresh token dentro de la misma familia. Intentar
reutilizar un token ya rotado fuera de la ventana de concurrencia revocará toda
la sesión y sus desbloqueos de Master Key.

Las rutas canónicas serán:

| Método | Ruta | Propósito |
|---|---|---|
| `POST` | `/api/auth/refresh` | Rotar el refresh token y emitir un nuevo par |
| `DELETE` | `/api/auth/sessions/current` | Revocar únicamente la sesión autenticada |
| `DELETE` | `/api/auth/sessions` | Revocar todas las sesiones de la cuenta |

La ruta histórica `/api/auth/logout` delegará temporalmente en el cierre de la
sesión actual. Cerrar todas las sesiones también invalidará todos los
desbloqueos de Master Key de la cuenta.

## Ciclo de vida

1. Login válido crea la sesión y emite access y refresh token.
2. Cada refresh valida sesión, expiración y hash, rota el token y emite un nuevo
   access token con un `jti` diferente.
3. Logout revoca la sesión actual, elimina su refresh token, invalida su sesión
   de Master Key y bloquea el access token actual hasta `exp`.
4. Un cambio de contraseña revoca todas las sesiones de la cuenta.
5. Un cambio de Master Key invalida todos los desbloqueos de Master Key, pero no
   cierra por sí mismo las sesiones de autenticación.

La lista temporal de access tokens revocados se almacenará en Redis con TTL
igual al tiempo restante del token. La sesión y el hash del refresh token se
persistirán para sobrevivir reinicios y permitir auditoría. Nunca se guardarán
tokens completos ni se escribirán en logs.

## Rotación y concurrencia

La fila de sesión se bloqueará con `SELECT ... FOR UPDATE` durante la rotación.
La validación del hash actual y su reemplazo ocurrirán dentro de esa misma
transacción. No se implementará una secuencia de leer y actualizar sin control
de concurrencia.

Además del hash actual se conservarán temporalmente:

- `previous_refresh_token_hash`;
- `previous_rotated_at`;
- `refresh_token_expires_at`;
- el estado de revocación de la familia.

Después de una rotación, el hash anterior será reconocible durante cinco
segundos:

1. La primera solicitud válida rota el token y confirma la transacción.
2. Una solicitud concurrente con el token anterior durante esos cinco segundos
   recibe `409 Conflict` con código `REFRESH_ALREADY_ROTATED`. No se emite otro
   par y no se revoca la sesión.
3. Reutilizar el token anterior después de la ventana se considera replay:
   revoca toda la familia, bloquea los access tokens activos conocidos e
   invalida el desbloqueo de Master Key.

La ventana es configurable, pero debe ser corta y medirse desde la hora del
servidor mediante un `Clock` inyectado. Las pruebas incluirán dos renovaciones
concurrentes y reutilización posterior a la ventana.

## Transporte

- Navegador: access token en la cookie `token`, con `Path=/`, `HttpOnly`,
  `SameSite=Lax`, duración de 15 minutos y `Secure` en producción. Refresh token
  en `refresh_token`, con `Path=/api/auth/refresh`, `HttpOnly`, `SameSite=Lax`,
  duración de 30 días y `Secure` en producción. El body nunca expone esos
  tokens al navegador.
- Cliente nativo: login y refresh conservan los campos `token`, `tokenType` y
  `expiresIn` del contrato actual, y añaden `refreshToken` y
  `refreshExpiresIn`. El access token se envía posteriormente como
  `Authorization: Bearer` y el refresh token solo en el body de
  `POST /api/auth/refresh`.
- Las solicitudes autenticadas por cookie usarán protección CSRF. Una petición
  se considerará nativa solo cuando presente un Bearer válido; no se decidirá
  por `User-Agent`.
- CORS no se considerará una defensa CSRF.

Al cerrar una sesión, el servidor limpia ambas cookies usando exactamente el
mismo `Path`, `Secure` y `SameSite` con los que se crearon. El endpoint de
refresh requiere CSRF para navegador, pero no exige un access token vigente;
la identidad se obtiene del refresh token rotativo.

## Aislamiento de Master Key

El desbloqueo se indexará como `master-key:{accountId}:{sessionId}`. Verificar,
consultar o cerrar el gestor actuará únicamente sobre la sesión autenticada.
Cerrar una sesión de autenticación eliminará el desbloqueo asociado. La
implementación en memoria tendrá la misma expiración funcional que Redis.

## Consecuencias

Logout pasa a tener efecto real, cada dispositivo verifica su propia Master Key
y es posible revocar sesiones comprometidas. A cambio, auth deja de ser
completamente stateless y requiere almacenamiento de sesiones, rotación segura,
limpieza por expiración y pruebas de concurrencia y reutilización.
