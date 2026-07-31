# ADR 0003: Categorías de sistema y categorías personales

- Estado: aceptada
- Fecha: 2026-07-31
- Alcance: módulos `finances`, `agenda` y persistencia

## Contexto

Las categorías actuales son globales y no tienen propietario. Web y móvil
permiten que un usuario cree categorías, pero las mutaciones del servidor
exigen `ROLE_ADMIN`; todas las cuentas reciben únicamente `ROLE_USER`.

También es posible asociar una categoría de agenda a un movimiento financiero
o cambiar el tipo de una categoría referenciada. Esto conserva integridad de
clave foránea, pero rompe la semántica del dominio.

## Decisión

Se conservará una sola tabla de categorías con dos alcances:

- `SYSTEM`: visible para todas las cuentas e inmutable mediante la API de
  usuario.
- `PERSONAL`: visible y mutable solamente por la cuenta propietaria.

La siguiente migración disponible añadirá, sin modificar V1--V3:

- `owner_account_id`, nullable y con clave foránea a `accounts`.
- `category_scope`, con valores `SYSTEM` o `PERSONAL`.
- `normalized_name`, no nullable.
- Un discriminador no nullable para imponer unicidad tanto a categorías del
  sistema como personales aun cuando `owner_account_id` sea `NULL`.

Las filas existentes se migrarán como `SYSTEM`. La base de datos impondrá estas
invariantes:

- `SYSTEM` requiere propietario nulo.
- `PERSONAL` requiere propietario no nulo.
- Nombre normalizado único por propietario, tipo y alcance.
- Tipo y alcance no pueden ser nulos.

El nombre normalizado se obtendrá con Unicode normalizado, espacios exteriores
eliminados, secuencias internas de espacios colapsadas y minúsculas
independientes del locale. El valor original se conservará para presentación.

## Autorización y consultas

`accountId` siempre se obtiene del principal autenticado. Las consultas de
categorías accesibles se limitan en SQL a:

```text
scope = SYSTEM OR owner_account_id = accountId
```

Un usuario con `ROLE_USER` puede crear, renombrar y eliminar sus categorías
`PERSONAL`. No puede crear ni alterar categorías `SYSTEM`, ni leer o mutar una
categoría personal ajena. Un recurso ajeno o no visible se representa como
`404 Not Found`.

El tipo de categoría será inmutable después de crearla. Una eliminación que
viole referencias existentes producirá `409 Conflict`; no habrá eliminación en
cascada de movimientos, eventos o gastos.

## Integridad entre módulos

Crear o actualizar un recurso validará en una sola consulta que la categoría:

1. existe;
2. es `SYSTEM` o pertenece a la cuenta autenticada;
3. tiene el tipo requerido por el módulo.

Movimientos y gastos fijos requieren `FINANCES`; eventos requieren `AGENDA`.
Las respuestas incluirán la categoría accesible completa y nunca resolverán
categorías mediante una consulta global sin contexto de cuenta.

## Consecuencias

Las categorías personalizadas pasan a ser utilizables sin exponer datos entre
cuentas. La unicidad deja de depender de comprobaciones previas vulnerables a
carreras y el tipo deja de poder corromper referencias existentes. Será
necesario adaptar migración, dominio, repositorios, DTO, casos de uso y pruebas
de ownership antes de habilitar las mutaciones para `ROLE_USER`.
