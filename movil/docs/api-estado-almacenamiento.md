# API, estado y almacenamiento

## Configuracion de API

Archivo principal: `src/constants/config.ts`.

Constantes:

- `API_BASE_URL`: URL base hardcodeada del backend.
- `API_TIMEOUT`: timeout Axios en milisegundos.
- `STORAGE_KEYS`: llaves usadas para persistir sesion y datos de usuario.
- `API_ENDPOINTS`: endpoints agrupados por dominio.

Estado local detectado:

- El archivo tiene un cambio no confirmado frente a Git: la URL base paso de un dominio ngrok a `http://38.225.48.28:8083`.
- Existe `.env` con `API_URL` y `API_TIMEOUT`, pero no se consume desde el codigo.

## Cliente Axios

Archivo: `src/api/client.ts`.

Configuracion:

- `baseURL`: `API_BASE_URL`.
- `timeout`: `API_TIMEOUT`.
- Headers base:
  - `Content-Type: application/json`
  - `Accept: application/json`

Interceptor de request:

- Lee `STORAGE_KEYS.TOKEN` desde `storage`.
- Si existe token, asigna `Authorization: Bearer <token>`.
- Agrega `X-Client-Type: mobile`.
- Registra metodo, URL y headers por consola.

Interceptor de response:

- Devuelve respuestas exitosas sin transformar.
- En errores imprime status y data.
- Si status es 401 o 403, elimina `auth_token`.
- Rechaza la promesa para que hooks/pantallas manejen el error.

## Contrato generico de respuesta

Archivo: `src/types/auth.types.ts`.

```ts
export interface ApiResponse<T> {
  status: number;
  message: string;
  data: T;
}
```

Todos los modulos API implementados esperan que el backend responda con `data.data`.

## Autenticacion

Archivo: `src/api/authApi.ts`.

### `login(credentials)`

Endpoint:

- `POST /api/auth/login`

Payload:

- `email`
- `password`

Respuesta esperada:

- `AuthResponse`

Efectos locales:

- Guarda token, token type, expiracion, account id, email, nombres, apellidos, telefono y `hasMasterKey`.

Notas:

- `DEMO_MODE` existe pero esta en `false`.
- Fuerza explicitamente `X-Client-Type: mobile` tambien en esta llamada.

### `register(userData)`

Endpoint:

- `POST /api/auth/register`

Payload:

- `names`
- `lastnames`
- `phone`
- `email`
- `password`
- `masterKey?`

Efectos locales:

- Igual que login: persiste datos de sesion.

### `logout()`

Endpoint:

- `POST /api/auth/logout`

Efectos locales siempre ejecutados:

- Elimina token, token type, expiracion, account id, email, nombres, apellidos, telefono y `hasMasterKey`.

### `isAuthenticated()`

No consulta backend. Devuelve true si existe token local.

### `getUserData()`

No consulta backend. Reconstruye datos del usuario desde storage local.

## Cuenta

Archivo: `src/api/accountApi.ts`.

### `getAccount(accountId)`

Endpoint:

- `GET /api/account/{id}`

### `updateAccount(accountId, data)`

Endpoint:

- `PATCH /api/account/{id}`

Payload:

- `names`
- `lastnames`
- `phone`
- `email`

Nota:

- `profile/edit.tsx` usa `useProfile`, que delega en `accountApi.updateAccount`.

### `changePassword(accountId, data)`

Endpoint:

- `PATCH /api/account/{id}/password`

Payload:

- `currentPassword`
- `newPassword`

### `deleteAccount(accountId)`

Endpoint:

- `DELETE /api/account/{id}`

## Finanzas

Archivo: `src/api/financesApi.ts`.

### Movimientos

Endpoints:

- `GET /api/finances/movements`
- `POST /api/finances/movements`
- `PATCH /api/finances/movements/{id}`
- `DELETE /api/finances/movements/{id}`

Query params soportados en wrapper:

- `startDate`
- `endDate`
- `movementType`

Entidad:

- `id`
- `movementType`: `INCOME` o `EXPENSE`
- `amount`
- `movementDate`
- `description?`
- `category?`

### Gastos fijos

Endpoints:

- `GET /api/finances/fixed-expenses`
- `POST /api/finances/fixed-expenses`
- `PATCH /api/finances/fixed-expenses/{id}`
- `DELETE /api/finances/fixed-expenses/{id}`

Query params soportados:

- `status`
- `categoryId`

Entidad:

- `id`
- `name`
- `category?`
- `frequency`
- `amount`
- `nextDueDate`
- `status`

Estado:

- Las frecuencias y estados usan los valores reales del backend: `WEEKLY`, `BIWEEKLY`, `MONTHLY`, `QUARTERLY`, `SEMIANNUAL`, `ANNUAL`, `ACTIVE`, `INACTIVE`.
- La opcion `DAILY` no se expone porque el backend no la acepta.

### Categorias

Endpoints:

- `GET /api/finances/categories`
- `POST /api/finances/categories`

Entidad:

- `id`
- `name`
- `type`: `FINANCES` o `AGENDA`

Nota:

- Aunque las categorias de agenda se piden mediante este modulo de finanzas, el hook filtra por `type`.

## Agenda

Archivo: `src/api/agendaApi.ts`.

Endpoints:

- `GET /api/agenda/events`
- `POST /api/agenda/events`
- `PATCH /api/agenda/events/{id}`
- `DELETE /api/agenda/events/{id}`

Query params soportados:

- `status`
- `priority`
- `categoryId`

Entidad `Event`:

- `id`
- `title`
- `priority`: `Baja`, `Media`, `Alta`
- `eventDate`
- `frequency`
- `reminder`
- `startHour`
- `endHour`
- `description?`
- `status`: `Pendiente`, `Completado`, `Cancelado`, `Pospuesto`
- `category?`
- `streak?`
- `lastCompletionDate?`

Nota:

- `streak` y `lastCompletionDate` son enriquecidos localmente por `useEvents`; no se deben enviar al backend.

## Seguridad y contrasenas

Archivo: `src/api/securityApi.ts`.

### Clave maestra

Endpoints:

- `POST /api/security/master-key`
- `POST /api/security/master-key/verify`
- `PATCH /api/security/master-key`
- `DELETE /api/security/master-key/session`
- `GET /api/security/master-key/status`

Notas:

- `createMasterKey(masterKey)` crea la clave despues del registro cuando la cuenta no la tiene.
- `verifyMasterKey(masterKey)` activa la sesion de clave para leer contrasenas.
- `changeMasterKey(currentMasterKey, newMasterKey)` cambia la clave y recifra las contrasenas en backend.
- `clearMasterKeySession()` bloquea de nuevo el gestor.

### Contrasenas

Endpoints:

- `GET /api/security/passwords`
- `GET /api/security/passwords/{id}`
- `POST /api/security/passwords`
- `PATCH /api/security/passwords/{id}`
- `DELETE /api/security/passwords/{id}`

Entidad:

- `id`
- `applicationName`
- `password?`
- `lastChangeDate`

Notas:

- La lista puede venir sin `password`.
- El valor descifrado se solicita individualmente con `getPassword(id)`.

## Dashboard API mock

Archivo: `src/api/dashboardApi.ts`.

Contiene funciones mock:

- `getDashboardSummary`
- `getMonthlyStats`
- `getUpcomingFixedExpenses`
- `getUpcomingEvents`

Actualmente no las usa `useDashboard`, que calcula datos a partir de hooks reales. Este archivo parece preparado para una futura API de dashboard.

## Estado global

### `AuthContext`

Archivo: `src/contexts/AuthContext.tsx`.

Estado:

- `user`
- `isLoading`
- `isAuthenticated`
- `error`
- `streak`
- `maxStreak`

Acciones:

- `login`
- `register`
- `logout`
- `checkAuthStatus`
- `refreshUser`

Dependencias:

- `authApi`
- `streakService`

### `ThemeContext`

Archivo: `src/contexts/ThemeContext.tsx`.

Estado:

- `isDarkMode`
- `colors`

Accion:

- `toggleTheme`

Persistencia:

- `AsyncStorage` key `theme`, valores `dark` o `light`.

Nota:

- `settings.tsx` escribe directamente la misma key, pero no invoca `toggleTheme`.

## Hooks de dominio

### `useMovements`

Depende de:

- `useAuth`
- `financesApi`

Retorna:

- `movements`
- `loading`
- `error`
- `fetchMovements`
- `createMovement`
- `updateMovement`
- `deleteMovement`

Regla:

- Para crear/actualizar transforma `category` a `categoryId` antes de enviar al backend.

### `useFixedExpenses`

Depende de:

- `financesApi`
- `useMovements`

Retorna:

- `expenses`
- `loading`
- `error`
- `fetchExpenses`
- `createExpense`
- `updateExpense`
- `deleteExpense`
- `markAsPaid`

Regla:

- `markAsPaid` crea movimiento de egreso y recalcula `nextDueDate`.

### `useEvents`

Depende de:

- `agendaApi`
- `AsyncStorage`

Retorna:

- `events`
- `loading`
- `error`
- `fetchEvents`
- `createEvent`
- `updateEvent`
- `completeEvent`
- `deleteEvent`

Reglas:

- Formatea prioridad a `Alta`, `Media`, `Baja`.
- Formatea estado a `Pendiente`, `Completado`, `Cancelado`.
- Maneja rachas locales para eventos recurrentes.

### `usePasswords`

Depende de:

- `securityApi`
- `useAuth`
- `AsyncStorage`

Retorna:

- `passwords`
- `loading`
- `error`
- `verifying`
- `hasMasterKey`
- `masterKeyVerified`
- `verifyMasterKey`
- `resetVerification`
- `refreshMasterKeyStatus`
- `createMasterKey`
- `changeMasterKey`
- `fetchPasswords`
- `createPassword`
- `updatePassword`
- `deletePassword`

No retorna flujos de recuperacion por correo porque el backend actual no puede recuperar contrasenas cifradas si se pierde la clave maestra.

### `useCategories`

Depende de:

- `financesApi`

Retorna:

- `categories`
- `loading`
- `error`
- `fetchCategories`
- `createCategory`

Regla:

- Filtra categorias por `type`, por defecto `FINANCES`.

### `useDashboard`

Depende de:

- `useMovements`
- `useFixedExpenses`
- `useEvents`
- `AsyncStorage`

Retorna:

- `data`
- `loading`
- `error: null`

Data calculada:

- `totalIncome`
- `totalExpense`
- `balance`
- `upcomingFixedExpenses`
- `upcomingEvents`
- `streak`
- `chartData`
- `passwordCount`

### `useProfile`

Depende de:

- `storage`
- `AsyncStorage`
- `useMovements`
- `useFixedExpenses`
- `useEvents`

Retorna:

- `profile`
- `loading`
- `error`
- `stats`
- `fetchProfile`
- `updateProfile`
- `changePassword`
- `deleteAccount`

Notas:

- `fetchProfile` consulta `GET /api/account/{id}` y sincroniza storage local.
- `updateProfile` usa `PATCH /api/account/{id}`.
- `changePassword` usa `PATCH /api/account/{id}/password`.
- `deleteAccount` usa `DELETE /api/account/{id}`.

## Storage seguro

Archivo: `src/services/storage.ts`.

Comportamiento:

- Web: usa `localStorage`.
- Movil: usa `expo-secure-store`.

Metodos:

- `setItem`
- `getItem`
- `deleteItem`

Uso principal:

- Token.
- Datos de usuario.
- Datos de sesion.

## AsyncStorage

Llaves detectadas:

- `theme`: tema claro/oscuro.
- `notifications`: preferencias de notificaciones.
- `password_count`: contador de contrasenas guardadas.
- `last_login_date`: ultima fecha de login.
- `current_streak`: racha actual de login.
- `max_streak`: racha maxima de login.
- `event_streaks`: rachas por evento recurrente.
- `event_last_completion`: ultima fecha de completado por evento recurrente.

## Assets y notificaciones

`app.json` configura `expo-notifications` con:

- `./assets/images/notification-icon.png`
- `./assets/sounds/notification.wav`

Pero en `assets/` solo existen:

- `assets/images/favicon.png`
- `assets/images/splash-icon.png`
- `assets/images/icon.png`
- `assets/images/adaptive-icon.png`
- `assets/fonts/SpaceMono-Regular.ttf`

Por tanto, la configuracion de notificaciones referencia archivos ausentes.
