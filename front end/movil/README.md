# Adulto Funcional Móvil

Cliente móvil de Adulto Funcional construido con React Native, Expo SDK 54 y
JavaScript. Su propósito es permitir que cada usuario gestione desde el
teléfono sus finanzas, agenda y bóveda de credenciales consumiendo el backend
Spring Boot del repositorio.

## Estado actual

El proyecto está inicializado y preparado para Expo Go. Las pantallas se
implementarán progresivamente a partir de referencias visuales aprobadas; por
eso el scaffold inicial no anticipa todavía una navegación o diseño final.

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

## Conexión con la API

El backend diferencia el cliente nativo mediante el encabezado:

```http
X-Client-Type: mobile
```

En las operaciones autenticadas se enviará `Authorization: Bearer <token>`.
Cuando se implemente autenticación, los refresh tokens se almacenarán en
`expo-secure-store`; nunca en texto plano, logs ni almacenamiento general.

## Arquitectura prevista

Cada módulo funcional conservará sus reglas y dependencias separadas:

```text
src/
  app/              composicion y navegacion
  core/             red, configuracion y almacenamiento seguro
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

## Dependencias y seguridad

Las dependencias Expo deben instalarse mediante `npx expo install` para usar
versiones compatibles con SDK 54. No se debe ejecutar `npm audit fix --force`,
porque puede saltar a otro SDK y romper Expo Go. Las alertas transitivas se
revisan y se actualizan siguiendo las versiones soportadas oficialmente.

