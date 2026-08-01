# ADR 0006: Semántica HTTP de los errores de Master Key

- Estado: aceptada
- Fecha: 2026-08-01
- Alcance: módulos `security` y `shared`
- Reemplaza: clasificación de Master Key incorrecta del ADR 0004

## Contexto

El ADR 0001 asigna `403 Forbidden` a una Master Key incorrecta y reserva
`401 Unauthorized` para fallos de autenticación. La matriz del ADR 0004
incluyó posteriormente una Master Key incorrecta dentro de `401`, creando dos
contratos incompatibles para el mismo fallo.

La Master Key no autentica la cuenta ante la API. Es un segundo secreto que
autoriza el acceso a la bóveda después de que la identidad ya fue establecida
por una sesión válida. Rechazar ese secreto no convierte al principal en una
identidad anónima ni invalida su sesión de autenticación.

## Decisión

Prevalece la semántica especializada del ADR 0001:

| Situación | Estado | Código estable |
|---|---:|---|
| Master Key incorrecta | `403 Forbidden` | `MASTER_KEY_INVALID` |
| Bóveda no desbloqueada en la sesión actual | `403 Forbidden` | `MASTER_KEY_REQUIRED` |
| Reautenticación principal fallida al configurar o cambiar la clave | `403 Forbidden` | `REAUTHENTICATION_FAILED` |
| Login incorrecto | `401 Unauthorized` | `AUTHENTICATION_FAILED` |
| JWT ausente | `401 Unauthorized` | `AUTHENTICATION_REQUIRED` |
| JWT inválido, expirado o revocado | `401 Unauthorized` | Código específico del fallo |

Un error de Master Key no cerrará ni revocará la sesión de autenticación. Los
controles de intentos podrán responder `429 Too Many Requests` sin cambiar la
clasificación del secreto rechazado.

La ruta histórica
`POST /api/security/passwords/master-key/verify` adoptará inmediatamente el
estado `403` y el código `MASTER_KEY_INVALID`. La ruta se conserva durante la
ventana de compatibilidad definida por el ADR 0001, pero no mantendrá la
semántica incorrecta de `401`.

## Compatibilidad

Los clientes que interpretaban cualquier `401` como una orden para cerrar la
sesión dejarán de hacerlo ante una Master Key incorrecta. Durante la migración,
los consumidores deben usar primero `code` y solo usar el estado HTTP como
clasificación general.

Los campos históricos `status`, `message` y `data` se conservan. Los campos
`code`, `fieldErrors` y `traceId` se añaden conforme al ADR 0004.

## Consecuencias

La autenticación de cuenta y la autorización de la bóveda quedan separadas de
forma explícita. Esto evita cierres de sesión indebidos, permite contabilizar
intentos de Master Key independientemente y elimina la contradicción entre los
dos ADR aceptados.
