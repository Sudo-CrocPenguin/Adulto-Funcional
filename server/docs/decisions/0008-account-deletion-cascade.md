# ADR 0008: Eliminación de cuenta mediante cascadas referenciales

- Estado: aceptada
- Implementación: completa
- Fecha: 2026-08-11
- Alcance: `account`, persistencia, sesiones y datos dependientes
- Reemplaza: orden explícito de eliminación de cuenta del ADR 0003

## Contexto

El ADR 0003 exigió cargar y eliminar explícitamente movimientos, eventos,
gastos, credenciales y categorías antes de borrar la cuenta, dejando las claves
foráneas en cascada únicamente como garantía adicional.

La implementación posterior incorporó cascadas referenciales para evitar
materializar colecciones potencialmente grandes y simplificar la operación
transaccional. El caso de uso actual bloquea la cuenta, reautentica y elimina
directamente la raíz.

## Decisión

La cuenta es la raíz de agregación para su eliminación irreversible:

1. se consulta con bloqueo para impedir cambios concurrentes;
2. se verifica `currentPassword` contra Argon2;
3. dentro de la transacción se elimina la fila `accounts`;
4. MariaDB elimina mediante `ON DELETE CASCADE` roles, sesiones, categorías
   personales, movimientos, gastos fijos, eventos y credenciales;
5. después se limpia el estado efímero de Master Key de la cuenta.

Las categorías `SYSTEM` no tienen propietario y no se eliminan. Si la sentencia
SQL o una cascada falla, la transacción se revierte. No se ejecutan borrados
globales por identificadores proporcionados por el cliente.

## Migración y compatibilidad

- V4 añadió cascada para roles y sesiones de autenticación.
- V6 añadió cascada para categorías personales.
- V10 añadió cascada para movimientos, gastos fijos, eventos y credenciales.

Las migraciones publicadas no se modifican. Una nueva relación dependiente de
cuenta debe definir y probar su política antes de desplegarse; no se asume que
queda cubierta automáticamente.

## Consecuencias

- La eliminación usa una sentencia de raíz y trabajo referencial en la base.
- El consumo de memoria no crece con la cantidad de recursos de la cuenta.
- La corrección depende de que todas las FK de datos personales tengan la
  política prevista.
- Backups externos pueden conservar datos hasta cumplir su retención y deben
  tratarse por separado de la operación API.

## Evidencia

- `DeleteAccountUseCase` reautentica, elimina por ID y limpia Master Key.
- Las migraciones V4, V6 y V10 contienen las cascadas aplicables.
- Las pruebas de ownership impiden eliminar cuentas ajenas y validan la
  reautenticación.
