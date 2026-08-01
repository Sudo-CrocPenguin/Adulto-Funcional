# Contrato de errores de la API

Esta guía describe qué devuelve el backend cuando una petición falla, para qué
sirve cada campo y cómo deben consumirlo web, móvil y futuras integraciones. La
decisión arquitectónica se encuentra en el
[ADR 0004](./decisions/0004-api-error-contract.md), y la semántica especializada
de Master Key en los [ADR 0001](./decisions/0001-master-key-contract.md) y
[0006](./decisions/0006-master-key-error-semantics.md).

## Forma de la respuesta

Todo error controlado devuelve `application/json;charset=UTF-8`:

```json
{
  "status": 400,
  "code": "VALIDATION_FAILED",
  "message": "La solicitud contiene datos inválidos",
  "fieldErrors": [
    {
      "field": "email",
      "code": "NotBlank",
      "message": "El email es obligatorio"
    }
  ],
  "traceId": "c7ab2c1f9c1e4c06b19e3798ba2cb200",
  "data": null
}
```

| Campo | Para qué sirve |
|---|---|
| `status` | Estado HTTP numérico. Siempre coincide con la respuesta HTTP. |
| `code` | Identificador estable para decisiones de los clientes. |
| `message` | Explicación segura destinada a una persona. Puede cambiar o traducirse. |
| `fieldErrors` | Lista ordenada de restricciones incumplidas. Está vacía si no aplica. |
| `traceId` | Correlaciona respuesta y logs sin exponer detalles internos. |
| `data` | Siempre es `null` en errores para conservar el contrato histórico. |

Las respuestas exitosas mantienen exclusivamente `status`, `message` y
`data`. De esta forma, añadir metadatos a los errores no altera la forma JSON
que ya consumían los clientes en operaciones exitosas.

## Cómo debe consumirlo un cliente

1. Usar primero el estado HTTP para clasificar el fallo.
2. Usar `code` para decidir la acción concreta.
3. Mostrar `message` cuando no exista un mensaje de interfaz más específico.
4. Asociar `fieldErrors` a los campos del formulario sin asumir el orden de
   llegada de las restricciones; el servidor los ordena por campo y código.
5. Conservar `traceId` al reportar un problema, pero no presentarlo como causa
   del error.

Un cliente no debe cerrar la sesión ante cualquier error de la bóveda. Solo los
códigos de autenticación `AUTHENTICATION_REQUIRED`, `AUTHENTICATION_FAILED`,
`JWT_INVALID` y `JWT_EXPIRED` pertenecen a la identidad de la cuenta.

## Catálogo implementado

### Solicitud y validación

| Estado | Código | Significado |
|---:|---|---|
| 400 | `VALIDATION_FAILED` | Bean Validation encontró uno o más campos inválidos. |
| 400 | `REQUEST_BODY_INVALID` | JSON truncado, ilegible o incompatible con el DTO. |
| 400 | `PARAMETER_INVALID` | Path o query parameter con tipo inválido. |
| 400 | `REQUIRED_PARAMETER_MISSING` | Falta un parámetro, header u otro valor obligatorio. |
| 400 | `BUSINESS_RULE_VIOLATION` | La entrada incumple una regla del dominio. |
| 405 | `METHOD_NOT_ALLOWED` | La ruta existe, pero no admite el método HTTP usado. |
| 406 | `REPRESENTATION_NOT_ACCEPTABLE` | El cliente no acepta un formato disponible. |
| 415 | `MEDIA_TYPE_UNSUPPORTED` | El endpoint no admite el `Content-Type` recibido. |

### Autenticación y autorización

| Estado | Código | Significado |
|---:|---|---|
| 401 | `AUTHENTICATION_REQUIRED` | La ruta requiere una identidad autenticada. |
| 401 | `AUTHENTICATION_FAILED` | Las credenciales de login no son válidas. |
| 401 | `JWT_INVALID` | El JWT está malformado, manipulado o no es válido. |
| 401 | `JWT_EXPIRED` | El JWT superó su vencimiento. |
| 403 | `ACCESS_DENIED` | El principal es válido, pero no tiene el rol o permiso requerido. |

### Master Key

| Estado | Código | Significado |
|---:|---|---|
| 403 | `MASTER_KEY_INVALID` | La clave proporcionada no coincide. No invalida la sesión. |
| 403 | `MASTER_KEY_REQUIRED` | La bóveda no está desbloqueada para operar. |
| 403 | `REAUTHENTICATION_FAILED` | La contraseña principal no superó una reautenticación sensible. |
| 409 | `MASTER_KEY_NOT_CONFIGURED` | La cuenta todavía no tiene Master Key. |

`REAUTHENTICATION_FAILED` forma parte del contrato aceptado y será utilizado
cuando se implemente la configuración y rotación canónica de Master Key.

### Recursos, conflictos y servidor

| Estado | Código | Significado |
|---:|---|---|
| 404 | `RESOURCE_NOT_FOUND` | El recurso no existe o es ajeno al principal. |
| 404 | `ENDPOINT_NOT_FOUND` | No hay una ruta registrada para la petición. |
| 409 | `RESOURCE_CONFLICT` | El estado actual impide la operación. |
| 409 | `DATA_INTEGRITY_CONFLICT` | Una restricción de persistencia impide la operación. |
| 429 | `RATE_LIMIT_EXCEEDED` | Se excedió un límite de intentos o solicitudes. |
| 500 | `INTERNAL_ERROR` | Fallo inesperado no atribuible al cliente. |

`RATE_LIMIT_EXCEEDED` queda reservado para los controles de intentos aceptados
en los ADR de sesiones y Master Key. Las restricciones conocidas de base de
datos podrán recibir códigos más específicos conforme se incorporen sus
migraciones.

## Trazabilidad y seguridad

El servidor genera un identificador aleatorio de 128 bits por petición, lo
incluye en el cuerpo de los errores y en la cabecera `X-Trace-Id`. La política
CORS expone esa cabecera a clientes web.

No se acepta un trace ID enviado por el cliente. Esto evita inyectar valores
arbitrarios en logs. Los errores inesperados se registran internamente con
stack trace y trace ID, pero la respuesta pública nunca incluye excepciones,
SQL, rutas internas, tokens, contraseñas ni Master Keys.

Spring Security conserva sus cabeceras de no-cache. La cobertura HTTP verifica
explícitamente `Cache-Control: no-cache, no-store, max-age=0, must-revalidate`
en respuestas protegidas; no depende de una afirmación documental sin prueba.

## Compatibilidad

Los campos históricos no se eliminan:

- `status` conserva su tipo numérico.
- `message` continúa presente.
- `data` continúa presente y vale `null` en errores.

Los clientes antiguos pueden seguir leyendo esos campos. Los clientes nuevos
deben migrar a `code`; analizar el texto de `message` no constituye un contrato
estable. El cambio intencional de compatibilidad es que una Master Key
incorrecta pasa de `401` a `403`, evitando que los clientes cierren una sesión
de autenticación válida.
