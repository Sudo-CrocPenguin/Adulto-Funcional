# ADR 0002: Sesiones de autenticación y revocación

- Estado: aceptada
- Fecha: 2026-07-31
- Alcance: módulos `auth`, `security` y configuración de seguridad

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
reutilizar un token ya rotado revocará toda la sesión y sus desbloqueos de
Master Key.

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

## Transporte

- Navegador: access y refresh token en cookies `HttpOnly` y `Secure` en
  producción. La cookie de refresh tendrá ruta limitada al endpoint de
  renovación.
- Cliente nativo: tokens en el cuerpo de la respuesta y `Authorization: Bearer`
  para el access token.
- Las solicitudes autenticadas por cookie usarán protección CSRF. Una petición
  se considerará nativa solo cuando presente un Bearer válido; no se decidirá
  por `User-Agent`.
- CORS no se considerará una defensa CSRF.

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
