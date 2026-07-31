# Aislamiento de recursos por cuenta

## Qué es

El ownership es la regla que limita cada recurso privado a la cuenta que lo
creó. Un UUID identifica el recurso, pero nunca concede acceso por sí mismo. La
autorización efectiva siempre combina el UUID con el `accountId` obtenido del
principal autenticado.

Esta regla se aplica actualmente a movimientos, gastos fijos, eventos y
credenciales del gestor de contraseñas.

## Para qué sirve

El aislamiento evita ataques IDOR, en los que una persona cambia un UUID de la
ruta para consultar, modificar o eliminar información de otra cuenta. También
reduce trabajo innecesario: la base de datos descarta la fila ajena antes de
crear entidades JPA o modelos de dominio.

Un recurso ajeno y uno inexistente producen `404 Not Found`. Así la API no
confirma si un identificador pertenece a otra cuenta.

## Cómo funciona

Las responsabilidades se mantienen separadas por capa:

1. El controlador obtiene `accountId` de `AuthenticatedAccount`; no lo acepta
   desde el cuerpo o query string.
2. El caso de uso solicita una operación limitada por recurso y cuenta.
3. El puerto de dominio expresa esa condición sin depender de JPA.
4. El adaptador de infraestructura ejecuta el filtro en MariaDB.
5. Un resultado vacío o cero filas afectadas se traduce a `NotFoundException`.

Para lectura y actualización, el puerto utiliza esta forma:

```java
Optional<Resource> findByIdAndAccountId(UUID resourceId, UUID accountId);
```

Para eliminación utiliza una única operación atómica:

```java
boolean deleteByIdAndAccountId(UUID resourceId, UUID accountId);
```

La eliminación no realiza primero `exists` o `find` seguido de un DELETE global.
La sentencia incluye ambas condiciones y el caso de uso decide según el número
de filas afectadas.

## Reglas para nuevos recursos

- No exponer en un puerto privado `findById` o `deleteById` sin contexto de
  cuenta cuando la operación proviene de la API.
- No cargar una entidad ajena para comparar `accountId` posteriormente.
- No responder `403` para un UUID ajeno; usar `404`.
- No confiar en un `accountId` enviado por el cliente.
- Los listados deben filtrar por cuenta en SQL y posteriormente incorporar
  paginación.
- Las operaciones de escritura deben ser transaccionales.
- Toda nueva ruta por UUID debe incluir pruebas con cuenta propietaria y cuenta
  ajena.

## Cobertura de regresión

La suite incluye:

- matrices unitarias de GET, PATCH y DELETE para gastos fijos y movimientos;
- pruebas unitarias de eliminación para eventos y credenciales;
- pruebas de integración con MariaDB 11.8 que comprueban consultas, filas
  afectadas y conservación de datos ante una cuenta ajena.

Las pruebas de persistencia son necesarias porque una prueba con mocks no puede
detectar un nombre de columna incorrecto ni confirmar que el filtro se ejecuta
en SQL.
