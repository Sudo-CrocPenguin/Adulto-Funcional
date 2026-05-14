# Adulto Funcional — Frontend

Aplicación web de organización personal que centraliza la gestión de tareas, finanzas, gastos fijos y contraseñas en un solo lugar. Este repositorio contiene el frontend desarrollado con React, TypeScript y Vite.

---

## Tabla de contenidos

- [Tecnologías](#tecnologías)
- [Requisitos previos](#requisitos-previos)
- [Instalación y configuración](#instalación-y-configuración)
- [Scripts disponibles](#scripts-disponibles)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Páginas y rutas](#páginas-y-rutas)
- [Autenticación](#autenticación)
- [Conexión con el backend](#conexión-con-el-backend)
- [Estilos](#estilos)
- [Calidad de código](#calidad-de-código)
- [Build de producción](#build-de-producción)
- [Flujo de trabajo](#flujo-de-trabajo)
- [Equipo](#equipo)

---

## Tecnologías

| Tecnología | Versión | Descripción |
|---|---|---|
| [React](https://react.dev/) | 19 | Librería principal de UI |
| [TypeScript](https://www.typescriptlang.org/) | ~6.0 | Tipado estático |
| [Vite](https://vite.dev/) | 8 | Bundler y servidor de desarrollo |
| [React Router DOM](https://reactrouter.com/) | 7 | Enrutamiento del lado del cliente |
| [Axios](https://axios-http.com/) | 1.x | Cliente HTTP para consumo de la API |
| [Recharts](https://recharts.org/) | 3.x | Gráficas y visualización de datos |
| [Lucide React](https://lucide.dev/) | 1.x | Iconografía |
| [ESLint](https://eslint.org/) | 9.x | Linting y calidad de código |

---

## Requisitos previos

- [Node.js](https://nodejs.org/) v18 o superior
- [npm](https://www.npmjs.com/) v9 o superior (incluido con Node.js)
- El servidor backend de Adulto Funcional corriendo localmente o en un entorno accesible

---

## Instalación y configuración

**1. Clonar el repositorio**

```bash
git clone https://github.com/adulto-funcional/adulto-funcional-web.git
cd adulto-funcional-web
```

**2. Instalar dependencias**

```bash
npm install
```

**3. Configurar variables de entorno**

Crea un archivo `.env` en la raíz del proyecto basándote en `.env.example`:

```env
VITE_API_URL=http://localhost:8080/api
```

> En producción, reemplaza el valor por la URL del backend desplegado.

**4. Iniciar el servidor de desarrollo**

```bash
npm run dev
```

La aplicación estará disponible en `http://localhost:5173`.

---

## Scripts disponibles

| Comando | Descripción |
|---|---|
| `npm run dev` | Inicia el servidor de desarrollo con HMR |
| `npm run build` | Compila TypeScript y genera el build de producción en `/dist` |
| `npm run preview` | Sirve el build de producción localmente para pruebas |
| `npm run lint` | Analiza el código con ESLint |

---

## Estructura del proyecto

```
src/
├── assets/
│   ├── logo.png
│   └── styles/
│       ├── global.css              # Estilos globales
│       └── variables.css           # Variables CSS (colores, tipografía, espaciado)
├── components/
│   ├── Layout/                     # Shell principal: sidebar, topbar y panel de contenido
│   └── ProtectedRoute/             # Guard que redirige al login si no hay sesión activa
├── context/
│   └── AuthContext.tsx             # Contexto global de autenticación
├── pages/
│   ├── LandingPage/                # Página de inicio pública (incluye login)
│   ├── ForgotPassword/             # Recuperación de contraseña
│   ├── Dashboard/                  # Panel principal con resumen del usuario
│   ├── Commitments/                # Gestión de compromisos y tareas
│   ├── Finances/                   # Seguimiento de ingresos y gastos
│   ├── FixedExpenses/              # Administración de gastos fijos recurrentes
│   ├── Profile/                    # Perfil y datos del usuario
│   ├── PasswordManagerAccess/      # Verificación de acceso al gestor de contraseñas
│   ├── PasswordManagerReset/       # Restablecimiento del PIN del gestor
│   └── PasswordManager/            # Listado de credenciales guardadas
├── services/
│   ├── api.config.ts               # Configuración base de Axios (baseURL, interceptors)
│   ├── auth.service.ts             # Login y gestión de sesión
│   ├── account.service.ts          # Operaciones sobre la cuenta del usuario
│   ├── financeService.ts           # CRUD de movimientos financieros
│   ├── fixedExpenses.service.ts    # CRUD de gastos fijos
│   └── password.service.ts         # CRUD del gestor de contraseñas
├── App.tsx                         # Definición de rutas y providers
└── main.tsx                        # Punto de entrada de la aplicación
```

Cada página cuenta con su propio archivo `.module.css` para estilos con alcance local (CSS Modules).

---

## Páginas y rutas

### Rutas públicas

| Ruta | Página | Descripción |
|---|---|---|
| `/` | LandingPage | Página de inicio e inicio de sesión |
| `/login` | LandingPage | Alias interno utilizado por `ProtectedRoute` |
| `/forgot-password` | ForgotPassword | Recuperación de contraseña |

### Rutas protegidas

Requieren sesión activa. Si no existe, el usuario es redirigido a `/login`. Todas se renderizan dentro del `Layout` compartido (sidebar + topbar).

| Ruta | Página | Descripción |
|---|---|---|
| `/dashboard` | Dashboard | Resumen general del usuario |
| `/commitments` | Commitments | Gestión de compromisos y tareas |
| `/finances` | Finances | Ingresos, gastos y gráficas |
| `/fixed-expenses` | FixedExpenses | Control de pagos recurrentes |
| `/profile` | Profile | Datos y configuración de la cuenta |
| `/password-manager` | PasswordManagerAccess | Verificación de acceso al gestor |
| `/password-manager/reset` | PasswordManagerReset | Restablecimiento del PIN |
| `/password-manager/home` | PasswordManager | Listado de credenciales guardadas |

---

## Autenticación

La aplicación utiliza JWT (JSON Web Token) para la gestión de sesiones.

- **Login:** el backend devuelve un token JWT al autenticarse correctamente.
- **Almacenamiento:** el token se guarda en `sessionStorage`.
- **Interceptor:** cada petición saliente incluye automáticamente el encabezado `Authorization: Bearer <token>`.
- **Protección de rutas:** `ProtectedRoute` redirige a `/login` si no hay sesión activa.
- **Cierre de sesión:** elimina el token de `sessionStorage` y redirige al login.

**Flujo general**

```
Usuario → Login → Backend → JWT → sessionStorage
                                       |
Todas las peticiones ← Interceptor Axios ← Authorization: Bearer <token>
```

---

## Conexión con el backend

La configuración de Axios en `api.config.ts` centraliza toda la comunicación HTTP:

| Característica | Descripción |
|---|---|
| Base URL | Configurable por entorno mediante `VITE_API_URL` |
| Interceptor de solicitud | Agrega el token JWT automáticamente |
| Interceptor de respuesta | Manejo global de errores (401 redirige al login) |
| Credentials | `withCredentials: true` habilitado |

**Endpoints del backend**

| Módulo | Base URL |
|---|---|
| Autenticación | `/api/auth` |
| Cuenta | `/api/account` |
| Finanzas | `/api/finances` |
| Gastos fijos | `/api/finances/fixed-expenses` |
| Agenda | `/api/agenda` |
| Contraseñas | `/api/security/passwords` |

---

## Estilos

- **CSS Modules:** cada componente y página tiene su propio archivo `.module.css`, garantizando estilos con alcance local y sin colisiones de clases.
- **Variables globales:** `variables.css` centraliza colores, tipografía y espaciado reutilizables en toda la aplicación.
- **Diseño responsivo:** el layout se adapta a diferentes tamaños de pantalla.

---

## Calidad de código

```bash
# Verificar tipos de TypeScript sin emitir archivos
npx tsc --noEmit

# Ejecutar el linter
npm run lint
```

La configuración de TypeScript (`tsconfig.app.json`) tiene habilitadas reglas estrictas: `noUnusedLocals`, `noUnusedParameters` y `noFallthroughCasesInSwitch`.

---

## Build de producción

```bash
npm run build
```

La salida se genera en la carpeta `dist/` como archivos estáticos listos para ser servidos desde cualquier servidor web o directamente desde el backend.

---

## Flujo de trabajo

- **Ramas:** `main` (producción), `develop` (desarrollo), `feature/*` (funcionalidades)
- **Commits:** convencionales — `feat:`, `fix:`, `docs:`, `refactor:`
- **Pull Requests:** requieren revisión antes de hacer merge a `develop`

---

## Equipo

- Miguel Angel Blandon
- Jeronimo Ospina
- Lidys Jaraba
- Daniel Salazar
- Juan Sebastian Rios

---

> Proyecto académico desarrollado con fines educativos.