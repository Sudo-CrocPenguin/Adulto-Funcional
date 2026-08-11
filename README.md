# Adulto Funcional

Adulto Funcional es una aplicación cliente-servidor para organizar finanzas,
compromisos, gastos recurrentes y credenciales personales desde un único
espacio. El repositorio contiene tres proyectos independientes que comparten el
mismo dominio, pero se construyen y despliegan por separado.

## Estado de la versión 0.2.0

| Componente | Estado | Entorno |
|---|---|---|
| Backend y base de datos | Desplegados en el homelab | Docker Compose en `server1` |
| Aplicación móvil | Primera versión funcional | Expo/EAS, Android y desarrollo con Expo Go |
| Cliente web | Scaffold técnico | Sin desarrollo funcional ni despliegue |

La API privada se encuentra en `http://10.119.54.220:8090` y solo debe
alcanzarse desde la red ZeroTier autorizada. El frontend móvil consume esa URL;
Expo no aloja el backend ni la base de datos. La aplicación web todavía no
forma parte de la entrega.

## Capacidades actuales

- Registro, inicio de sesión, refresh rotativo y cierre de sesión.
- Perfil y edición de datos personales.
- Compromisos con categoría, prioridad, recurrencia, fecha y recordatorio.
- Ingresos, egresos, saldo y análisis financiero con 20 visualizaciones.
- Gastos fijos, vencimientos y registro de pagos como movimientos de egreso.
- Bóveda cifrada con Master Key y secretos visibles temporalmente.
- Dashboard, tema claro/oscuro y avisos contextuales.
- Actualizaciones móviles obligatorias mediante EAS Update.

Recuperar la contraseña de acceso y recuperar una Master Key olvidada no están
implementados porque el backend no ofrece esos contratos. La interfaz móvil lo
informa expresamente y no simula una operación exitosa.

## Arquitectura de despliegue

```text
Aplicación móvil instalada
        │
        │ HTTP privado sobre ZeroTier
        ▼
server1:10.119.54.220:8090
        │
        ├── Spring Boot 3 / Java 21
        ├── MariaDB 11.8
        └── Redis 7.4

GitHub main ──► GitHub Actions ──► EAS Update ──► aplicación instalada
```

- La PC de desarrollo sirve código y Metro; no es el entorno productivo.
- `server1` aloja exclusivamente API, base de datos y Redis.
- Expo/EAS construye y distribuye el frontend móvil.
- El frontend web se desplegará en una etapa posterior.

La separación completa está explicada en
[Ejecución end-to-end](docs/END_TO_END.md).

## Tecnologías

| Capa | Tecnologías |
|---|---|
| Backend | Java 21, Spring Boot 3, Spring Security, Spring Data JPA |
| Datos | MariaDB 11.8, Flyway y Redis 7.4 |
| Seguridad | JWT, refresh rotativo, Argon2, AES-256-GCM, CSRF y cookies HttpOnly |
| Móvil | Expo SDK 54, React Native 0.81, React 19.1 y JavaScript |
| Navegación móvil | React Navigation 7 |
| Web | React 19, TypeScript y Vite 8 |
| Calidad | JUnit 5, Mockito, Testcontainers, Jest y Expo Doctor |
| Operación | Docker Compose, GitHub Actions y EAS Update |

## Estructura del repositorio

```text
.
├── server/             API, persistencia, contenedores y documentación técnica
├── front end/
│   ├── movil/          aplicación Expo/React Native en JavaScript
│   └── web/            scaffold React/Vite en TypeScript
├── docs/               guías transversales y validación de entregas
├── CONTRIBUTING.md     GitFlow, commits y criterios de colaboración
└── CHANGELOG.md        cambios publicados por versión
```

Los clientes comparten repositorio y ramas Git; no son repositorios Git
anidados.

## Inicio rápido

### Aplicación móvil

```bash
cd "front end/movil"
npm ci
cp .env.example .env
npm start
```

En `.env`, configura una URL alcanzable desde el dispositivo:

```dotenv
EXPO_PUBLIC_API_URL=http://10.119.54.220:8090
```

El teléfono necesita ZeroTier activo para consumir la API. Expo Go sirve para
desarrollo visual, pero las actualizaciones obligatorias se prueban en un APK
creado con EAS. Consulta el
[README móvil](<front end/movil/README.md>) y la
[guía OTA](<front end/movil/docs/ACTUALIZACIONES.md>).

### Backend local

```bash
cd server
cp .env.example .env
# Completa los secretos y credenciales de .env
docker compose up -d --build
curl http://127.0.0.1:8080/actuator/health
```

El Compose base crea su propia red interna. La red externa `coolify` solo se
usa cuando se añade explícitamente el archivo `docker-compose.coolify.yml`.
Consulta el [README del servidor](server/README.md) antes de ejecutar el
backend sin contenedores.

### Cliente web

```bash
cd "front end/web"
npm install
npm run dev
```

El proyecto web es únicamente un scaffold y no consume todavía la API. Consulta
su [README](<front end/web/README.md>) para conocer el alcance real.

## API

Todas las rutas propias usan el prefijo `/api`. Los grupos principales son:

| Módulo | Prefijo |
|---|---|
| Autenticación | `/api/auth` |
| Cuenta | `/api/account` |
| Finanzas | `/api/finances` |
| Agenda | `/api/agenda` |
| Master Key | `/api/security/master-key` |
| Bóveda | `/api/security/passwords` |

La ruta histórica
`POST /api/security/passwords/master-key/verify` se conserva temporalmente por
compatibilidad; los clientes nuevos deben usar
`POST /api/security/master-key/verify`.

Consulta la [referencia completa](server/docs/API_REFERENCE.md) y el
[contrato de errores](server/docs/API_ERROR_CONTRACT.md). No existe todavía una
especificación OpenAPI generada; la referencia Markdown y los DTO/controladores
son las fuentes actuales del contrato.

## Verificación

```bash
# Móvil
cd "front end/movil"
npm test -- --runInBand
npm run doctor

# Backend; requiere Docker para Testcontainers
cd ../../server
./mvnw clean verify
```

La versión 0.2.0 contiene 77 pruebas móviles. El backend contiene 137 pruebas,
pero al 11 de agosto de 2026 una integración falla porque utiliza una fecha de
evento fija que ya quedó en el pasado. El defecto y el procedimiento de
validación se documentan en la
[matriz de pruebas](docs/TEST_MATRIX.md); una entrega no debe declararse verde
hasta corregirlo.

## Documentación

- [Ejecución y validación end-to-end](docs/END_TO_END.md)
- [Matriz de pruebas y aceptación](docs/TEST_MATRIX.md)
- [Checklist de release](docs/RELEASE_CHECKLIST.md)
- [Datos y privacidad técnica](docs/DATA_AND_PRIVACY.md)
- [Contribución y GitFlow](CONTRIBUTING.md)
- [Historial de cambios](CHANGELOG.md)
- [Arquitectura del backend](server/ARCHITECTURE.md)
- [Modelo de datos](server/DATABASE.md)
- [Operación](server/docs/OPERATIONS.md)
- [Despliegue en homelab](server/docs/HOMELAB_DEPLOYMENT.md)
- [Seguridad](server/docs/SECURITY.md)
- [Documentación del frontend](<front end/README.md>)

## Entrega y ramas

`main` representa producción y `develop` integración. El trabajo se realiza en
ramas temporales creadas desde `develop`; no se hace `push` automático desde
herramientas locales. Las reglas completas y los tipos de commit están en
[CONTRIBUTING.md](CONTRIBUTING.md).

## Licencia y distribución

El backend contiene su propia licencia en `server/LICENSE`. Los clientes móvil
y web no tienen todavía una licencia de reutilización independiente; no debe
asumirse que el archivo del backend los cubre. Antes de una distribución
pública deben definirse también la política de privacidad, los términos de uso
y el responsable del tratamiento de datos.
