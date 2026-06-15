# Rutas y pantallas

## Mapa de rutas

```text
/
├─ (auth)
│  ├─ login
│  ├─ register
│  ├─ forgot-password
│  └─ reset-password
└─ (app)
   ├─ index
   ├─ finances
   │  ├─ index
   │  ├─ new
   │  └─ [id]
   ├─ fixed-expenses
   │  ├─ index
   │  ├─ new
   │  └─ [id]
   ├─ compromises
   │  ├─ index
   │  ├─ new
   │  └─ [id]
   ├─ passwords
   │  ├─ index
   │  ├─ new
   │  └─ master-key
   │     ├─ create
   │     ├─ verify
   │     ├─ reset-request
   │     ├─ reset-verify
   │     └─ reset-new
   ├─ categories
   │  ├─ index
   │  └─ new
   └─ profile
      ├─ index
      ├─ edit
      ├─ change-password
      ├─ settings
      └─ delete-account
```

## Layout raiz

### `app/_layout.tsx`

Monta la app con:

- `ThemeProvider`
- `AuthProvider`
- `Stack` de Expo Router con `(auth)` y `(app)`

No muestra headers nativos.

## Rutas publicas

### `app/(auth)/_layout.tsx`

Layout simple con `Stack` sin header.

### `app/(auth)/login.tsx`

Pantalla de inicio de sesion.

Responsabilidades:

- Captura email y contrasena.
- Valida email requerido, formato de email, contrasena requerida y longitud minima de 8 caracteres.
- Llama `useAuth().login`.
- Redirige a `/(app)` si el login es exitoso.
- Navega a `forgot-password` y `register`.

Estado local:

- `email`
- `password`
- `showPassword`
- `rememberMe`
- `error`

Notas:

- `rememberMe` solo afecta UI; no tiene persistencia propia.
- Usa iconos de ojo como texto unicode.

### `app/(auth)/register.tsx`

Pantalla de registro.

Responsabilidades:

- Captura nombres, apellidos, telefono, email, contrasena, confirmacion, clave maestra y confirmacion.
- Valida campos requeridos.
- Valida telefono colombiano con regex local.
- Exige clave maestra minima de 8 caracteres.
- Llama `useAuth().register`.
- Redirige a `/(app)` si el registro es exitoso.

Notas:

- La clave maestra se envia en el payload de registro.
- La validacion de telefono aqui acepta mas formatos que `src/utils/validators.ts`.

### `app/(auth)/forgot-password.tsx`

Pantalla de recuperacion de contrasena.

Responsabilidades:

- Captura email.
- Valida requerido y formato.
- Simula envio con `setTimeout`.
- Muestra alerta de exito.

Notas:

- Importa `apiClient`, pero no lo usa porque el endpoint real esta comentado.
- No navega a `reset-password` automaticamente.

### `app/(auth)/reset-password.tsx`

Placeholder documentado.

Muestra:

- Titulo `Restablecer contrasena`.
- Subtitulo `Pantalla en construccion`.

## Layout autenticado

### `app/(app)/_layout.tsx`

Protege todas las rutas del grupo `(app)`.

Comportamiento:

- Si `AuthContext.isLoading` es true, muestra `ActivityIndicator`.
- Si `isAuthenticated` es false, redirige a `/(auth)/login`.
- Si hay sesion, renderiza `Stack` sin header.

## Dashboard

### `app/(app)/index.tsx`

Pantalla principal autenticada.

Muestra:

- Saludo con nombre de usuario.
- Acceso a configuracion.
- Saldo actual.
- Contadores de proximos gastos, compromisos pendientes y contrasenas.
- Tarjeta de racha global.
- Resumen de primer gasto fijo y primer compromiso proximo.
- Grafico de ingresos y egresos de los ultimos tres meses.
- `BottomNav`.

Datos:

- `useDashboard()`
- `useAuth()`
- `formatCurrencyParts()`
- `LineChart` de `react-native-chart-kit`

Notas:

- La leyenda contiene el texto `Osio`, probablemente typo de `Ocio`.
- La variable `maxStreak` se lee desde `useAuth`, pero no se usa.

## Finanzas

### `app/(app)/finances/index.tsx`

Lista de movimientos financieros.

Responsabilidades:

- Carga movimientos con `useMovements`.
- Refresca al enfocar con `useFocusEffect`.
- Calcula ingresos, egresos y balance.
- Filtra por `todos`, `ingresos`, `egresos`.
- Muestra cada movimiento con descripcion, categoria, fecha y monto.
- Navega a `/(app)/finances/new`.

### `app/(app)/finances/new.tsx`

Formulario de nuevo movimiento.

Responsabilidades:

- Captura tipo (`INCOME`/`EXPENSE`), categoria, monto, fecha y descripcion.
- Carga categorias `FINANCES` con `useCategories`.
- Permite crear categoria en modal.
- Valida monto positivo y categoria seleccionada.
- Llama `createMovement`.

Payload final:

- `movementType`
- `amount`
- `movementDate`
- `description`
- `category`

### `app/(app)/finances/[id].tsx`

Placeholder.

Muestra pantalla `En construccion`.

## Gastos fijos

### `app/(app)/fixed-expenses/index.tsx`

Lista de gastos fijos.

Responsabilidades:

- Carga gastos con `useFixedExpenses`.
- Refresca al enfocar.
- Filtra `Todos` o `Proximos a vencer`.
- Calcula dias restantes.
- Permite marcar como pagado.
- Navega a edicion por id.
- Navega a nuevo gasto fijo.

Notas:

- Al pagar llama `markAsPaid`, que crea un movimiento de egreso.

### `app/(app)/fixed-expenses/new.tsx`

Formulario de nuevo gasto fijo.

Responsabilidades:

- Captura nombre, categoria, frecuencia, monto, estado y proxima fecha.
- Carga categorias `FINANCES`.
- Permite crear categoria en modal.
- Valida nombre, monto, categoria y fecha futura.
- Llama `createExpense`.

Frecuencias de UI:

- `DAILY`
- `WEEKLY`
- `BIWEEKLY`
- `MONTHLY`
- `QUARTERLY`
- `SEMIANNUAL`
- `ANNUAL`

Estados de UI:

- `ACTIVE`
- `INACTIVE`

### `app/(app)/fixed-expenses/[id].tsx`

Formulario de edicion de gasto fijo.

Responsabilidades:

- Lee `id` desde parametros.
- Carga lista completa y busca el gasto por id.
- Permite editar nombre, categoria, frecuencia, monto, estado y proxima fecha.
- Valida campos antes de guardar.
- Permite eliminar gasto fijo.

Notas:

- En update envia `category: { id, name: '' }`; el hook convierte esto a `categoryId`.
- Maneja varios estados de carga: `loading`, `formLoading`, `categoriesLoading`.

## Compromisos

### `app/(app)/compromises/index.tsx`

Lista de compromisos.

Responsabilidades:

- Carga eventos con `useEvents`.
- Refresca al enfocar.
- Filtra por `Todas`, `Pendientes`, `Completadas`.
- Muestra prioridad, categoria, frecuencia, fecha, estado y racha por evento recurrente.
- Permite completar evento.
- Permite eliminar evento.
- Navega a edicion por id.
- Navega a nuevo compromiso.

Notas:

- Importa `Event` desde `src/hooks/useEvents`, pero ese hook no exporta `Event`.
- Filtra contra estados capitalizados (`Pendiente`, `Completado`), mientras el tipo del API declara mayusculas.

### `app/(app)/compromises/new.tsx`

Formulario de nuevo compromiso.

Responsabilidades:

- Captura titulo, categoria, frecuencia, prioridad, fecha, hora inicio y hora fin.
- Carga categorias `AGENDA`.
- Permite crear categoria en modal.
- Calcula recordatorio como un dia antes de la fecha.
- Llama `createEvent`.

Frecuencias:

- `0`: unica
- `1`: diaria
- `7`: semanal
- `30`: mensual
- `365`: anual

Prioridades:

- `ALTA`
- `MEDIA`
- `BAJA`

### `app/(app)/compromises/[id].tsx`

Formulario de edicion de compromiso.

Responsabilidades:

- Lee `id`.
- Carga eventos.
- Busca evento por id.
- Llena formulario.
- Permite editar titulo, categoria, frecuencia, prioridad, fecha, inicio y fin.
- Permite marcar como completado.
- Permite eliminar.

Notas:

- `handleComplete` llama `updateEvent(id, { status: 'Completado' })`; el tipo de `Event.status` no acepta ese literal.

## Contrasenas

### `app/(app)/passwords/index.tsx`

Gestor de contrasenas.

Responsabilidades:

- Verifica que el usuario tenga clave maestra.
- Pide clave maestra antes de cargar lista.
- Llama `verifyMasterKey`.
- Carga lista con `fetchPasswords`.
- Muestra contrasenas ocultas.
- Revela contrasena individual con `securityApi.getPassword(id)`.
- Permite borrar contrasena.
- Navega a nuevo registro.
- Intenta navegar a edicion por id.

Notas:

- La ruta de edicion por id no existe como `.tsx`; existe como `.tsx.bak`.
- `useFocusEffect` esta importado, pero no se usa.

### `app/(app)/passwords/new.tsx`

Formulario de nueva contrasena.

Responsabilidades:

- Captura nombre de aplicacion.
- Captura contrasena.
- Captura fecha de ultimo cambio.
- Llama `createPassword`.

### `app/(app)/passwords/[id].tsx`

Edicion de contrasena activa como ruta dinamica.

Responsabilidades:

- Cargar contrasena por id.
- Obtener valor descifrado con `securityApi.getPassword`.
- Editar aplicacion y contrasena.
- Eliminar contrasena.

### `app/(app)/passwords/master-key/create.tsx`

Formulario funcional.

Responsabilidades:

- Crear la clave maestra despues del registro.
- Validar longitud minima y confirmacion.
- Enviar `POST /api/security/master-key`.
- Volver al gestor de contrasenas cuando la clave queda creada.

### `app/(app)/passwords/master-key/verify.tsx`

Placeholder minimo.

Muestra `Redirigiendo...`.

### `app/(app)/passwords/master-key/reset-request.tsx`

Pantalla informativa de cambio de clave maestra.

Responsabilidades:

- Explicar que el cambio requiere la clave maestra actual.
- Advertir que no hay recuperacion por correo de contrasenas cifradas.
- Navegar a `reset-new`.

### `app/(app)/passwords/master-key/reset-verify.tsx`

Pantalla de compatibilidad para rutas existentes.

Responsabilidades:

- Explicar que el backend actual no usa codigo por correo para este flujo.
- Redirigir hacia el cambio con clave actual.

### `app/(app)/passwords/master-key/reset-new.tsx`

Formulario de cambio de clave maestra.

Responsabilidades:

- Solicitar clave maestra actual.
- Solicitar y confirmar nueva clave maestra.
- Enviar `PATCH /api/security/master-key`.
- Volver al gestor cuando el backend recifra correctamente las contrasenas.

## Perfil

### `app/(app)/profile/index.tsx`

Pantalla de perfil.

Muestra:

- Actividad del usuario.
- Compromisos completados.
- Racha maxima.
- Contrasenas guardadas.
- Gastos fijos registrados.
- Informacion personal.
- Acciones de editar perfil, cambiar contrasena, configuracion, cerrar sesion y eliminar cuenta.

Datos:

- `useProfile`
- `useAuth`

Nota:

- Eliminar cuenta muestra `alert('Funcion no implementada')`.

### `app/(app)/profile/edit.tsx`

Formulario de edicion de perfil.

Responsabilidades previstas:

- Editar nombres, apellidos, telefono y correo.
- Validar datos.
- Enviar PATCH a `/api/account/{id}`.
- Actualizar storage local.
- Refrescar usuario en contexto.

Problema actual:

- Los `TextInput` usan `onChangeText={() => setError('')}` y no actualizan `names`, `lastnames`, `phone` ni `email`, por lo que el usuario no puede modificar los campos.

### `app/(app)/profile/change-password.tsx`

Formulario de cambio de contrasena.

Responsabilidades:

- Captura contrasena actual, nueva y confirmacion.
- Valida campos y coincidencia.
- Llama `useProfile().changePassword`.

Nota:

- `changePassword` es un TODO que solo hace `console.log`.

### `app/(app)/profile/settings.tsx`

Pantalla de configuracion.

Responsabilidades:

- Editar perfil.
- Cambiar contrasena.
- Mostrar idioma fijo.
- Persistir modo oscuro en `AsyncStorage`.
- Persistir preferencias de notificaciones.
- Alternar 2FA y respaldo automatico solo en memoria.
- Cerrar sesion.
- Simular eliminar cuenta con `console.log`.

Nota:

- Cambiar `theme` en esta pantalla no llama `ThemeContext.toggleTheme`; solo escribe en storage.

### `app/(app)/profile/delete-account.tsx`

Placeholder.

Muestra pantalla `En construccion`.

## Categorias

### `app/(app)/categories/index.tsx`

Placeholder.

### `app/(app)/categories/new.tsx`

Placeholder.

La creacion real de categorias ocurre embebida en modales dentro de:

- `finances/new.tsx`
- `fixed-expenses/new.tsx`
- `compromises/new.tsx`
