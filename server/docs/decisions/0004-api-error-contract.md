# ADR 0004: Contrato uniforme de errores HTTP

- Estado: aceptada
- Implementación: completa
- Fecha: 2026-07-31
- Alcance: toda la API REST

> La clasificación de una Master Key incorrecta fue reemplazada por el
> [ADR 0006](0006-master-key-error-semantics.md). El resto de esta decisión
> permanece vigente.

## Contexto

Los controladores usan `ApiResponse`, pero los errores producidos dentro de la
cadena de Spring Security no siguen siempre esa estructura. Una petición sin
token puede devolver un cuerpo vacío y una ruta inexistente termina actualmente
en el handler genérico como `500 Internal Server Error`.

Los consumidores necesitan distinguir de forma estable un error de validación,
autenticación, autorización, ausencia, conflicto o límite de solicitudes sin
analizar el texto destinado a personas.

## Decisión

Todos los errores de la API devolverán JSON con `Content-Type:
application/json;charset=UTF-8` y esta estructura:

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
  "traceId": "4c1f5a5e1fc34e0e",
  "data": null
}
```

- `status`: código HTTP numérico, idéntico al estado de la respuesta.
- `code`: identificador estable, en mayúsculas y `SNAKE_CASE`.
- `message`: explicación segura para una persona; no es un identificador de
  programa.
- `fieldErrors`: lista ordenada de errores por campo; vacía cuando no aplica.
- `traceId`: identificador de observabilidad de la petición.
- `data`: `null` en errores para conservar compatibilidad estructural con
  `ApiResponse`.

Los campos `code`, `fieldErrors` y `traceId` se añadirán sin retirar `status`,
`message` ni `data`, evitando romper a los clientes actuales. Las respuestas
exitosas conservarán el contrato existente.

## Matriz HTTP

| Estado | Uso |
|---|---|
| `400 Bad Request` | JSON inválido, parámetros inválidos o validación de campos |
| `401 Unauthorized` | Credenciales ausentes/incorrectas, JWT inválido o Master Key incorrecta |
| `403 Forbidden` | Principal válido sin permiso o solicitud rechazada por la política CORS |
| `404 Not Found` | Ruta inexistente, recurso inexistente o recurso ajeno por UUID |
| `409 Conflict` | Unicidad, estado incompatible o recurso referenciado |
| `429 Too Many Requests` | Límite de intentos o solicitudes excedido |
| `500 Internal Server Error` | Fallo inesperado no atribuible al cliente |

Un recurso ajeno se representará como inexistente para no confirmar su
presencia. Un `401` no distinguirá entre cuenta inexistente y contraseña
incorrecta. `429` incluirá la cabecera `Retry-After` cuando el tiempo sea
conocido.

## Implementación

- Un único componente construirá errores para controladores, validación y
  excepciones de dominio.
- `AuthenticationEntryPoint` y `AccessDeniedHandler` serializarán el mismo
  contrato desde la cadena de seguridad.
- Los rechazos del filtro CORS, incluidas las solicitudes preflight, usarán el
  mismo JSON mediante el código estable `CORS_REQUEST_REJECTED`.
- `NoResourceFoundException` y rutas no registradas producirán `404`.
- Los errores conservarán las cabeceras normativas de HTTP: los `401`
  anunciarán `WWW-Authenticate: Bearer` y los `405` publicarán `Allow`.
- Restricciones de base de datos conocidas se traducirán a códigos `409`
  específicos; los detalles SQL no saldrán del servidor.
- Errores inesperados se registrarán con stack trace y `traceId`, pero la
  respuesta pública será genérica.
- El patrón global de logging incluirá el `traceId` almacenado en MDC para que
  la correlación aplique también a mensajes que no lo interpolan manualmente.
- Ningún log incluirá contraseñas, Master Keys, tokens ni cuerpos sensibles.

## Consecuencias

Web y móvil podrán reaccionar usando `code` sin acoplarse a mensajes. Seguridad,
MVC y dominio tendrán semántica uniforme y cada error será correlacionable con
logs y métricas. La implementación exige pruebas de contrato para todos los
estados de la matriz.
