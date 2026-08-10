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
las contraseñas.

La navegación usa React Navigation con transiciones nativas. Recuperación de
contraseña está maquetada y valida el correo, pero no simula el envío: el
backend todavía no expone un endpoint para solicitar el restablecimiento.

SDK 54 se mantiene de forma intencional mientras Expo Go para dispositivos
físicos use esa versión. Expo SDK 54 corresponde a React Native 0.81 y React
19.1.

## Requisitos

- Node.js 20.19 o superior.
- npm 10 o superior.
- Expo Go instalado en Android o iOS.
- El backend ejecutándose y accesible desde el dispositivo.

## Configuración

Instala las dependencias:

```bash
cd "front end/movil"
npm install
```

Copia las variables locales:

```bash
cp .env.example .env
```

Edita `EXPO_PUBLIC_API_URL`. Para Expo Go en un teléfono debe contener la IP
LAN del equipo, no `localhost`:

```dotenv
EXPO_PUBLIC_API_URL=http://192.168.1.100:8080
```

No se deben guardar secretos en variables `EXPO_PUBLIC_*`, porque Expo las
incluye en el bundle de la aplicación. Esta variable contiene únicamente una
URL pública.

## Ejecución

```bash
npm start
```

Expo mostrará un código QR. El teléfono y el equipo deben estar en la misma
red; abre Expo Go y escanea el código. Si la red bloquea conexiones LAN:

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

## Conexión con la API

El backend diferencia el cliente nativo mediante el encabezado:

```http
X-Client-Type: mobile
```

En las operaciones autenticadas se enviará `Authorization: Bearer <token>`.
Los refresh tokens se almacenan en `expo-secure-store`; nunca en texto plano,
logs ni almacenamiento general. En login, `Recuérdame` decide si el refresh
token persiste después de cerrar la aplicación. Registro conserva la sesión
de forma predeterminada.

## Arquitectura

Cada módulo funcional conservará sus reglas y dependencias separadas:

```text
src/
  composition/      construccion e inyeccion de dependencias
  core/             red, configuracion y almacenamiento seguro
  navigation/       rutas y transiciones entre pantallas
  modules/
    auth/
    finances/
    agenda/
    vault/
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

## Limitación conocida

`PasswordRecoveryScreen` no realiza una solicitud remota. Al pulsar `Enviar`
explica que el servicio aún no está disponible. Para completar esa función, el
backend deberá definir solicitud, expiración y consumo de tokens de
recuperación, además del canal de envío correspondiente.

## Dependencias y seguridad

Las dependencias Expo deben instalarse mediante `npx expo install` para usar
versiones compatibles con SDK 54. No se debe ejecutar `npm audit fix --force`,
porque puede saltar a otro SDK y romper Expo Go. Las alertas transitivas se
revisan y se actualizan siguiendo las versiones soportadas oficialmente.
