# ADR 0001: Contrato canónico de Master Key

- Estado: aceptada
- Implementación: completa
- Fecha: 2026-07-31
- Alcance: módulo `security`

## Contexto

La Master Key protege las credenciales almacenadas y es independiente de la
contraseña de inicio de sesión. El servidor solo expone actualmente la
verificación bajo `/api/security/passwords/master-key/verify`, mientras los
clientes web y móvil consumen rutas bajo `/api/security/master-key`.

Además, el contrato actual usa un mapa sin validación, no permite consultar el
estado, crear una clave para una cuenta que no la tenga, cerrar únicamente la
sesión activa ni cambiar la clave recifrando las credenciales existentes.

## Decisión

La gestión de la Master Key tendrá un controlador separado del CRUD de
credenciales y la ruta base canónica será `/api/security/master-key`. Todas las
operaciones requieren una cuenta autenticada y obtienen `accountId` y
`sessionId` exclusivamente del contexto de seguridad.

El contrato será:

| Método | Ruta | Propósito | Respuesta exitosa |
|---|---|---|---|
| `GET` | `/status` | Consultar configuración y desbloqueo de la sesión actual | `200` con `configured`, `verified` y `expiresAt` |
| `POST` | `/` | Configurar la primera Master Key | `201` con el estado actualizado |
| `POST` | `/verify` | Desbloquear el gestor en la sesión actual | `200` con el estado actualizado |
| `PATCH` | `/` | Cambiar la Master Key y recifrar todas las credenciales | `200` con el estado actualizado |
| `DELETE` | `/session` | Bloquear el gestor en la sesión actual | `200` con `verified=false` |

Se usarán DTO tipados y validados. Los campos sensibles nunca aparecerán en la
respuesta ni en logs. El estado público tendrá esta forma dentro de `data`:

```json
{
  "configured": true,
  "verified": true,
  "expiresAt": "2026-07-31T21:00:00Z"
}
```

`expiresAt` será `null` cuando no exista una sesión desbloqueada. Los errores
seguirán el contrato uniforme definido para toda la API.

Los DTO de escritura tendrán estos campos mínimos:

- Configuración inicial: `currentPassword` y `newMasterKey`.
- Verificación: `masterKey`.
- Cambio: `currentPassword`, `currentMasterKey` y `newMasterKey`.

`currentPassword` constituye una reautenticación explícita en el momento de la
operación. El servidor la compara con el hash de la contraseña principal y no
la persiste, registra ni reutiliza. De esta forma, un JWT robado no basta para
configurar la primera Master Key ni para reemplazar una existente.

## Reglas de negocio

- Crear una clave cuando ya existe produce `409 Conflict`.
- Verificar o cambiar una clave no configurada produce `409 Conflict`.
- Una Master Key incorrecta produce `403 Forbidden` con el código
  `MASTER_KEY_INVALID`, sin cerrar ni invalidar la sesión de autenticación.
- Intentar operar sobre el gestor sin desbloquear la sesión produce
  `403 Forbidden` con el código `MASTER_KEY_REQUIRED`.
- `401 Unauthorized` queda reservado para fallos de login, JWT ausente,
  inválido, expirado o revocado.
- Configurar o cambiar la Master Key exige reautenticación mediante
  `currentPassword`; una contraseña principal incorrecta produce
  `403 Forbidden` con el código `REAUTHENTICATION_FAILED`.
- Cambiar la clave exige la clave actual y una nueva clave válida.
- El cambio recifra todas las credenciales dentro de una sola transacción. Un
  fallo revierte el hash y todos los cifrados.
- Tras un cambio exitoso se invalidan todas las sesiones desbloqueadas de la
  cuenta. La sesión actual debe verificar nuevamente la nueva clave.
- El formato cifrado almacenará una versión para permitir futuras migraciones
  de algoritmo o parámetros.
- La ruta antigua se retirará después de una ventana de compatibilidad
  documentada; durante esa ventana delegará en el mismo caso de uso.

## Consecuencias

El contrato queda alineado con web y móvil, se elimina lógica de Master Key del
controlador de credenciales y es posible evolucionar la criptografía sin
cambiar las rutas públicas. La implementación requiere aislamiento por sesión,
recifrado transaccional y pruebas con varias sesiones de una misma cuenta.
