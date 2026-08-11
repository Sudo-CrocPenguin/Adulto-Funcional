# Frontend de Adulto Funcional

Este directorio contiene los dos clientes del sistema. Comparten contratos de
negocio y el mismo repositorio Git, pero son proyectos independientes: no
comparten dependencias, build ni despliegue.

## Proyectos

| Proyecto | Tecnología | Estado | Despliegue |
|---|---|---|---|
| [`movil`](./movil) | Expo SDK 54, React Native y JavaScript | Primera versión funcional | Expo/EAS |
| [`web`](./web) | React 19, Vite 8 y TypeScript | Scaffold sin negocio | No desplegado |

El cliente móvil implementa autenticación, inicio, compromisos, finanzas,
gastos fijos, análisis, bóveda, perfil, tema y actualizaciones obligatorias. El
cliente web conserva únicamente el scaffold hasta definir su experiencia y
alcance.

## Arquitectura cliente-servidor

```text
Móvil instalado ──HTTP privado/ZeroTier──► server1:8090
Web futuro      ──HTTPS/cookies/CSRF─────► API pública futura

Expo/EAS distribuye móvil, pero no aloja API ni base de datos.
server1 aloja Spring Boot, MariaDB y Redis, pero no sirve Metro ni el web.
```

La URL móvil vigente es:

```dotenv
EXPO_PUBLIC_API_URL=http://10.119.54.220:8090
```

El teléfono debe pertenecer a la red ZeroTier autorizada. Para desarrollo con
un backend local se puede usar `10.0.2.2` desde un emulador Android,
`localhost` desde un simulador iOS o la IP LAN desde un teléfono, siempre que
el servidor escuche en una interfaz alcanzable y su CORS lo permita.

## Diferencia de transporte de autenticación

| Cliente | Access token | Refresh token | CSRF |
|---|---|---|---|
| Móvil nativo | Body inicial y `Authorization: Bearer` | Body y almacenamiento seguro | No para Bearer |
| Navegador | Cookie HttpOnly | Cookie HttpOnly limitada a refresh | Obligatorio en mutaciones por cookie |

Expo web sirve para revisión visual, pero no debe usarse para validar como si
fuera una aplicación nativa: el navegador y el móvil usan contratos de
transporte diferentes.

## Instalación

Móvil tiene lockfile y se instala de forma reproducible:

```bash
cd "front end/movil"
npm ci
```

Web todavía no tiene lockfile registrado:

```bash
cd "front end/web"
npm install
```

Cuando comience el desarrollo web se debe registrar `package-lock.json` y usar
`npm ci` en automatización.

## Flujo Git

Los dos proyectos pertenecen al repositorio raíz y comparten sus ramas. El
trabajo nace de `develop` en `feature/*`, `bugfix/*`, `docs/*`, `refactor/*` o
`chore/*`. Una `release/*` estabiliza el paso a `main`; los hotfix nacen de
`main` y regresan también a `develop`.

La rama histórica `feature/frontend-foundation` ya fue integrada. La versión
móvil actual fue publicada como `v0.2.0`; no debe tratarse esa rama como punto
de partida para trabajo nuevo. Consulta [CONTRIBUTING.md](../CONTRIBUTING.md).

## Documentación

- [Cliente móvil](./movil/README.md)
- [Cliente web](./web/README.md)
- [Ejecución end-to-end](../docs/END_TO_END.md)
- [Matriz de pruebas](../docs/TEST_MATRIX.md)
- [Contrato API](../server/docs/API_REFERENCE.md)
- [Actualizaciones móviles](./movil/docs/ACTUALIZACIONES.md)
