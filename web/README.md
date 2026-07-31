# adulto-funcional-web

Cliente web operativo para Adulto Funcional.

## Que es

Aplicacion web estatica construida con HTML, CSS y modulos ES. Consume la API REST del backend para autenticacion, finanzas, agenda y gestor de contrasenas.

## Para que sirve

Permite validar desde navegador los flujos principales del sistema:

- Login y registro usando la cookie `HttpOnly` emitida por el backend.
- Configuracion local de la URL base de la API.
- Consulta de categorias, movimientos, eventos y gastos fijos.
- Verificacion de Master Key y consulta de credenciales cifradas.

## Como funciona

La aplicacion esta separada en modulos:

- `src/shared/api/ApiClient.js`: cliente HTTP. Normaliza la URL base, envia `credentials: include` para cookies y marca las peticiones como cliente web con `X-Client-Type: web`.
- `src/auth/infrastructure/SessionStore.js`: persistencia local no sensible. Guarda la URL de API y una copia de datos de cuenta para reconstruir la UI; no guarda JWT.
- `src/app/AdultoFuncionalApp.js`: orquesta estado, formularios y renderizado de la interfaz.
- `src/main.js`: punto de entrada del navegador.

El JWT web debe viajar solo en la cookie `HttpOnly` configurada por el backend. Si se requiere probar un cliente nativo, usar el proyecto `movil`, que recibe el token en el body y lo guarda en almacenamiento seguro.

## Comandos

```bash
npm run dev
npm run lint
npm run typecheck
npm test
```

`npm run dev` sirve el proyecto en `http://localhost:5173`.

## Requisitos

- Backend disponible, por defecto en `http://localhost:8080`.
- CORS del backend permitiendo el origen donde se sirve este cliente web.
