# Actualizaciones obligatorias

## Qué es y para qué sirve

La aplicación móvil usa EAS Update para distribuir cambios compatibles de
JavaScript y recursos sin esperar una nueva instalación desde la tienda. Antes
de montar la sesión o la navegación, una compuerta comprueba el canal asignado
al binario. Si existe una actualización, la descarga, reinicia la aplicación y
solo entonces permite continuar.

El proyecto aplica una política estricta: una instalación de distribución que
no pueda confirmar que está actualizada queda bloqueada y ofrece únicamente la
acción `Reintentar`. Comprueba al iniciar, al regresar desde segundo plano y
cada cinco minutos mientras permanece activa. Esto cumple el requisito de
impedir el uso prolongado de una versión desactualizada, pero también significa
que esas comprobaciones necesitan conexión con el servicio de actualizaciones.

EAS Update no incluye una opción nativa denominada "actualización
obligatoria". La obligatoriedad se implementa en este cliente mediante
`MandatoryUpdateGate`, antes de `SessionProvider` y de la navegación.

## Flujo automático

```text
push a main con cambios en front end/movil/**
  -> GitHub Actions valida el acceso a Expo
  -> instala dependencias con npm ci
  -> ejecuta pruebas y expo-doctor
  -> publica con eas update en el canal production
  -> una instalación production vuelve a abrirse o pasa a primer plano
  -> MandatoryUpdateGate consulta EAS
       ├─ sin actualización: monta sesión y navegación
       ├─ con actualización: descarga, muestra progreso y reinicia
       └─ error de red/servicio: bloquea la app y permite reintentar
```

Si el dispositivo mantiene la aplicación abierta, repite la consulta cada
cinco minutos. Si está suspendida, el sistema operativo no ejecuta el cliente y
la consulta ocurre inmediatamente cuando vuelve a primer plano. No se requiere
un servicio de notificaciones ni un endpoint del backend para este proceso.

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
- Versión nativa actual: `0.2.0` (`versionCode`/`buildNumber` 2).

La política `appVersion` evita que una actualización JavaScript se cargue en
un binario con cambios nativos incompatibles. Cuando se agregue o actualice una
dependencia nativa, se cambien permisos o se modifique configuración nativa,
se debe incrementar `expo.version` y generar un binario nuevo. Los cambios
compatibles de JavaScript, estilos e imágenes pueden publicarse por OTA sin
cambiar esa versión.

## Configuración de GitHub

Antes de ejecutar el flujo por primera vez, crea este secreto en
`Settings > Secrets and variables > Actions` del repositorio de GitHub:

| Secreto | Contenido |
|---|---|
| `EXPO_TOKEN` | Token de acceso de la cuenta Expo con permiso sobre el proyecto |

El token se genera en <https://expo.dev/settings/access-tokens>. No debe
guardarse en el repositorio, en un commit ni compartirse por el chat.

El workflow falla antes de publicar cuando falta el token. No necesita que el
backend ni la base de datos estén desplegados: EAS distribuye únicamente el
bundle y los recursos del frontend móvil.

### Estado verificado de 0.2.0

El workflow ejecutado después de publicar `main` falló en `Comprobar secretos
requeridos` porque `EXPO_TOKEN` no estaba configurado. Para cerrar el flujo:

1. crear el token en Expo;
2. guardarlo como secreto `EXPO_TOKEN` del repositorio;
3. reejecutar el workflow fallido o publicar un nuevo cambio móvil en `main`;
4. comprobar que pruebas, Expo Doctor y `eas update` terminan en verde;
5. instalar un binario compatible y validar la descarga en un dispositivo.

No se debe describir la publicación automática como operativa hasta completar
estos pasos.

`EXPO_PUBLIC_API_URL` está configurada como variable de proyecto en los
entornos EAS `preview` y `production`. El workflow publica con
`--environment production`, por lo que builds y OTA incorporan la misma URL
sin depender de la PC de desarrollo ni de una variable adicional de GitHub.
La variable no es un secreto: contiene únicamente
`http://10.119.54.220:8090`.

La API es privada y el dispositivo debe pertenecer a la red ZeroTier del
homelab. No se necesita un dominio en esta etapa. Android 0.2.0 habilita el
tráfico HTTP mediante `expo-build-properties`; los datos viajan por el túnel
cifrado de ZeroTier y el puerto no debe exponerse en el router. Antes de una
distribución pública o una compilación iOS de producción se debe ofrecer HTTPS
con certificado válido.

## Primera instalación de prueba

EAS Update actualiza binarios que ya fueron compilados con `expo-updates`; no
instala por sí mismo la aplicación. Para validar el flujo en Android, primero
genera un APK interno desde `front end/movil`:

```bash
npx eas-cli build --platform android --profile production-apk
```

Instala el APK entregado por EAS en el dispositivo. Para publicar una prueba
manual en su mismo canal:

```bash
npx eas-cli update \
  --channel production \
  --environment production \
  --message "prueba OTA"
```

`--environment production` es obligatorio en este proyecto: garantiza que la
publicación incorpore `EXPO_PUBLIC_API_URL` desde el mismo entorno EAS que los
builds de producción. La guía oficial de Expo explica el uso de entornos en
<https://docs.expo.dev/eas/environment-variables/usage/>.

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

Expo/EAS aloja únicamente el bundle y los recursos del frontend móvil. La API,
MariaDB y Redis permanecen en `server1`; el frontend web todavía no forma parte
del despliegue.

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
