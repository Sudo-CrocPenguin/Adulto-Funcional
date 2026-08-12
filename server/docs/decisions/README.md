# Decisiones de arquitectura del backend

Este directorio conserva las decisiones que cambian contratos públicos,
límites de seguridad o invariantes compartidas por varios módulos. Su propósito
es explicar qué se decidió, por qué se decidió y qué debe cumplir la
implementación, incluso cuando el código evolucione.

## Cómo se usan

1. Una decisión se documenta antes de iniciar el cambio transversal.
2. Su estado indica si está propuesta, aceptada, reemplazada o retirada.
3. Código, migraciones y pruebas deben enlazar o respetar la decisión aplicable.
4. Una decisión aceptada no se reescribe para ocultar cambios históricos. Una
   decisión posterior debe reemplazarla y explicar la migración.

La aceptación y la implementación son estados distintos. La columna de
implementación refleja el código de esta entrega; una evolución posterior debe
actualizarla sin reescribir el contexto histórico del ADR.

## Índice

| ADR | Decisión | Estado | Implementación |
|---|---|---|---|
| [0001](0001-master-key-contract.md) | Contrato canónico de Master Key | Aceptada | Completa |
| [0002](0002-authentication-sessions.md) | Sesiones de autenticación y revocación | Parcialmente reemplazada por 0007 | Completa salvo detección nativa |
| [0003](0003-category-ownership.md) | Categorías de sistema y personales | Parcialmente reemplazada por 0008 | Completa salvo orden de borrado |
| [0004](0004-api-error-contract.md) | Contrato uniforme de errores HTTP | Aceptada | Completa |
| [0005](0005-pagination-time-money.md) | Paginación, tiempo y precisión monetaria | Aceptada | Completa |
| [0006](0006-master-key-error-semantics.md) | Semántica HTTP de los errores de Master Key | Aceptada | Completa |
| [0007](0007-native-client-detection.md) | Detección de cliente nativo | Aceptada | Completa |
| [0008](0008-account-deletion-cascade.md) | Eliminación de cuenta por cascada | Aceptada | Completa |
| [0009](0009-public-api-https.md) | API pública mediante proxy HTTPS | Aceptada | Completa |

## Evidencia de implementación

- ADR 0001: controlador canónico, aislamiento por sesión y pruebas de ciclo de
  vida/rollback.
- ADR 0002: `auth_sessions`, refresh rotativo, replay, CSRF y revocación.
- ADR 0003: migración V6, scopes, normalización, ownership y consultas SQL.
- ADR 0004: handlers MVC/seguridad/CORS, `traceId` y pruebas de cabeceras.
- ADR 0005: `PageResult`, consultas paginadas, `Clock`, UTC/IANA y validación de
  `DECIMAL(10,2)`.
- ADR 0006: códigos `403` especializados y compatibilidad de la ruta histórica.
- ADR 0007: `ClientTypeResolver`, contrato web/nativo y headers del cliente Expo.
- ADR 0008: caso de uso de borrado y cascadas de V4, V6 y V10.
- ADR 0009: router Traefik, TLS público, API en loopback y cliente móvil sin
  tráfico HTTP claro.

La [referencia API](../API_REFERENCE.md), la [guía de seguridad](../SECURITY.md)
y la [guía operativa](../OPERATIONS.md) describen el comportamiento resultante.
