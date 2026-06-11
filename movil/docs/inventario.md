# Inventario de archivos

Este inventario describe los archivos del proyecto excluyendo `node_modules`, `package-lock.json` y binarios de assets.

## Raiz

### `package.json`

Define una app privada llamada `adulto-funcional-movil-fork`.

Entrada:

- `expo-router/entry`

Scripts:

- `start`: `expo start`
- `android`: `expo run:android`
- `ios`: `expo run:ios`
- `web`: `expo start --web`

Dependencias destacadas:

- Expo SDK 54.
- React 19.1.
- React Native 0.81.5.
- Expo Router 6.
- Axios.
- AsyncStorage.
- SecureStore.
- DateTimePicker.
- Picker.
- Chart Kit.
- Expo Notifications.

### `app.json`

Configura Expo:

- Nombre y slug.
- Orientacion portrait.
- Iconos y splash.
- Scheme `adultofuncionalmovilfork`.
- iOS bundle id.
- Android package.
- Web con Metro.
- Plugins: `expo-router`, `expo-secure-store`, DateTimePicker y `expo-notifications`.
- Typed routes experimental.

Advertencia:

- Referencia icono y sonido de notificaciones que no existen en `assets/`.

### `tsconfig.json`

Extiende `expo/tsconfig.base`.

Opciones:

- `strict: true`
- `jsx: react-native`
- Alias `@/*` hacia raiz.

Incluye:

- Todos los `.ts` y `.tsx`.
- Tipos generados de Expo.
- `expo-env.d.ts`.

### `.gitignore`

Ignora dependencias, builds Expo/web, carpetas nativas generadas, archivos locales de entorno `.env*.local`, certificados y temporales.

Nota:

- `.env` no esta ignorado por esta regla porque solo ignora `.env*.local`.

### `.env`

Contiene configuracion de backend:

- `API_URL`
- `API_TIMEOUT`

No se documentan valores. El codigo actual no consume este archivo.

### `.vscode/settings.json`

Configura acciones explicitas en save:

- fix all.
- organize imports.
- sort members.

### `.vscode/extensions.json`

Recomienda:

- `expo.vscode-expo-tools`

## Assets

### `assets/images/icon.png`

Icono principal usado por Expo y por pantallas auth.

### `assets/images/adaptive-icon.png`

Icono adaptativo Android.

### `assets/images/splash-icon.png`

Imagen de splash.

### `assets/images/favicon.png`

Favicon web.

### `assets/fonts/SpaceMono-Regular.ttf`

Fuente incluida, no se encontro uso directo en codigo leido.

## Layouts

### `app/_layout.tsx`

Monta `ThemeProvider`, `AuthProvider` y `Stack` raiz.

### `app/(auth)/_layout.tsx`

Stack sin header para rutas publicas.

### `app/(app)/_layout.tsx`

Layout protegido. Muestra loading, redirige a login si no hay sesion o renderiza stack autenticado.

## Pantallas auth

### `app/(auth)/login.tsx`

Login con validaciones locales, logo, campos email/password y enlaces a registro/recuperacion.

### `app/(auth)/register.tsx`

Registro con datos personales, contrasena y clave maestra obligatoria.

### `app/(auth)/forgot-password.tsx`

Recuperacion simulada de contrasena por email.

### `app/(auth)/reset-password.tsx`

Placeholder en construccion.

## Pantallas app

### `app/(app)/index.tsx`

Dashboard con saludo, saldo, estadisticas, racha, proximos items y grafico.

### `app/(app)/finances/index.tsx`

Lista y resumen de movimientos financieros.

### `app/(app)/finances/new.tsx`

Formulario de creacion de movimiento y categoria financiera.

### `app/(app)/finances/[id].tsx`

Placeholder de edicion/detalle de movimiento.

### `app/(app)/fixed-expenses/index.tsx`

Lista de gastos fijos con filtro y accion pagar.

### `app/(app)/fixed-expenses/new.tsx`

Formulario de creacion de gasto fijo.

### `app/(app)/fixed-expenses/[id].tsx`

Formulario de edicion y eliminacion de gasto fijo.

### `app/(app)/compromises/index.tsx`

Lista de compromisos con filtros, completado, eliminacion y rachas.

### `app/(app)/compromises/new.tsx`

Formulario de creacion de compromiso y categoria de agenda.

### `app/(app)/compromises/[id].tsx`

Formulario de edicion, completado y eliminacion de compromiso.

### `app/(app)/passwords/index.tsx`

Gestor de contrasenas protegido por clave maestra.

### `app/(app)/passwords/new.tsx`

Formulario de nueva contrasena.

### `app/(app)/passwords/[id].tsx.bak`

Pantalla de edicion de contrasena no activa como ruta.

### `app/(app)/passwords/master-key/create.tsx`

Placeholder que redirige a perfil.

### `app/(app)/passwords/master-key/verify.tsx`

Placeholder minimo.

### `app/(app)/passwords/master-key/reset-request.tsx`

Pantalla de solicitud de reset de clave maestra, actualmente incompatible con `usePasswords`.

### `app/(app)/passwords/master-key/reset-verify.tsx`

Pantalla para ingresar codigo de verificacion.

### `app/(app)/passwords/master-key/reset-new.tsx`

Pantalla para nueva clave maestra, actualmente incompatible con `usePasswords`.

### `app/(app)/profile/index.tsx`

Perfil, actividad, datos personales y acciones de cuenta.

### `app/(app)/profile/edit.tsx`

Edicion de perfil con bug en inputs.

### `app/(app)/profile/change-password.tsx`

Formulario de cambio de contrasena conectado a un TODO.

### `app/(app)/profile/settings.tsx`

Configuracion local de tema, notificaciones, seguridad y cuenta.

### `app/(app)/profile/delete-account.tsx`

Placeholder.

### `app/(app)/categories/index.tsx`

Placeholder.

### `app/(app)/categories/new.tsx`

Placeholder.

## API

### `src/api/client.ts`

Cliente Axios comun con interceptores de token y errores 401/403.

### `src/api/authApi.ts`

Autenticacion, registro, logout, persistencia de sesion y lectura local de usuario.

### `src/api/accountApi.ts`

Consulta y actualizacion de cuenta.

### `src/api/financesApi.ts`

Movimientos, gastos fijos y categorias.

### `src/api/agendaApi.ts`

Eventos/compromisos.

### `src/api/securityApi.ts`

Clave maestra y contrasenas.

### `src/api/dashboardApi.ts`

Mocks de dashboard, no integrados con `useDashboard`.

### `src/api/endpoints.ts`

Placeholder invalido.

### `src/api/passwordsApi.ts`

Placeholder invalido.

## Contextos

### `src/contexts/AuthContext.tsx`

Estado global de autenticacion y racha de login.

### `src/contexts/ThemeContext.tsx`

Tema claro/oscuro con storage.

### `src/contexts/MasterKeyContext.tsx`

Placeholder invalido.

## Hooks

### `src/hooks/useMovements.ts`

Estado y CRUD de movimientos.

### `src/hooks/useFixedExpenses.ts`

Estado y CRUD de gastos fijos, mas pago como movimiento.

### `src/hooks/useEvents.ts`

Estado y CRUD de compromisos, mas rachas locales.

### `src/hooks/usePasswords.ts`

Estado de contrasenas y verificacion de clave maestra.

### `src/hooks/useCategories.ts`

Estado de categorias filtradas por tipo.

### `src/hooks/useDashboard.ts`

Agregacion de datos para dashboard.

### `src/hooks/useProfile.ts`

Perfil desde storage y estadisticas derivadas.

### `src/hooks/useAuth.ts`

Placeholder invalido.

### `src/hooks/useApi.ts`

Placeholder invalido.

### `src/hooks/useMasterKey.ts`

Placeholder invalido.

## Servicios

### `src/services/storage.ts`

Abstraccion de storage seguro/web.

### `src/services/streakService.ts`

Calculo y persistencia de racha global de login.

### `src/services/validators.ts`

Placeholder invalido.

### `src/services/currencyUtils.ts`

Placeholder invalido.

### `src/services/dateUtils.ts`

Placeholder invalido.

### `src/services/errorHandler.ts`

Placeholder invalido.

## Utilidades

### `src/utils/validators.ts`

Valida telefono colombiano, email y contrasena minima.

### `src/utils/currencyUtils.ts`

Formatea moneda completa o por partes.

## Constantes

### `src/constants/config.ts`

URL base, timeout, storage keys y endpoints.

### `src/constants/Colors.ts`

Paleta principal de la app.

### `src/constants/Styles.ts`

Estilos globales base.

### `src/constants/enums.ts`

Placeholder invalido.

### `constants/Colors.ts`

Archivo de colores default estilo plantilla Expo. No se encontro uso en la app principal.

## Componentes

### `src/components/common/BottomNav.tsx`

Navegacion inferior custom con iconos MaterialCommunityIcons.

Rutas configuradas:

- Inicio
- Compromisos
- Finanzas
- Gastos Fijos
- Contrasenas
- Perfil

Advertencia:

- Usa paths como `/finances`, pero muchas pantallas navegan con `/(app)/finances`. Expo Router puede resolver grupos, pero conviene estandarizar.

### Placeholders invalidos de componentes

Archivos common:

- `Button.tsx`
- `Input.tsx`
- `Header.tsx`
- `BottomTabBar.tsx`
- `Card.tsx`
- `ErrorMessage.tsx`
- `LoadingSpinner.tsx`

Archivos forms:

- `EventForm.tsx`
- `MovementForm.tsx`
- `FixedExpenseForm.tsx`
- `PasswordForm.tsx`

Archivos lists:

- `MovementItem.tsx`
- `FixedExpenseItem.tsx`
- `EventItem.tsx`
- `PasswordItem.tsx`

Archivos modals:

- `ConfirmModal.tsx`
- `FilterModal.tsx`

Todos contienen una linea invalida `export src/...;`.
