# Adulto Funcional Movil

Aplicacion movil construida con Expo, React Native, TypeScript y Expo Router para organizar finanzas personales, compromisos, gastos fijos, perfil de usuario y gestor de contrasenas protegido por clave maestra.

Este repositorio contiene una app movil en estabilizacion: las pantallas principales estan implementadas, los placeholders invalidos ya no bloquean TypeScript y la documentacion de `docs/` describe el estado real del codigo, no solo la arquitectura esperada.

## Inicio rapido

Requisitos habituales:

- Node.js compatible con Expo SDK 54.
- npm.
- Expo CLI via `npx expo`.
- Android Studio, Xcode o Expo Go segun el entorno de prueba.

Comandos disponibles:

```bash
npm install
npm run start
npm run android
npm run ios
npm run web
```

Chequeo de tipos:

```bash
npx tsc --noEmit
```

Estado actual del chequeo: finaliza correctamente. Ver [docs/estado-tecnico.md](docs/estado-tecnico.md).

Empaquetado Android verificado:

```bash
npx expo export --platform android --output-dir /tmp/adulto-funcional-mobile-export
```

## Estructura general

```text
app/                  Rutas de Expo Router y pantallas principales
src/api/              Cliente Axios y modulos HTTP por dominio
src/contexts/         Contextos globales de autenticacion y tema
src/hooks/            Hooks de estado y operaciones por funcionalidad
src/services/         Servicios locales de storage y rachas
src/utils/            Utilidades puras de validacion y moneda
src/constants/        Configuracion, colores y estilos globales
src/components/       Componentes reutilizables y placeholders validos sin runtime
assets/               Iconos, splash y fuente
docs/                 Documentacion detallada del proyecto
```

## Documentacion detallada

- [Arquitectura](docs/arquitectura.md): capas, dependencias internas y flujo de ejecucion.
- [Rutas y pantallas](docs/rutas-y-pantallas.md): inventario de pantallas Expo Router y comportamiento de cada ruta.
- [API, estado y almacenamiento](docs/api-estado-almacenamiento.md): backend, endpoints, hooks, storage local y datos derivados.
- [Modelos y reglas de negocio](docs/modelos-y-reglas.md): entidades, validaciones, estados y transformaciones.
- [Inventario de archivos](docs/inventario.md): descripcion archivo por archivo.
- [Estado tecnico](docs/estado-tecnico.md): errores conocidos, placeholders, riesgos y prioridades de reparacion.

## Backend y configuracion

La URL del backend se resuelve en [src/constants/config.ts](src/constants/config.ts). En Expo Go se infiere desde `Constants.expoConfig.hostUri`, de modo que una sesion LAN como `exp://192.168.78.161:8081` usa automaticamente `http://192.168.78.161:8080`.

Para sobrescribirla, define `EXPO_PUBLIC_API_URL` en `.env`:

```bash
EXPO_PUBLIC_API_URL=http://192.168.78.161:8080
```

Si no hay host LAN disponible, Android usa `http://10.0.2.2:8080` y otros entornos usan `http://localhost:8080`.

El cliente HTTP esta en [src/api/client.ts](src/api/client.ts). Agrega automaticamente:

- `Content-Type: application/json`
- `Accept: application/json`
- `X-Client-Type: mobile`
- `Authorization: Bearer <token>` cuando existe token guardado

## Flujo de autenticacion

1. [app/_layout.tsx](app/_layout.tsx) monta `ThemeProvider` y `AuthProvider`.
2. [app/(app)/_layout.tsx](app/(app)/_layout.tsx) protege las rutas autenticadas.
3. [src/contexts/AuthContext.tsx](src/contexts/AuthContext.tsx) revisa token local, guarda usuario y actualiza racha de login.
4. [src/api/authApi.ts](src/api/authApi.ts) llama login/registro/logout y persiste datos en storage seguro.

## Funcionalidades principales

- Autenticacion: login, registro con clave maestra opcional y recuperacion de contrasena.
- Dashboard: balance, proximos gastos, proximos compromisos, contador de contrasenas y grafico de tres meses.
- Finanzas: lista de movimientos, filtro por ingresos/egresos, creacion de movimientos y categorias.
- Gastos fijos: lista, filtros, creacion, edicion, eliminacion y registro de pago como movimiento.
- Compromisos: lista, filtros, creacion, edicion, eliminacion, completado y rachas locales por evento recurrente.
- Contrasenas: verificacion de clave maestra, lista, creacion, borrado y revelado individual desde backend.
- Perfil: datos del usuario, estadisticas derivadas, edicion parcial, cambio de contrasena y eliminacion de cuenta.

## Advertencias importantes

- Los 32 archivos placeholder invalidos fueron convertidos en modulos validos con `export {};`.
- La ruta `/(app)/passwords/[id]` esta activa para Expo Router.
- `app.json` referencia assets de notificaciones que no existen en `assets/`.
- El flujo de clave maestra usa crear, verificar, cambiar y cerrar sesion interna; no hay recuperacion por correo.
- Los enums de agenda y finanzas fueron alineados con el backend actual.
- El cambio local actual en [src/constants/config.ts](src/constants/config.ts) apunta a `http://38.225.48.28:8083`.

## Convenciones detectadas

- Rutas con Expo Router bajo `app/`.
- Capa HTTP en `src/api`.
- Estado de dominio en hooks de `src/hooks`.
- Persistencia sensible con `expo-secure-store` en movil y `localStorage` en web.
- Persistencia no sensible con `AsyncStorage`.
- Colores globales en [src/constants/Colors.ts](src/constants/Colors.ts).
- Navegacion inferior custom con [src/components/common/BottomNav.tsx](src/components/common/BottomNav.tsx).

## Siguiente paso recomendado

Antes de ampliar funcionalidades, conviene completar los placeholders que se necesiten en runtime, corregir assets de notificaciones, aplicar tema en vivo desde `ThemeContext` y extraer formularios/listas repetidos hacia componentes reales.
