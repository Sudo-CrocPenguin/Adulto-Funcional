# Documentación del esquema de base de datos

## 1. Tabla accounts

Almacena las cuentas de usuario (titulares). Es la entidad central del sistema.

| Columna            | Tipo         | Restricciones        | Descripción                                                                                                              |
| ------------------ | ------------ | -------------------- | ------------------------------------------------------------------------------------------------------------------------ |
| account_id         | CHAR(36)     | NOT NULL PRIMARY KEY | Identificador único de la cuenta (UUID v7 generado por la aplicación).                                                   |
| account_names      | VARCHAR(50)  | NOT NULL             | Nombres del titular.                                                                                                     |
| account_lastnames  | VARCHAR(50)  | NOT NULL             | Apellidos del titular.                                                                                                   |
| account_email      | VARCHAR(255) | NOT NULL UNIQUE      | Correo electrónico, utilizado como username en Spring Security.                                                          |
| account_phone      | VARCHAR(20)  | NOT NULL             | Número de teléfono de contacto.                                                                                          |
| account_password   | VARCHAR(255) | NOT NULL             | Hash de la contraseña de inicio de sesión (Argon2).                                                                      |
| account_master_key | VARCHAR(255) | NULL                 | Hash de la clave maestra opcional (Argon2). Se usa para autenticar al usuario antes de acceder al gestor de contraseñas. |
| account_created_at | TIMESTAMP    | NOT NULL             | Fecha de creación de la cuenta (asignada por la aplicación, no por la BD).                                               |

### Índices

- Clave primaria `account_id`
- Índice único implícito en `account_email`

---

## 2. Tabla categories

Categorías para clasificar movimientos, gastos fijos y eventos.

| Columna             | Tipo        | Restricciones        | Descripción                                                  |
| ------------------- | ----------- | -------------------- | ------------------------------------------------------------ |
| category_id         | CHAR(36)    | NOT NULL PRIMARY KEY | Identificador único (UUID v7 generado por la aplicación).    |
| category_name       | VARCHAR(50) | NOT NULL             | Nombre de la categoría (ej. "Alimentación", "Transporte").   |
| category_type       | VARCHAR(20) | NOT NULL             | Tipo de categoría (puede ser INGRESO, GASTO, AHORRO, etc.).  |
| category_created_at | TIMESTAMP   | NOT NULL             | Fecha de creación (asignada por la aplicación).              |
| category_deleted_at | TIMESTAMP   | NULL DEFAULT NULL    | Fecha de borrado lógico (si NULL, la categoría está activa). |

---

## 3. Tabla movements

Registra los movimientos financieros (ingresos y gastos) de cada cuenta.

| Columna                 | Tipo          | Restricciones        | Descripción                                     |
| ----------------------- | ------------- | -------------------- | ----------------------------------------------- |
| movement_id             | CHAR(36)      | NOT NULL PRIMARY KEY | Identificador único (UUID v7).                  |
| movement_type           | VARCHAR(20)   | NOT NULL             | Tipo: INGRESO, GASTO, TRANSFERENCIA.            |
| movement_amount         | DECIMAL(10,2) | NOT NULL             | Monto del movimiento.                           |
| movement_register_date  | TIMESTAMP     | NOT NULL             | Fecha de registro (asignada por la aplicación). |
| movement_description    | TEXT          | NULL                 | Descripción opcional del movimiento.            |
| movement_date           | DATE          | NOT NULL             | Fecha en que ocurrió el movimiento.             |
| movement_fk_account_id  | CHAR(36)      | NULL                 | Clave foránea a `accounts(account_id)`.         |
| movement_fk_category_id | CHAR(36)      | NULL                 | Clave foránea a `categories(category_id)`.      |

### Índices adicionales

- `idx_movements_account` → `movement_fk_account_id`
- `idx_movements_date` → `movement_date`

---

## 4. Tabla fixed_expenses

Gastos fijos recurrentes (mensuales, semanales, etc.) asociados a una cuenta.

| Columna                      | Tipo          | Restricciones        | Descripción                                        |
| ---------------------------- | ------------- | -------------------- | -------------------------------------------------- |
| fixed_expense_id             | CHAR(36)      | NOT NULL PRIMARY KEY | Identificador único.                               |
| fixed_expense_name           | VARCHAR(20)   | NOT NULL             | Nombre del gasto fijo (ej. "Netflix", "Arriendo"). |
| fixed_expense_frequency      | VARCHAR(15)   | NOT NULL             | Frecuencia: MENSUAL, SEMANAL, QUINCENAL, ANUAL.    |
| fixed_expense_amount         | DECIMAL(10,2) | NOT NULL             | Monto del gasto.                                   |
| fixed_expense_status         | VARCHAR(15)   | NOT NULL             | Estado: ACTIVO, PAGADO, VENCIDO, CANCELADO.        |
| fixed_expense_closing_date   | DATE          | NOT NULL             | Fecha de cierre o próxima fecha de pago.           |
| fixed_expense_fk_category_id | CHAR(36)      | NULL                 | FK a `categories`.                                 |
| fixed_expense_fk_account_id  | CHAR(36)      | NULL                 | FK a `accounts`.                                   |

### Índices adicionales

- `idx_fixed_expenses_account` → `fixed_expense_fk_account_id`

---

## 5. Tabla events

Eventos de la agenda (citas, recordatorios, tareas).

| Columna              | Tipo        | Restricciones        | Descripción                               |
| -------------------- | ----------- | -------------------- | ----------------------------------------- |
| event_id             | CHAR(36)    | NOT NULL PRIMARY KEY | Identificador único.                      |
| event_title          | VARCHAR(35) | NOT NULL             | Título del evento.                        |
| event_priority       | VARCHAR(15) | NULL DEFAULT 'Media' | Prioridad: Alta, Media, Baja.             |
| event_date           | DATE        | NOT NULL             | Fecha en que ocurre el evento.            |
| event_frequency      | INT         | NOT NULL             | Días de recurrencia (0 = no recurrente).  |
| event_reminder       | DATETIME    | NOT NULL             | Fecha/hora del recordatorio.              |
| event_start_hour     | DATETIME    | NOT NULL             | Fecha/hora de inicio.                     |
| event_end_hour       | DATETIME    | NOT NULL             | Fecha/hora de fin.                        |
| event_description    | TEXT        | NULL                 | Descripción detallada.                    |
| event_status         | VARCHAR(20) | DEFAULT 'Pendiente'  | Estado: Pendiente, Completado, Cancelado. |
| event_fk_category_id | CHAR(36)    | NULL                 | FK a `categories`.                        |
| event_fk_account_id  | CHAR(36)    | NULL                 | FK a `accounts`.                          |

### Índices adicionales

- `idx_events_account` → `event_fk_account_id`
- `idx_events_date` → `event_date`

---

## 6. Tabla passwords (Gestor de contraseñas)

Almacena las credenciales de aplicaciones externas cifradas con AES‑256 mediante derivación de clave maestra. La seguridad de este módulo se basa en que la clave maestra del usuario no se guarda en la BD, solo su hash (en `accounts.account_master_key`). Cada secreto se cifra con una clave AES derivada de la clave maestra + un `password_salt` único.

| Columna                   | Tipo            | Restricciones        | Descripción                                                                                                                    |
| ------------------------- | --------------- | -------------------- | ------------------------------------------------------------------------------------------------------------------------------ |
| password_id               | CHAR(36)        | NOT NULL PRIMARY KEY | Identificador único.                                                                                                           |
| password_application_name | VARCHAR(35)     | NOT NULL             | Nombre de la aplicación o sitio web (ej. "Facebook", "Gmail").                                                                 |
| password_salt             | VARCHAR(255)    | NOT NULL             | Valor aleatorio único (generalmente Base64) que se combina con la clave maestra para derivar la clave AES de este secreto.     |
| password_iv               | BINARY(16)      | NOT NULL             | Vector de inicialización (IV) de 16 bytes usado en el cifrado.                                                                 |
| password_ciphertext       | VARBINARY(2048) | NOT NULL             | Texto cifrado (nombre de usuario + contraseña) protegido con AES‑256-GCM o CBC. Incluye el tag de autenticación si se usa GCM. |
| password_last_change_date | DATE            | NULL                 | Fecha de la última modificación de esta credencial.                                                                            |
| passwords_fk_account_id   | CHAR(36)        | NULL                 | FK a `accounts`.                                                                                                               |

### Índices adicionales

- `idx_passwords_account` → `passwords_fk_account_id`

---

## Notas de diseño y seguridad

- **Generación de IDs**: Todos los `*_id` son UUID v7 generados en la aplicación (no por la base de datos). Esto permite controlar la identidad desde el dominio y garantiza ordenación temporal.
- **Fechas**: Las columnas de tipo `TIMESTAMP` (`account_created_at`, `category_created_at`, `movement_register_date`, etc.) son asignadas por la aplicación mediante `@PrePersist` en las entidades JPA. La base de datos no aplica `DEFAULT CURRENT_TIMESTAMP` para mantener la coherencia.
- **Borrado lógico**: En `categories`, la columna `category_deleted_at` permite borrado lógico sin pérdida de referencias históricas.
- **Cifrado de contraseñas externas**:
  - La clave maestra del usuario se ingresa en cada sesión (o se deriva de la contraseña de login) y nunca se persiste.
  - Para cada credencial se genera un salt aleatorio, un IV aleatorio, y se deriva una clave AES de 256 bits usando HKDF (o PBKDF2) a partir de la clave maestra y el salt.
  - El texto cifrado almacena el par `usuario:contraseña` (o cualquier otro formato) y, si se usa AES‑GCM, también el tag de autenticación.
  - Un atacante que obtenga acceso completo a la base de datos no podrá descifrar ninguna credencial sin conocer la clave maestra del usuario correspondiente.
- **Índices**: Los índices adicionales mejoran el rendimiento en consultas por `account_id` (muy frecuentes) y por fechas en movimientos y eventos.
- **Migraciones**: El esquema se gestiona mediante Flyway (`src/main/resources/database/migrations/`), siguiendo las convenciones de versionado del proyecto (referenciado en README.md).
