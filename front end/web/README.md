# Adulto Funcional Web

Scaffold del futuro cliente web de Adulto Funcional, creado con React,
TypeScript y Vite. Todavía no implementa pantallas de negocio, autenticación ni
consumo de la API y no forma parte del despliegue de la versión 0.2.0.

## Estado y alcance

El proyecto conserva una base técnica para iniciar el desarrollo cuando se
aprueben los diseños web. Su presencia en el repositorio no significa que la
aplicación móvil se adapte automáticamente al navegador ni que exista una
versión web productiva.

Antes de declararlo funcional se deben implementar como mínimo:

- configuración de entornos y cliente HTTP;
- autenticación por cookies HttpOnly y protección CSRF;
- rutas públicas y autenticadas;
- módulos de inicio, compromisos, finanzas, gastos fijos, bóveda y perfil;
- estados de carga, error, colección vacía y accesibilidad;
- pruebas unitarias, integración y aceptación;
- build reproducible, CI y estrategia de despliegue HTTPS.

## Tecnologías actuales

| Herramienta | Uso |
|---|---|
| React 19 | interfaz |
| TypeScript 6 | tipado estático |
| Vite 8 | desarrollo y build |
| Oxlint | análisis estático |

Consulta `package.json` para las versiones exactas.

## Requisitos

- Node.js compatible con Vite 8.
- npm.

El proyecto todavía no registra `package-lock.json`. Durante el estado de
scaffold se instala con `npm install`; al comenzar el desarrollo funcional se
debe registrar el lockfile y usar `npm ci` en CI.

## Ejecución

```bash
cd "front end/web"
npm install
npm run dev
```

Vite mostrará la URL local, normalmente `http://localhost:5173`.

Otros comandos:

| Comando | Función |
|---|---|
| `npm run lint` | Ejecuta Oxlint |
| `npm run build` | Comprueba TypeScript y genera `dist` |
| `npm run preview` | Sirve localmente el build existente |

## Conexión futura con la API

El navegador no debe imitar los headers del cliente móvil para obtener tokens
en el body. El contrato previsto usa:

- cookies `token` y `refresh_token` con `HttpOnly`;
- credenciales incluidas en las solicitudes HTTP;
- token CSRF para mutaciones autenticadas por cookie;
- origen permitido exactamente en CORS;
- HTTPS y cookies `Secure` en producción.

La referencia vigente está en
[API_REFERENCE.md](../../server/docs/API_REFERENCE.md) y
[SECURITY.md](../../server/docs/SECURITY.md). La URL de producción web, dominio
y hosting aún no están definidos.

## Arquitectura propuesta

Al comenzar el desarrollo, cada módulo debe separar:

```text
src/
  composition/       dependencias y configuración
  core/              HTTP, sesión y errores
  modules/<modulo>/
    domain/           modelos y reglas independientes de React
    application/      casos de uso
    infrastructure/   adaptadores HTTP
    presentation/     rutas, páginas y componentes
  shared/             componentes sin negocio propio
```

Esta estructura es una dirección documentada, no una afirmación sobre el
scaffold actual.

## Criterio para cambiar su estado

El estado puede pasar de `scaffold` a `desarrollo activo` cuando existan un
diseño aprobado, configuración de API, lockfile y primera prueba. Solo puede
marcarse `desplegado` cuando haya URL, HTTPS, variables de entorno, CI y una
validación autenticada contra un backend autorizado.
