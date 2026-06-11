# Estado tecnico

## Resultado de verificacion

Comando ejecutado:

```bash
npx tsc --noEmit
```

Resultado actual:

- Falla con codigo 2.
- El compilador se detiene en errores sintacticos `TS1128`.
- La causa inmediata son archivos placeholder con contenido invalido como `export src/...;`.

## Archivos placeholder invalidos

Estos archivos contienen una unica linea invalida de TypeScript y bloquean compilacion:

- `src/api/endpoints.ts`
- `src/api/passwordsApi.ts`
- `src/components/common/BottomTabBar.tsx`
- `src/components/common/Button.tsx`
- `src/components/common/Card.tsx`
- `src/components/common/ErrorMessage.tsx`
- `src/components/common/Header.tsx`
- `src/components/common/Input.tsx`
- `src/components/common/LoadingSpinner.tsx`
- `src/components/forms/EventForm.tsx`
- `src/components/forms/FixedExpenseForm.tsx`
- `src/components/forms/MovementForm.tsx`
- `src/components/forms/PasswordForm.tsx`
- `src/components/lists/EventItem.tsx`
- `src/components/lists/FixedExpenseItem.tsx`
- `src/components/lists/MovementItem.tsx`
- `src/components/lists/PasswordItem.tsx`
- `src/components/modals/ConfirmModal.tsx`
- `src/components/modals/FilterModal.tsx`
- `src/constants/enums.ts`
- `src/contexts/MasterKeyContext.tsx`
- `src/hooks/useApi.ts`
- `src/hooks/useAuth.ts`
- `src/hooks/useMasterKey.ts`
- `src/services/currencyUtils.ts`
- `src/services/dateUtils.ts`
- `src/services/errorHandler.ts`
- `src/services/validators.ts`
- `src/types/account.types.ts`
- `src/types/agenda.types.ts`
- `src/types/finances.types.ts`
- `src/types/security.types.ts`

Reparacion minima:

- Convertir cada archivo en un modulo valido, por ejemplo `export {};`, si no se usa.
- O implementar/exportar el contenido real esperado.

## Problemas de rutas

### Edicion de contrasenas no activa

Existe:

- `app/(app)/passwords/[id].tsx.bak`

Pero Expo Router no lo toma como ruta activa.

La pantalla `passwords/index.tsx` navega a:

- `/(app)/passwords/${item.id}`

Resultado esperado actual:

- Esa ruta no deberia resolver hasta que el archivo sea renombrado a `[id].tsx`.

### Rutas de categorias son placeholders

Estas rutas no implementan CRUD real:

- `app/(app)/categories/index.tsx`
- `app/(app)/categories/new.tsx`

La creacion de categorias ocurre en modales embebidos.

### Rutas de reset de clave maestra incompletas

`reset-request.tsx` y `reset-new.tsx` llaman funciones que no existen en `usePasswords`:

- `resetMasterKeyRequest`
- `resetMasterKeyVerify`

## Problemas de contratos y tipos

### Estados de compromisos

Tipo API:

- `PENDIENTE`
- `COMPLETADO`
- `CANCELADO`

UI/hook:

- `Pendiente`
- `Completado`
- `Cancelado`

Riesgo:

- Filtros pueden fallar si backend devuelve mayusculas.
- TypeScript marcaria errores cuando se supere el bloqueo sintactico de placeholders.

### Prioridades de compromisos

Tipo API:

- `ALTA`
- `MEDIA`
- `BAJA`

UI/hook:

- `Alta`
- `Media`
- `Baja`

Riesgo:

- Estilos y payloads pueden quedar desalineados con backend.

### Frecuencias y estados de gastos fijos

Tipo API:

- Frecuencia: `DIARIO`, `SEMANAL`, `MENSUAL`, `ANUAL`
- Estado: `ACTIVO`, `INACTIVO`, `PAGADO`

UI/hook:

- Frecuencia: `DAILY`, `WEEKLY`, `BIWEEKLY`, `MONTHLY`, `QUARTERLY`, `SEMIANNUAL`, `ANNUAL`
- Estado: `ACTIVE`, `INACTIVE`

Riesgo:

- `markAsPaid` no recalcula fecha si recibe valores en espanol.
- Listado muestra `Activo` solo si estado es exactamente `ACTIVE`.

### Endpoint de verificar clave maestra

En `config.ts`:

- `/api/security/master-key/verify`

En `securityApi.ts`:

- `/api/security/passwords/master-key/verify`

Riesgo:

- Si backend implementa solo uno de los dos, una parte de la app fallara.

## Bugs funcionales detectados

### Edicion de perfil no cambia campos

Archivo:

- `app/(app)/profile/edit.tsx`

Problema:

- Los `TextInput` usan `onChangeText={() => setError('')}`.
- No llaman `setNames`, `setLastnames`, `setPhone` ni `setEmail`.

Impacto:

- El usuario no puede editar realmente el perfil desde la UI.

### Cambio de contrasena no implementado

Archivo:

- `src/hooks/useProfile.ts`

Problema:

- `changePassword` solo hace `console.log`.

Impacto:

- `profile/change-password.tsx` parece funcional, pero no cambia nada en backend.

### Eliminar cuenta no implementado

Archivos:

- `app/(app)/profile/index.tsx`
- `app/(app)/profile/settings.tsx`
- `app/(app)/profile/delete-account.tsx`

Problema:

- Hay alertas o placeholders, pero no llamada real al backend.

### Settings no sincroniza tema en vivo

Archivo:

- `app/(app)/profile/settings.tsx`

Problema:

- Escribe `theme` en `AsyncStorage`, pero no usa `ThemeContext.toggleTheme`.

Impacto:

- La preferencia puede no aplicarse hasta reiniciar o remount.

### Assets de notificaciones ausentes

Archivo:

- `app.json`

Referencias ausentes:

- `assets/images/notification-icon.png`
- `assets/sounds/notification.wav`

Impacto:

- Configuracion de notificaciones puede fallar en builds o prebuild.

### Dashboard contiene texto con typo

Archivo:

- `app/(app)/index.tsx`

Texto:

- `Osio`

Probable correccion:

- `Ocio`

### Imports no usados o inconsistentes

Ejemplos:

- `apiClient` importado en `forgot-password.tsx` pero no usado.
- `useFocusEffect` importado en `passwords/index.tsx` pero no usado.
- `useAuth` importado en `profile/edit.tsx`, pero `user` no se usa.
- `getCurrentStreak` y `getMaxStreak` importados en `AuthContext`, pero no usados.

## Riesgos de seguridad

### URL de API hardcodeada

Archivo:

- `src/constants/config.ts`

Riesgo:

- Cambiar entorno requiere editar codigo.
- Puede exponer infraestructura interna.

Recomendacion:

- Usar variables de entorno Expo (`EXPO_PUBLIC_*`) o configuracion por perfil.

### Logs con headers

Archivo:

- `src/api/client.ts`

Problema:

- Loguea headers enviados, incluyendo potencialmente `Authorization`.

Impacto:

- Riesgo de exponer token en logs durante desarrollo o debugging.

### Almacenamiento web

Archivo:

- `src/services/storage.ts`

Comportamiento:

- En web usa `localStorage`.

Impacto:

- `localStorage` no es equivalente a storage seguro. Es aceptable para web demo, pero no para secretos de alta sensibilidad.

### Contrasenas descifradas en estado React

Archivo:

- `app/(app)/passwords/index.tsx`

Comportamiento:

- Guarda contrasenas reveladas en `visiblePasswords`.

Impacto:

- Es normal para UI, pero conviene limpiar al salir de pantalla o al bloquear/verificar nuevamente.

## Riesgos de arquitectura

### Componentes reutilizables no implementados

La UI se repite en pantallas:

- Formularios.
- Cards.
- Inputs.
- Modales de categoria.
- Botones.

Impacto:

- Cambios visuales requieren editar muchas pantallas.
- Mayor riesgo de inconsistencias.

### Hooks anidados multiplican requests

Ejemplos:

- `useDashboard` llama `useMovements`, `useFixedExpenses`, `useEvents`.
- `useProfile` tambien llama `useMovements`, `useFixedExpenses`, `useEvents`.
- `useFixedExpenses` llama `useMovements`.

Impacto:

- Varias pantallas pueden disparar llamadas redundantes al backend.

### Tipos duplicados en API en vez de `src/types`

Hay archivos en `src/types`, pero la mayoria son placeholders. Los tipos reales viven dentro de `src/api`.

Impacto:

- Se dificulta compartir contratos y validar consistencia.

## Prioridades recomendadas

1. Reemplazar placeholders invalidos por modulos validos o implementaciones reales.
2. Ejecutar `npx tsc --noEmit` nuevamente y corregir errores semanticos resultantes.
3. Alinear enums de backend/frontend para estados, prioridades y frecuencias.
4. Corregir edicion de perfil.
5. Activar o eliminar ruta de edicion de contrasenas.
6. Implementar o retirar flujo de reset de clave maestra.
7. Mover API URL a configuracion de entorno.
8. Retirar logs sensibles de headers.
9. Crear componentes comunes reales para inputs, botones, cards, formularios y modales.
10. Revisar configuracion de assets de notificaciones.

## Estado de Git observado

Antes de documentar, el repo ya tenia un cambio local:

- `src/constants/config.ts`

Cambio:

- URL base del API modificada desde un dominio ngrok a una IP HTTP.

La documentacion no revierte ni modifica ese cambio.
