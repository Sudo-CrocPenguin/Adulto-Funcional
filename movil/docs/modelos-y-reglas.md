# Modelos y reglas de negocio

## Usuario y autenticacion

### `LoginRequest`

Campos:

- `email`
- `password`

Validaciones en pantalla:

- Email requerido.
- Formato de email valido.
- Contrasena requerida.
- Longitud minima de 8 caracteres.

### `RegisterRequest`

Campos:

- `names`
- `lastnames`
- `phone`
- `email`
- `password`
- `masterKey?`

Validaciones en pantalla:

- Nombres requeridos.
- Apellidos requeridos.
- Telefono requerido.
- Telefono colombiano valido.
- Email requerido y valido.
- Contrasena minima de 8 caracteres.
- Confirmacion de contrasena igual.
- Clave maestra obligatoria.
- Clave maestra minima de 8 caracteres.
- Confirmacion de clave maestra igual.

### `AuthResponse`

Campos:

- `token`
- `tokenType`
- `expiresIn`
- `accountId`
- `names`
- `lastnames`
- `email`
- `phone`
- `createdAt`
- `hasMasterKey`

Regla:

- Estos datos se persisten en storage despues de login o registro.

## Perfil

### `UserProfile`

Campos:

- `id`
- `names`
- `lastnames`
- `email`
- `phone`
- `createdAt`

Origen actual:

- Storage local, no backend directo.

Reglas:

- Editar perfil deberia enviar `PATCH /api/account/{id}`.
- Despues del PATCH se actualiza storage y `AuthContext`.

Problema actual:

- Los inputs de `profile/edit.tsx` no actualizan estado, solo limpian errores.

## Movimiento financiero

Archivo base: `src/api/financesApi.ts`.

Campos:

- `id`
- `movementType`
- `amount`
- `movementDate`
- `description?`
- `category?`

Tipos:

- `movementType`: `INCOME` o `EXPENSE`.

Reglas:

- Para crear un movimiento, `useMovements` transforma `category` en `categoryId`.
- El dashboard suma movimientos `INCOME` como ingresos y `EXPENSE` como egresos.
- La pantalla de finanzas filtra por tipo.
- Un pago de gasto fijo crea un movimiento `EXPENSE`.

Validaciones de nuevo movimiento:

- Monto debe existir y ser mayor que 0.
- Debe haber categoria seleccionada.
- La categoria seleccionada debe existir en la lista local.

## Gasto fijo

Campos:

- `id`
- `name`
- `category?`
- `frequency`
- `amount`
- `nextDueDate`
- `status`

Frecuencias en pantallas:

- `WEEKLY`
- `BIWEEKLY`
- `MONTHLY`
- `QUARTERLY`
- `SEMIANNUAL`
- `ANNUAL`

Estados en pantallas:

- `ACTIVE`
- `INACTIVE`

Estados declarados en tipo:

- `ACTIVE`
- `INACTIVE`

Frecuencias declaradas en tipo:

- `WEEKLY`
- `BIWEEKLY`
- `MONTHLY`
- `QUARTERLY`
- `SEMIANNUAL`
- `ANNUAL`

Reglas:

- Al crear/actualizar, `useFixedExpenses` convierte `category` a `categoryId`.
- Al crear, intenta convertir `ACTIVO` a `ACTIVE` e `INACTIVO` a `INACTIVE`.
- Al pagar, crea un movimiento de egreso y calcula la siguiente fecha.
- La proxima fecha de pago debe ser posterior a hoy.

Validaciones:

- Nombre requerido.
- Monto numerico mayor que 0.
- Categoria requerida.
- Fecha futura requerida.

Riesgo:

- La mezcla de enums en espanol e ingles puede causar errores de UI, filtros o payloads segun lo que devuelva el backend.

## Categoria

Campos:

- `id`
- `name`
- `type`

Tipos:

- `FINANCES`
- `AGENDA`

Reglas:

- `useCategories('FINANCES')` filtra categorias financieras.
- `useCategories('AGENDA')` filtra categorias de compromisos.
- La creacion de categorias se hace dentro de modales en formularios, no en las rutas `categories`.

Validacion:

- Nombre obligatorio.

## Compromiso / evento

Campos:

- `id`
- `title`
- `priority`
- `eventDate`
- `frequency`
- `reminder`
- `startHour`
- `endHour`
- `description?`
- `status`
- `category?`
- `streak?`
- `lastCompletionDate?`

Frecuencias:

- `0`: evento unico.
- `1`: diario.
- `7`: semanal.
- `30`: mensual aproximado.
- `365`: anual.

Prioridades declaradas:

- `Baja`
- `Media`
- `Alta`

Estados declarados:

- `Pendiente`
- `Completado`
- `Cancelado`
- `Pospuesto`
- `Completado`
- `Cancelado`

Reglas:

- Al crear, se exige titulo y categoria.
- El recordatorio se calcula como un dia antes de la fecha.
- Al completar evento unico, se marca como completado.
- Al completar evento recurrente, se incrementa racha local y se mueve `eventDate` a la siguiente fecha.
- Al eliminar, se borran rachas locales asociadas.

Rachas por evento:

- `event_streaks`: mapa `{ [eventId]: number }`.
- `event_last_completion`: mapa `{ [eventId]: string | null }`.

Riesgo:

- Comparaciones de estado en pantallas usan valores capitalizados, mientras el tipo API declara mayusculas.

## Contrasena guardada

Campos:

- `id`
- `applicationName`
- `password?`
- `lastChangeDate`

Reglas:

- El usuario debe tener `hasMasterKey`.
- La clave maestra debe verificarse antes de cargar contrasenas.
- La lista muestra contrasenas ocultas.
- La contrasena descifrada se obtiene por id solo al revelar.
- El contador local `password_count` se actualiza al cargar, crear y borrar.

Validaciones de nueva contrasena:

- Nombre de aplicacion obligatorio.
- Contrasena obligatoria.

Riesgos:

- La edicion de contrasena esta activa como ruta dinamica.
- La recuperacion de clave maestra por correo no esta disponible; el cambio requiere la clave actual.

## Racha global

Archivo: `src/services/streakService.ts`.

Llaves:

- `last_login_date`
- `current_streak`
- `max_streak`

Regla:

- Si el ultimo login fue hoy, no cambia.
- Si el ultimo login fue ayer, incrementa racha.
- Si no fue ayer, reinicia a 1.
- Actualiza maximo historico.

Uso:

- `AuthContext` llama `updateStreak` al autenticar usuario.

## Tema

Llave:

- `theme`

Valores:

- `dark`
- `light`

Regla:

- `ThemeContext` toma valor guardado si existe.
- Si no existe, usa `useColorScheme`.
- `toggleTheme` guarda la nueva preferencia.

Riesgo:

- `SettingsScreen` cambia la key directamente sin sincronizar con `ThemeContext` durante la sesion actual.

## Notificaciones

Llave:

- `notifications`

Estructura:

```ts
{
  compromisos: boolean;
  finanzas: boolean;
  gastosFijos: boolean;
}
```

Estado:

- Solo se guardan preferencias. No hay programacion real de notificaciones en el codigo de aplicacion.

## Validaciones utilitarias

Archivo: `src/utils/validators.ts`.

### `isValidColombianPhone(phone)`

Acepta:

- `3001234567`
- `+573001234567`
- Espacios o guiones, porque limpia caracteres no numericos excepto `+`.

Regla:

- Debe tener 10 digitos opcionalmente precedidos por `+57`.
- Debe comenzar por `3`.

### `isValidEmail(email)`

Usa regex simple:

```text
^[^\s@]+@[^\s@]+\.[^\s@]+$
```

### `isValidPassword(password)`

Regla:

- Longitud minima de 8.

## Moneda

Archivo: `src/utils/currencyUtils.ts`.

### `formatCurrency(amount, symbol = '$')`

Devuelve string con:

- Separador de miles con punto.
- Dos decimales.

Ejemplo:

- `1000` -> `$1.000.00`

Nota:

- El formato mezcla separador de miles de estilo latino con separador decimal de punto. Si se busca COP/locale colombiano, convendria revisar formato.

### `formatCurrencyParts(amount, symbol = '$')`

Devuelve:

- `integer`
- `decimal`

Uso:

- Permite renderizar parte entera y decimal con estilos distintos.
