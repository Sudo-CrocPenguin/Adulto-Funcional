# Adulto Funcional Móvil

Cliente móvil de Adulto Funcional construido con React Native, Expo SDK 54 y
JavaScript. Su propósito es permitir que cada usuario gestione desde el
teléfono sus finanzas, agenda y bóveda de credenciales consumiendo el backend
Spring Boot del repositorio.

## Estado actual

El flujo visual de autenticación incluye registro, inicio de sesión y
recuperación de contraseña, implementados a partir de referencias visuales
aprobadas. Registro y login consumen la API real, muestran errores locales y
del backend, controlan estados de carga y permiten alternar la visibilidad de
las contraseñas. Una sesión válida abre el inicio autenticado y puede
restaurarse mediante la rotación segura del refresh token.

El inicio compone información real de movimientos, eventos, gastos fijos y
estado de la Master Key. Presenta saldo, compromisos pendientes, próximos
gastos, total de credenciales disponible, racha de compromisos, próximos
elementos y estadísticas de los últimos tres meses. Consulta
[docs/INICIO.md](./docs/INICIO.md) para conocer las fuentes y cálculos.

La campana presenta avisos derivados de esos datos reales y permite
descartarlos durante la sesión de pantalla. El engranaje abre una configuración
reducida al modo claro u oscuro; esta preferencia visual se restaura al volver a
abrir la aplicación.

La sección de Compromisos consulta eventos y categorías `AGENDA`, permite
filtrar por estado y crea eventos con fecha, horarios, recurrencia, prioridad y
recordatorio válidos. Consulta
[docs/COMPROMISOS.md](./docs/COMPROMISOS.md) para conocer el contrato, las
reglas y los estados de la pantalla.

Las secciones de Finanzas y Gastos Fijos consumen los movimientos, categorías
`FINANCES` y pagos recurrentes reales de la cuenta. Finanzas calcula saldo,
permite buscar y registrar ingresos o egresos; Gastos Fijos permite crear,
filtrar y registrar un pago como egreso, avanzando su vencimiento. El ojo del
saldo abre 20 visualizaciones financieras construidas con SVG. Consulta
[docs/FINANZAS_Y_GASTOS_FIJOS.md](./docs/FINANZAS_Y_GASTOS_FIJOS.md) para
conocer contratos, cálculos, referencias y el manejo de pagos parciales.

La navegación usa React Navigation con transiciones nativas. Recuperación de
contraseña está maquetada y valida el correo, pero no simula el envío: el
backend todavía no expone un endpoint para solicitar el restablecimiento.

El Gestor de Contraseñas integra el ciclo real de la Master Key y el CRUD de la
bóveda. Permite configurar, verificar, rotar y bloquear la llave; los secretos
se consultan individualmente, viven solo en memoria y vuelven a ocultarse a los
30 segundos. Consulta [docs/CONTRASENAS.md](./docs/CONTRASENAS.md) para conocer
el contrato, los estados y las decisiones de seguridad.

Perfil consulta y edita los datos reales de la cuenta, conserva la sesión al
actualizarlos y calcula compromisos completados, racha máxima, contraseñas
disponibles y gastos fijos registrados. Consulta
[docs/PERFIL.md](./docs/PERFIL.md) para conocer las fuentes, reglas y
limitaciones del contrato actual.

Las instalaciones de distribución verifican EAS Update antes de montar la
sesión. Una actualización disponible se descarga y reinicia automáticamente;
si la comprobación falla, la aplicación queda bloqueada hasta poder reintentar.
Builds y actualizaciones OTA se publican manualmente mediante EAS; el repositorio
no contiene un workflow activo de publicación. Consulta
[docs/ACTUALIZACIONES.md](./docs/ACTUALIZACIONES.md) para crear un APK y validar
el flujo obligatorio.

La versión 0.3.0 no es offline-first: sin backend solo persisten el refresh
token y el tema; las entidades funcionales no tienen base local ni cola de
sincronización. La [política offline propuesta](./docs/OFFLINE_FIRST.md)
documenta el comportamiento actual y los cambios necesarios para usar la app
mientras el homelab está apagado.

## Instalación Android

El APK interno 0.3.0 (`versionCode` 3) está disponible en este
[enlace directo de Expo](https://expo.dev/artifacts/eas/jJVqGsO4YkZ1N2QkNQiPgIPj8C-cagwzcC35Yby6WVQ.apk).
El binario contiene la URL pública HTTPS, no contiene la dirección de ZeroTier
y establece `usesCleartextTraffic=false`. El usuario solo necesita instalarlo
y abrirlo con una conexión normal a Internet.

El artefacto pesa 79,284,510 bytes, tiene SHA-256
`cfad7e0da80f12f9309e5cfa8ed4bed646b618356e9be530345e6ae7cf94ac1d` y está
firmado con APK Signature Scheme v2. Es temporal: Expo prevé eliminarlo el 25
de agosto de 2026. Sus metadatos permanecen en el
[build 815c9cab](https://expo.dev/accounts/servermiguel1/projects/adulto-funcional/builds/815c9cab-ea2d-402f-8122-c05f1169c92f).

SDK 54 se mantiene de forma intencional mientras Expo Go para dispositivos
físicos use esa versión. Expo SDK 54 corresponde a React Native 0.81 y React
19.1.

La identidad visual usa el logo oficial de persona, escudo y candado en el
icono, adaptive icon, splash y favicon. Consulta
[docs/IDENTIDAD_VISUAL.md](./docs/IDENTIDAD_VISUAL.md) para conocer el activo
maestro, la zona segura y las reglas para futuras versiones.

## Requisitos

- Node.js 20.19 o superior.
- npm 10 o superior.
- Expo Go instalado en Android o iOS.
- El backend ejecutándose y accesible desde el dispositivo.

## Configuración

Instala las dependencias:

```bash
cd "front end/movil"
npm ci
```

Copia las variables locales:

```bash
cp .env.example .env
```

Edita `EXPO_PUBLIC_API_URL`. El backend se despliega por separado en `server1`
y se publica mediante Traefik con HTTPS. La aplicación instalada no necesita
ZeroTier:

```dotenv
EXPO_PUBLIC_API_URL=https://api-adulto-funcional.38-225-48-28.sslip.io
```

Los entornos EAS `preview` y `production` ya contienen esa misma variable. Los
perfiles de `eas.json` la incorporan en los builds y las publicaciones OTA sin
depender de archivos locales.

No se deben guardar secretos en variables `EXPO_PUBLIC_*`, porque Expo las
incluye en el bundle de la aplicación. Esta variable contiene únicamente una
URL de servicio no secreta.

## Ejecución

```bash
npm start
```

Expo mostrará un código QR. Para cargar Metro, el teléfono y la PC deben poder
verse por LAN o mediante el túnel de Expo. La API productiva solo requiere
Internet. Si la red bloquea Metro por LAN:

```bash
npm run start:tunnel
```

Comandos adicionales:

| Comando | Función |
|---|---|
| `npm run start:clear` | Reinicia Metro limpiando su caché |
| `npm run android` | Abre el proyecto en Android |
| `npm run ios` | Abre el proyecto en iOS; el simulador requiere macOS |
| `npm run doctor` | Valida la compatibilidad del proyecto Expo |
| `npm test` | Ejecuta las pruebas unitarias una vez |
| `npm run test:watch` | Reejecuta pruebas durante el desarrollo |

Expo Go sirve para el desarrollo visual, pero no ejecuta el control OTA
obligatorio. Ese flujo requiere un build interno o de producción creado con
EAS.

## Conexión con la API

La arquitectura es cliente-servidor: Expo/EAS distribuye este frontend y
`server1` ejecuta exclusivamente Spring Boot, MariaDB y Redis. El frontend web
todavía no está desplegado.

El backend diferencia el cliente nativo mediante el encabezado:

```http
X-Client-Type: mobile
```

En las operaciones autenticadas se enviará `Authorization: Bearer <token>`.
Los refresh tokens se almacenan en `expo-secure-store`; nunca en texto plano,
logs ni almacenamiento general. En login, `Recuérdame` decide si el refresh
token persiste después de cerrar la aplicación. Registro conserva la sesión
de forma predeterminada. La preferencia no sensible de tema se guarda por
separado en `AsyncStorage`.

## Arquitectura

Cada módulo funcional conserva sus reglas y dependencias separadas:

```text
src/
  composition/      construccion e inyeccion de dependencias
  core/             red, configuracion y almacenamiento seguro
  navigation/       rutas y transiciones entre pantallas
  session/          estado de autenticacion en memoria
  modules/
    auth/
    commitments/
    dashboard/
    finances/
    passwords/
    profile/
    updates/
      domain/       entidades y contratos
      application/  casos de uso
      infrastructure/adaptadores de API y dispositivo
      presentation/ pantallas y componentes
  shared/           elementos reutilizables sin negocio propio
```

La presentación dependerá de los casos de uso; el dominio no conocerá React
Native, Expo ni HTTP. Los adaptadores concretos encapsularán esas tecnologías.

### Flujo de registro y login

```text
RegisterScreen
  -> RegisterAccountUseCase
    -> RegistrationCommand
    -> HttpAuthRepository
      -> ApiClient
        -> POST /api/auth/register
    -> SecureSessionStore

LoginScreen
  -> LoginAccountUseCase
    -> LoginCommand
    -> HttpAuthRepository
      -> ApiClient
        -> POST /api/auth/login
    -> SecureSessionStore
```

La confirmación de contraseña existe únicamente en la interfaz y nunca se
envía a la API. El access token no se persiste en almacenamiento local; el
refresh token se guarda cifrado mediante `expo-secure-store`.

Al iniciar la aplicación, `RestoreSessionUseCase` busca el refresh token,
solicita su rotación y sustituye el valor almacenado. Un rechazo terminal de
la API elimina la sesión local; un fallo temporal de red conserva el token para
evitar perderlo, pero `AppSessionProvider` deja la interfaz en estado anónimo
porque todavía no existe una sesión offline persistida.

## Limitación conocida

`PasswordRecoveryScreen` no realiza una solicitud remota. Al pulsar `Enviar`
explica que el servicio aún no está disponible. Para completar esa función, el
backend deberá definir solicitud, expiración y consumo de tokens de
recuperación, además del canal de envío correspondiente.

La Master Key tampoco admite recuperación mediante correo o código con el
contrato actual. La aplicación permite cambiarla si el usuario conoce la llave
vigente y explica la limitación si la olvidó; no simula una recuperación que no
podría conservar las credenciales cifradas existentes.

## Dependencias y seguridad

Las dependencias Expo deben instalarse mediante `npx expo install` para usar
versiones compatibles con SDK 54. No se debe ejecutar `npm audit fix --force`,
porque puede saltar a otro SDK y romper Expo Go. Las alertas transitivas se
revisan y se actualizan siguiendo las versiones soportadas oficialmente.
