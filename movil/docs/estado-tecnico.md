# Estado tecnico

## Resultado de verificacion

Comando ejecutado:

```bash
npx tsc --noEmit
```

Resultado actual:

- Finaliza correctamente.
- La compilacion estricta de TypeScript ya no reporta errores.
- Tambien se verifico el empaquetado Android con:

```bash
npx expo export --platform android --output-dir /tmp/adulto-funcional-mobile-export
```

Resultado:

- Finaliza correctamente.
- Metro genera el bundle Android en `/tmp/adulto-funcional-mobile-export`.

## Archivos placeholder invalidos

Estos archivos contenian una unica linea invalida de TypeScript y bloqueaban compilacion:

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

Reparacion aplicada:

- Cada archivo quedo como modulo valido con `export {};`.
- Siguen siendo placeholders; si se necesitan en runtime, se deben implementar con el contrato real correspondiente.

## Problemas de rutas

### Edicion de contrasenas activa

Antes existia:

- `app/(app)/passwords/[id].tsx.bak`

Estado actual:

- El archivo fue restaurado como `app/(app)/passwords/[id].tsx`.
- La pantalla `passwords/index.tsx` navega a la ruta dinamica de edicion.

### Rutas de categorias son placeholders

Estas rutas no implementan CRUD real:

- `app/(app)/categories/index.tsx`
- `app/(app)/categories/new.tsx`

La creacion de categorias ocurre en modales embebidos.

### Rutas de clave maestra alineadas

Estado actual:

- `create.tsx` permite crear la clave maestra despues del registro.
- `reset-request.tsx` explica que no hay recuperacion por correo porque las contrasenas estan cifradas.
- `reset-new.tsx` cambia la clave maestra usando la clave actual y recifrado backend.
- `usePasswords` expone `createMasterKey`, `changeMasterKey`, `refreshMasterKeyStatus` y `resetVerification`.

## Problemas de contratos y tipos

### Estados de compromisos

Tipo API:

- `Pendiente`
- `Completado`
- `Cancelado`
- `Pospuesto`

Estado actual:

- Los tipos moviles usan los valores en espanol que devuelve el backend de agenda.
- Los filtros y calculos usan `Pendiente` / `Completado`.

### Prioridades de compromisos

Tipo API:

- `Alta`
- `Media`
- `Baja`

Estado actual:

- Los tipos moviles usan los valores en espanol que acepta y devuelve el backend.
- `useEvents` normaliza entradas antiguas en mayusculas antes de enviar payloads.

### Frecuencias y estados de gastos fijos

Tipo API:

- Frecuencia: `WEEKLY`, `BIWEEKLY`, `MONTHLY`, `QUARTERLY`, `SEMIANNUAL`, `ANNUAL`
- Estado: `ACTIVE`, `INACTIVE`

Estado actual:

- Los tipos moviles usan los enums reales del backend financiero.
- La opcion `DAILY` se retiro porque el backend no la expone.
- `markAsPaid` recalcula la siguiente fecha con esos enums.

### Endpoint de verificar clave maestra

En `config.ts`:

- `/api/security/master-key/verify`

En `securityApi.ts`:

- `/api/security/master-key/verify`

Estado actual:

- La app movil usa el endpoint canonico de Master Key.
- El cambio de clave usa `PATCH /api/security/master-key`.
- El cierre de sesion de clave usa `DELETE /api/security/master-key/session`.

## Bugs funcionales detectados

### Edicion de perfil conectada

Archivo:

- `app/(app)/profile/edit.tsx`

Estado actual:

- Los `TextInput` actualizan `names`, `lastnames`, `phone` y `email`.
- La pantalla valida datos y llama `PATCH /api/account/{id}` mediante `useProfile`.
- El storage local se sincroniza con la respuesta del backend.

### Cambio de contrasena implementado

Archivo:

- `src/hooks/useProfile.ts`

Estado actual:

- `changePassword` llama `PATCH /api/account/{id}/password`.
- `profile/change-password.tsx` valida longitud y confirmacion de la nueva contrasena.

Requisito:

- El backend desplegado debe incluir el endpoint de cambio de contrasena.

### Eliminar cuenta implementado

Archivos:

- `app/(app)/profile/index.tsx`
- `app/(app)/profile/settings.tsx`
- `app/(app)/profile/delete-account.tsx`

Estado actual:

- `profile/delete-account.tsx` exige escribir `ELIMINAR`.
- Llama `DELETE /api/account/{id}` y luego limpia la sesion local.
- Perfil y configuracion navegan a la pantalla real de eliminacion.

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
