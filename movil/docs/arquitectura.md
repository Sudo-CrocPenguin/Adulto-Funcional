# Arquitectura

## Resumen

Adulto Funcional Movil es una app Expo/React Native con TypeScript y Expo Router. La arquitectura real del repositorio esta organizada por rutas en `app/` y por capas tecnicas en `src/`.

La mayor parte de la UI vive directamente en las pantallas de `app/`. Aunque existe una carpeta `src/components/`, casi todos sus archivos son placeholders invalidos, excepto `BottomNav`. Por eso la reutilizacion visual todavia es limitada.

## Capas

### `app/`

Contiene rutas y pantallas de Expo Router.

- `app/_layout.tsx` monta proveedores globales.
- `app/(auth)` agrupa pantallas no autenticadas.
- `app/(app)` agrupa pantallas protegidas.
- Las rutas dinamicas usan archivos como `[id].tsx`.

### `src/api/`

Define la comunicacion HTTP con backend.

- `client.ts`: cliente Axios comun.
- `authApi.ts`: login, registro, logout y lectura local de usuario.
- `financesApi.ts`: movimientos, gastos fijos y categorias.
- `agendaApi.ts`: compromisos/eventos.
- `securityApi.ts`: clave maestra y contrasenas.
- `accountApi.ts`: consulta y actualizacion de cuenta.
- `dashboardApi.ts`: mock temporal de datos de dashboard.

### `src/contexts/`

Maneja estado global.

- `AuthContext.tsx`: usuario, autenticacion, login, registro, logout y rachas de login.
- `ThemeContext.tsx`: tema claro/oscuro con persistencia en `AsyncStorage`.
- `MasterKeyContext.tsx`: placeholder invalido.

### `src/hooks/`

Los hooks contienen estado local de cada dominio y conectan pantallas con API.

- `useMovements`: CRUD local/remoto de movimientos.
- `useFixedExpenses`: CRUD de gastos fijos y registro de pagos.
- `useEvents`: CRUD de compromisos y rachas locales por evento.
- `usePasswords`: verificacion de clave maestra y CRUD de contrasenas.
- `useCategories`: carga y creacion de categorias filtradas por tipo.
- `useDashboard`: agrega datos de movimientos, gastos, eventos y contrasenas.
- `useProfile`: arma perfil desde storage y calcula estadisticas.

`useAuth.ts`, `useApi.ts` y `useMasterKey.ts` son placeholders invalidos.

### `src/services/`

Servicios de infraestructura local.

- `storage.ts`: abstraccion de almacenamiento seguro. Usa `SecureStore` en movil y `localStorage` en web.
- `streakService.ts`: calcula racha global de login.
- `validators.ts`, `currencyUtils.ts`, `dateUtils.ts`, `errorHandler.ts`: placeholders invalidos.

### `src/utils/`

Utilidades puras reales.

- `validators.ts`: valida correo, contrasena minima y telefono colombiano movil.
- `currencyUtils.ts`: formatea moneda y partes de moneda.

### `src/constants/`

Constantes compartidas.

- `config.ts`: URL base, timeout, llaves de storage y endpoints.
- `Colors.ts`: paleta principal.
- `Styles.ts`: estilos globales base.
- `enums.ts`: placeholder invalido.

## Flujo de arranque

1. Expo Router entra por `expo-router/entry`, configurado en `package.json`.
2. `app/_layout.tsx` envuelve toda la app con `ThemeProvider` y `AuthProvider`.
3. `AuthProvider` llama `checkAuthStatus` en montaje.
4. `checkAuthStatus` revisa si existe `auth_token` en storage.
5. Si hay token, carga datos locales del usuario y actualiza racha de login.
6. `app/(app)/_layout.tsx` redirige a `/(auth)/login` cuando no hay sesion.
7. Las pantallas autenticadas consumen hooks de dominio y renderizan UI.

## Flujo HTTP

```text
Pantalla
  -> hook de dominio
    -> modulo src/api/*
      -> apiClient Axios
        -> backend
```

`apiClient` agrega token y headers antes de cada request. En respuestas 401/403 elimina el token local.

## Flujo de autenticacion

```text
Login/Register screen
  -> AuthContext.login/register
    -> authApi.login/register
      -> POST backend
      -> storage.setItem(...)
    -> AuthContext setUser + setIsAuthenticated
    -> updateStreak()
    -> router.replace('/(app)')
```

## Flujo de dashboard

`useDashboard` compone tres hooks:

- `useMovements`
- `useFixedExpenses`
- `useEvents`

Cuando los tres terminan de cargar:

- calcula ingresos y egresos totales;
- calcula balance;
- toma los tres proximos gastos fijos;
- toma los tres proximos compromisos;
- calcula datos de grafico para los ultimos tres meses;
- lee `password_count` desde `AsyncStorage`.

## Flujo de gastos fijos

`useFixedExpenses` carga gastos desde backend. Al marcar un gasto como pagado:

1. Crea un movimiento `EXPENSE` con descripcion `Pago: <nombre>`.
2. Calcula la proxima fecha segun frecuencia.
3. Actualiza el gasto fijo con nueva `nextDueDate`.
4. Refresca la lista.

Hay una inconsistencia importante: el tipo `FixedExpense.frequency` en `financesApi.ts` declara valores en espanol (`DIARIO`, `SEMANAL`, `MENSUAL`, `ANUAL`), pero las pantallas y `markAsPaid` usan valores en ingles (`DAILY`, `WEEKLY`, `MONTHLY`, etc.).

## Flujo de compromisos

`useEvents` maneja eventos del backend y rachas locales.

- Lee eventos desde `/api/agenda/events`.
- Lee rachas desde `AsyncStorage` con llaves `event_streaks` y `event_last_completion`.
- Al completar un evento recurrente, incrementa racha local y mueve la fecha del evento a la siguiente ocurrencia.
- Al eliminar un evento, elimina tambien la racha local asociada.

Hay una inconsistencia de estados/prioridades: el tipo del API declara mayusculas (`PENDIENTE`, `COMPLETADO`, `ALTA`), pero pantallas filtran y muestran valores capitalizados (`Pendiente`, `Completado`, `Alta`).

## Flujo de contrasenas

`usePasswords` usa `securityApi`.

1. La pantalla `passwords/index.tsx` exige clave maestra si `user.hasMasterKey` es true.
2. `verifyMasterKey` llama al backend.
3. Si la verificacion pasa, `fetchPasswords` trae la lista.
4. Cada contrasena se muestra oculta.
5. Al tocar revelar, se llama `securityApi.getPassword(id)` para obtener la contrasena descifrada.

La edicion de contrasenas no esta activa porque el archivo de edicion esta como `app/(app)/passwords/[id].tsx.bak`.

## Dependencias principales

- `expo`, `expo-router`, `expo-secure-store`, `expo-notifications`.
- `react`, `react-native`, `react-dom`, `react-native-web`.
- `axios`.
- `@react-native-async-storage/async-storage`.
- `@react-native-community/datetimepicker`.
- `@react-native-picker/picker`.
- `react-native-chart-kit`, `react-native-svg`.
- `@expo/vector-icons`.

## Estado de documentacion vs codigo

Esta documentacion describe el codigo leido en el workspace. No asume comportamientos que no existan. Donde hay mocks, placeholders o rutas incompletas, se indica explicitamente.
