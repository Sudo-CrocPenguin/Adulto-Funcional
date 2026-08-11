# Actualizaciones obligatorias

## Qué es y para qué sirve

La aplicación móvil usa EAS Update para distribuir cambios compatibles de
JavaScript y recursos sin esperar una nueva instalación desde la tienda. Antes
de montar la sesión o la navegación, una compuerta comprueba el canal asignado
al binario. Si existe una actualización, la descarga, reinicia la aplicación y
solo entonces permite continuar.

El proyecto aplica una política estricta: una instalación de distribución que
no pueda confirmar que está actualizada queda bloqueada y ofrece únicamente la
acción `Reintentar`. Esto cumple el requisito de impedir el uso de una versión
desactualizada, pero también significa que el primer arranque y cada regreso
desde segundo plano necesitan conexión con el servicio de actualizaciones.

EAS Update no incluye una opción nativa denominada "actualización
obligatoria". La obligatoriedad se implementa en este cliente mediante
`MandatoryUpdateGate`, antes de `SessionProvider` y de la navegación.

## Flujo automático

```text
push a main con cambios en front end/movil/**
  -> GitHub Actions valida secretos
  -> instala dependencias con npm ci
  -> ejecuta pruebas y expo-doctor
  -> publica con eas update en el canal production
  -> una instalación production vuelve a abrirse o pasa a primer plano
  -> MandatoryUpdateGate consulta EAS
       ├─ sin actualización: monta sesión y navegación
       ├─ con actualización: descarga, muestra progreso y reinicia
       └─ error de red/servicio: bloquea la app y permite reintentar
```

El flujo está definido en
`.github/workflows/mobile-eas-update.yml` y solo se activa mediante un `push` a
`main` que modifique archivos de `front end/movil`. Los demás cambios del
monorepo no publican una actualización móvil.

## Proyecto, canal y compatibilidad

- Cuenta Expo: `servermiguel1`.
- Proyecto EAS: `adulto-funcional`.
- Project ID: `ffd0b764-df14-4978-a424-de50e726a51b`.
- Canal de distribución: `production`.
- Runtime: política `appVersion`.
- Versión inicial compatible: `0.1.0`.

La política `appVersion` evita que una actualización JavaScript se cargue en
un binario con cambios nativos incompatibles. Cuando se agregue o actualice una
dependencia nativa, se cambien permisos o se modifique configuración nativa,
se debe incrementar `expo.version` y generar un binario nuevo. Los cambios
compatibles de JavaScript, estilos e imágenes pueden publicarse por OTA sin
cambiar esa versión.

## Configuración privada de GitHub

Antes de ejecutar el flujo por primera vez, crea estos secretos en
`Settings > Secrets and variables > Actions` del repositorio de GitHub:

| Secreto | Contenido |
|---|---|
| `EXPO_TOKEN` | Token de acceso de la cuenta Expo con permiso sobre el proyecto |
| `EXPO_PUBLIC_API_URL` | URL HTTPS pública del backend de producción |

El token se genera en <https://expo.dev/settings/access-tokens>. No debe
guardarse en el repositorio, en un commit ni compartirse por el chat. La URL de
API sí queda embebida en el bundle y por eso no debe contener credenciales. No
uses `localhost` ni una IP de la red local para una compilación distribuida.

El workflow falla antes de publicar cuando falta uno de estos valores. Así se
evita distribuir accidentalmente un bundle que no puede conectarse a la API.

## Primera instalación de prueba

EAS Update actualiza binarios que ya fueron compilados con `expo-updates`; no
instala por sí mismo la aplicación. Para validar el flujo en Android, primero
genera un APK interno desde `front end/movil`:

```bash
EXPO_PUBLIC_API_URL=https://api.ejemplo.com \
  npx eas-cli build --platform android --profile production-apk
```

Instala el APK entregado por EAS en el dispositivo. Para publicar una prueba
manual en su mismo canal:

```bash
EXPO_PUBLIC_API_URL=https://api.ejemplo.com \
  npx eas-cli update --channel production --message "prueba OTA"
```

Al abrir de nuevo el APK, la pantalla obligatoria debe consultar, descargar y
reiniciar antes de mostrar autenticación o inicio. Para una entrega de tienda
se usan los perfiles normales:

```bash
npx eas-cli build --platform android --profile production
npx eas-cli build --platform ios --profile production
```

La compilación de iOS requiere las credenciales de Apple correspondientes.

## Expo Go, desarrollo y web

Expo Go y el modo de desarrollo usan el bundle de Metro y no permiten probar
el comportamiento real de `checkForUpdateAsync`, `fetchUpdateAsync` y
`reloadAsync`. Por eso la compuerta se desactiva en `__DEV__` y en web. Esta
excepción permite seguir diseñando con Expo Go, pero la aceptación de la
actualización obligatoria siempre debe hacerse con un APK o build de
distribución.

## Recuperación y rollback

Si una actualización publicada presenta un problema, se puede seleccionar una
actualización anterior o crear un rollback desde el panel del proyecto:

<https://expo.dev/accounts/servermiguel1/projects/adulto-funcional>

También puede iniciarse el flujo interactivo desde la carpeta móvil:

```bash
npx eas-cli update:rollback
```

El rollback debe publicarse en el mismo canal y runtime de los dispositivos
afectados. Si el problema proviene de código nativo, la solución requiere un
nuevo binario y un incremento de versión, no una actualización OTA.

## Componentes responsables

```text
src/modules/updates/
  domain/ApplicationUpdateRepository.js
  application/EnsureLatestUpdateUseCase.js
  infrastructure/ExpoApplicationUpdateRepository.js
  presentation/MandatoryUpdateGate.js
  presentation/MandatoryUpdateScreen.js
```

El repositorio abstrae Expo, el caso de uso decide el flujo, la compuerta
controla el ciclo de vida y la pantalla comunica el estado sin ofrecer una ruta
para omitirlo. Ningún archivo del backend participa ni fue modificado para esta
funcionalidad.
