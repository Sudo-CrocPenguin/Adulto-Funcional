# Gestor de Contraseñas

## Qué es y para qué sirve

El Gestor de Contraseñas es la bóveda personal de Adulto Funcional. Permite
crear, consultar, actualizar y eliminar credenciales sin mezclar la Master Key
con la contraseña de inicio de sesión de la cuenta.

La pantalla no es un almacenamiento local de secretos. El teléfono actúa como
cliente del backend: envía la información mediante la sesión autenticada y el
servidor cifra cada contraseña antes de persistirla.

## Estados de la bóveda

La interfaz se construye a partir de `GET /api/security/master-key/status` y
puede estar en uno de estos estados:

```text
Sin Master Key
  -> crear con contraseña de la cuenta
  -> bloqueada
  -> verificar Master Key
  -> desbloqueada durante la sesión autorizada
  -> bloqueo manual o expiración
  -> bloqueada
```

- **Sin configurar:** solicita la contraseña actual de la cuenta, una nueva
  Master Key y su confirmación.
- **Bloqueada:** solicita únicamente la Master Key para abrir la bóveda.
- **Desbloqueada:** muestra los metadatos de las credenciales y habilita el
  alta, consulta individual, edición y eliminación.
- **Expirada:** limpia de memoria los secretos revelados, cierra formularios
  sensibles y vuelve al acceso bloqueado.

La creación y el cambio validan el contrato real del servidor: la Master Key
debe tener entre 15 y 128 caracteres. Al cambiarla se vuelven a cifrar las
credenciales y se bloquean todas las sesiones de la bóveda.

## Contrato HTTP utilizado

### Master Key

| Operación | Endpoint | Cuerpo |
|---|---|---|
| Consultar estado | `GET /api/security/master-key/status` | Sin cuerpo |
| Crear | `POST /api/security/master-key` | `currentPassword`, `newMasterKey` |
| Verificar | `POST /api/security/master-key/verify` | `masterKey` |
| Cambiar | `PATCH /api/security/master-key` | `currentPassword`, `currentMasterKey`, `newMasterKey` |
| Bloquear sesión | `DELETE /api/security/master-key/session` | Sin cuerpo |

### Credenciales

| Operación | Endpoint | Comportamiento |
|---|---|---|
| Listar | `GET /api/security/passwords` | Devuelve metadatos paginados, no el secreto |
| Revelar | `GET /api/security/passwords/{id}` | Devuelve una credencial descifrada |
| Crear | `POST /api/security/passwords` | Recibe `applicationName` y `password` |
| Actualizar | `PATCH /api/security/passwords/{id}` | Envía únicamente los campos modificados |
| Eliminar | `DELETE /api/security/passwords/{id}` | Elimina después de una confirmación local |

Todas las operaciones usan el access token de la sesión y el encabezado de
cliente móvil configurado por `ApiClient`.

## Protección de secretos en el cliente

- La Master Key nunca se guarda en `AsyncStorage`, `SecureStore`, variables de
  entorno, logs ni entidades de dominio.
- El listado ordinario conserva solo identificador, aplicación y fecha de
  cambio.
- Una contraseña se descarga únicamente después de tocar su ojo.
- El secreto revelado vive en memoria de React y se oculta automáticamente
  después de 30 segundos.
- Los secretos se limpian al ocultarlos, editar una entrada, refrescar la
  pantalla, bloquear la bóveda, vencer la sesión o desmontar la pantalla.
- La fortaleza se calcula localmente solo cuando el secreto existe de manera
  transitoria. Antes de revelarlo se muestra como `Cifrada`.
- El formulario vuelve a ocultar el campo sensible al cerrarse o limpiarse.

`expo-secure-store` sigue reservado para el refresh token de autenticación. No
se usa para copiar la Master Key ni para crear una segunda bóveda local.

## Reglas funcionales

- El nombre de aplicación es obligatorio, único según el backend, no admite
  HTML y tiene un máximo de 35 caracteres.
- El secreto es obligatorio al crear y admite hasta 2032 bytes en UTF-8.
- Al editar, dejar el secreto vacío conserva el valor actual.
- La lista se ordena por nombre de aplicación.
- Las credenciales con 60 días o más desde el último cambio generan un aviso
  de mantenimiento en la campana.
- `MASTER_KEY_REQUIRED` durante una operación devuelve inmediatamente la
  interfaz al estado bloqueado.
- La eliminación requiere confirmación y nunca se ejecuta al tocar el icono
  por accidente.

## Recuperación de la Master Key

El backend actual no ofrece endpoints para enviar códigos por correo ni para
restablecer una Master Key olvidada. Además, sustituir la llave sin conocer la
anterior impediría descifrar las credenciales ya guardadas. Por eso la ayuda de
esta versión no simula un correo ni un código exitoso.

Si el usuario todavía conoce la Master Key actual, puede rotarla desde la ayuda
aportando también la contraseña de su cuenta. Si la olvidó, la aplicación
explica la limitación sin afirmar que los datos pueden recuperarse.

## Organización del módulo

```text
src/modules/passwords/
  domain/          acceso, credenciales, borradores y contrato del repositorio
  application/     casos de uso de Master Key y CRUD de credenciales
  infrastructure/ adaptador de la API HTTP
  presentation/   pantalla, tarjetas, formularios y confirmaciones
```

El dominio no depende de React Native ni de HTTP. La composición crea el
repositorio concreto y entrega los casos de uso a la pantalla mediante el
contenedor de dependencias de la aplicación.

## Comprobación

Las pruebas cubren las validaciones de Master Key, los límites UTF-8, los
cambios inmutables de la colección, los casos de uso, la paginación del
repositorio y la traducción exacta de solicitudes HTTP. La validación completa
se ejecuta con:

```bash
npm test -- --runInBand
npx expo-doctor
npx expo export --platform web
npx expo export --platform android
```
