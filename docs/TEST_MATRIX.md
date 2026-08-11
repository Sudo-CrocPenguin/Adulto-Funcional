# Matriz de pruebas y aceptación

Esta matriz separa pruebas automatizadas, integración cliente-servidor y
validación operativa. Una interfaz visible no demuestra por sí sola que una
entrega funcione end-to-end.

## Estado de referencia

Referencia auditada el 11 de agosto de 2026 sobre la versión `v0.2.0`:

| Componente | Comando o comprobación | Resultado |
|---|---|---|
| Móvil | `npm test -- --runInBand` | 30 suites, 77 pruebas, 0 fallos |
| Expo | `npm run doctor` | 18/18 comprobaciones |
| Backend | `./mvnw clean verify` | 137 pruebas, 1 fallo temporal |
| Markdown | enlaces locales y bloques cercados | 46 enlaces; 1 roto antes de esta corrección |
| Homelab | Compose y healthcheck | API, MariaDB y Redis saludables |
| OTA automático | push móvil a `main` | Bloqueado por `EXPO_TOKEN` ausente |

El fallo conocido está en
`ResourceOwnershipHttpIntegrationTest.createEventAsOwner`: crea el evento del
10 de agosto de 2026. A partir del día siguiente, la API lo rechaza
correctamente por ser una fecha pasada. La solución pertenece al código de
prueba y debe utilizar un `Clock` controlado o una fecha calculada; la
documentación no debe ocultar el fallo ni declarar la regresión en verde.

## Automatizadas por componente

### Backend

```bash
cd server
./mvnw clean verify
```

Este comando compila y ejecuta pruebas unitarias y de integración. Requiere un
daemon Docker accesible para Testcontainers. `./mvnw test` también ejecuta las
clases de integración descubiertas por Surefire; no debe presentarse como un
comando exclusivamente unitario.

Validaciones adicionales:

```bash
./mvnw -Psecurity-scan verify
./mvnw dependency:tree
git diff --check
```

El SCA necesita red y una base de vulnerabilidades actualizada. Un fallo de
descarga no equivale a ausencia de vulnerabilidades.

### Móvil

```bash
cd "front end/movil"
npm ci
npm test -- --runInBand
npm run doctor
```

### Web

El cliente web aún no tiene lockfile ni funcionalidad de negocio. Para validar
el scaffold actual:

```bash
cd "front end/web"
npm install
npm run lint
npm run build
```

Cuando se reactive su desarrollo debe registrarse `package-lock.json` y cambiar
la instalación de CI a `npm ci`.

## Aceptación móvil contra API

| Área | Caso | Resultado esperado |
|---|---|---|
| Conectividad | Abrir healthcheck desde el teléfono | `UP` por ZeroTier |
| Registro | Crear cuenta válida | `201`, sesión abierta y refresh seguro |
| Registro | Email duplicado o datos inválidos | Error de campo o negocio visible |
| Login | Credenciales válidas | Inicio autenticado |
| Sesión | Reiniciar con “Recuérdame” | Refresh rotado y sesión restaurada |
| Sesión | Fallo temporal de red | Refresh local conservado |
| Sesión | Refresh rechazado definitivamente | Sesión local eliminada |
| Compromisos | Crear evento futuro | Aparece en lista y dashboard |
| Finanzas | Crear ingreso y egreso | Totales y saldo recalculados |
| Gastos fijos | Registrar pago | Egreso creado y vencimiento avanzado |
| Bóveda | Configurar y verificar Master Key | Bóveda desbloqueada solo para la sesión |
| Bóveda | Consultar secreto | Visible temporalmente, luego oculto |
| Perfil | Editar datos | API y sesión local actualizadas |
| Tema | Cambiar claro/oscuro y reiniciar | Preferencia restaurada |
| OTA | Publicar bundle compatible | App descarga, reinicia y continúa |
| OTA | Sin red o error de EAS | App bloqueada con reintento, por política |

## Consistencia finanzas–gastos fijos

El pago de un gasto fijo móvil realiza dos solicitudes:

1. crea un movimiento `EXPENSE`;
2. actualiza `nextDueDate` del gasto fijo.

Actualmente no existe una transacción HTTP única que cubra ambos pasos. La
prueba de aceptación debe simular un fallo entre solicitudes y confirmar que la
interfaz informa el pago parcial sin reintentar ciegamente ni duplicar el
egreso.

## Seguridad y aislamiento

- Una cuenta no puede leer, editar ni eliminar recursos de otra.
- Un recurso ajeno responde igual que uno inexistente.
- El navegador recibe tokens únicamente por cookies HttpOnly.
- El cliente nativo recibe tokens solo cuando las señales pasivas y
  `X-Client-Type` coinciden.
- El access token móvil vive únicamente en memoria.
- El refresh token móvil usa `expo-secure-store`.
- Los listados de credenciales nunca contienen el secreto descifrado.
- Logout, revocación y replay invalidan el desbloqueo de Master Key aplicable.

## Evidencia de una ejecución

Una validación formal registra:

- commit o tag;
- fecha, sistema operativo y versiones de Java/Node/Docker;
- comandos ejecutados;
- conteos de pruebas y fallos;
- URL del workflow/build sin incluir tokens;
- resultado del healthcheck;
- identificadores de datos descartables eliminados al terminar.
