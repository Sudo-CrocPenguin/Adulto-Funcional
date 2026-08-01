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

La presencia de un ADR aceptado no significa que ya esté implementado. El
estado de implementación se controla mediante pruebas, commits y la
documentación operativa del módulo.

## Índice

| ADR | Decisión | Estado |
|---|---|---|
| [0001](0001-master-key-contract.md) | Contrato canónico de Master Key | Aceptada |
| [0002](0002-authentication-sessions.md) | Sesiones de autenticación y revocación | Aceptada |
| [0003](0003-category-ownership.md) | Categorías de sistema y personales | Aceptada |
| [0004](0004-api-error-contract.md) | Contrato uniforme de errores HTTP | Aceptada |
| [0005](0005-pagination-time-money.md) | Paginación, tiempo y precisión monetaria | Aceptada |
| [0006](0006-master-key-error-semantics.md) | Semántica HTTP de los errores de Master Key | Aceptada |
