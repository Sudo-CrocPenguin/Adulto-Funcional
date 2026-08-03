# ADR 0005: Paginación, tiempo y precisión monetaria

- Estado: aceptada
- Implementación: completa
- Fecha: 2026-07-31
- Alcance: contratos REST, dominio y persistencia

## Contexto

Los listados cargan colecciones completas y aplican filtros en memoria. Algunos
DTO anuncian paginación y orden, pero la implementación los ignora. Tampoco hay
una política única para zonas horarias ni una validación que corresponda con
los importes `DECIMAL(10,2)` de MariaDB.

Estas decisiones deben ser comunes antes de optimizar repositorios para evitar
contratos distintos por módulo.

## Paginación y orden

Los listados públicos aceptarán `page`, `size`, `sortBy` y `sortDirection`:

- `page`: base cero y valor predeterminado `0`.
- `size`: valor predeterminado `20` y máximo `100`.
- `sortBy`: únicamente campos de una lista permitida por endpoint.
- `sortDirection`: `ASC` o `DESC`.

Filtros, orden y límites se aplicarán en SQL. Cada orden añadirá el UUID como
último desempate para que el resultado sea determinista. Parámetros fuera de
rango o campos de orden desconocidos producirán `400 Bad Request`.

Para mantener compatibilidad, `data` continuará siendo una lista y la
metainformación se añadirá al sobre de respuesta:

```json
{
  "status": 200,
  "message": "Movimientos listados exitosamente",
  "data": [],
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 0,
    "totalPages": 0,
    "hasNext": false,
    "hasPrevious": false
  }
}
```

Los clientes actuales pueden ignorar `page`. Offset pagination será el primer
contrato estable. Los historiales de gran volumen podrán incorporar después un
endpoint o modo keyset basado en `(campoOrden, id)`, sin cambiar silenciosamente
la semántica existente.

## Tiempo y zona horaria

- Timestamps técnicos como creación, actualización, expiración y auditoría se
  representarán con `Instant`, se almacenarán en UTC y se expondrán en ISO 8601
  con sufijo `Z`.
- Fechas puramente de negocio, como fecha de movimiento o vencimiento, seguirán
  usando `LocalDate`.
- Horas de agenda representan tiempo civil. El contrato incluirá una zona IANA
  explícita cuando la operación dependa de zona o recurrencia. Internamente se
  conservarán el instante normalizado y la zona necesaria para reconstruir la
  hora local.
- Datos antiguos sin zona se interpretarán mediante una zona de aplicación
  configurable, inicialmente `America/Bogota`, durante una migración
  documentada.
- La lógica obtendrá el tiempo mediante un `Clock` inyectado; no llamará
  directamente a `now()` dentro de reglas de negocio.

Los rangos son inclusivos y deben cumplir `startDate <= endDate`. Un evento con
horas requiere `start < end`; un recordatorio debe ocurrir antes del inicio.

## Dinero

Los importes usarán `BigDecimal` en Java y `DECIMAL(10,2)` en MariaDB mientras
ese límite siga vigente. Las entradas deben:

- ser mayores que cero;
- tener como máximo ocho dígitos enteros y dos decimales;
- rechazarse con `400` cuando excedan escala o precisión, sin redondeo
  silencioso;
- evitar conversiones intermedias a `float` o `double`.

La aplicación opera inicialmente con una única moneda configurada. No se
mezclarán monedas ni se añadirá una conversión implícita hasta que exista un
modelo de moneda explícito.

## Consecuencias

Los listados tendrán consumo acotado y un orden reproducible, las reglas de
fecha podrán probarse de forma determinista y los errores de precisión se
detectarán antes de llegar a la base de datos. Implementar esta decisión exige
adaptar el sobre de respuesta, consultas, DTO, índices y pruebas de contrato.
