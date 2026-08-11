# ADR 0007: Detección de cliente nativo para respuestas de autenticación

- Estado: aceptada
- Implementación: completa
- Fecha: 2026-08-11
- Alcance: `auth`, configuración de seguridad y clientes nativos
- Reemplaza: regla de transporte nativo del ADR 0002

## Contexto

El ADR 0002 indicó que una solicitud solo se consideraría nativa al presentar
un Bearer válido y que no se decidiría por `User-Agent`. Esa condición no puede
aplicarse al registro, login o primer refresh: precisamente son las operaciones
que deben emitir el token y todavía no existe un Bearer válido.

Al mismo tiempo, confiar únicamente en `X-Client-Type` permitiría que JavaScript
de un navegador solicite tokens en el body y evite el transporte por cookies
HttpOnly. Se necesita detectar la intención del cliente antes de autenticar sin
aceptar una señal declarativa aislada.

## Decisión

Una solicitud se considera nativa únicamente cuando se cumplen las dos
condiciones siguientes:

1. `X-Client-Type` declara `mobile` o `desktop`;
2. existe al menos una señal pasiva compatible:
   - `User-Agent` conocido: `MiApp/`, `MiAppDesktop/`, `okhttp` o `Alamofire`;
   - ausencia simultánea de `Origin` y `Referer`, con un agente que no contenga
     `Mozilla`.

El header declarativo nunca actúa solo. Una solicitud de navegador se trata
como web y recibe cookies HttpOnly, aunque intente añadir el header. Login,
registro y refresh nativos pueden devolver access y refresh token en el body
sin requerir un Bearer anterior.

Después de autenticar, las operaciones con `Authorization: Bearer` no requieren
CSRF. Las operaciones por cookie sí conservan CSRF; CORS no sustituye esa
protección.

## Contrato de clientes

El cliente Expo envía:

```http
X-Client-Type: mobile
User-Agent: MiApp/<version> (<plataforma>)
```

Los clientes desconocidos deben recibir por defecto el contrato web. Ningún
proxy debe inventar ni transformar `X-Client-Type`, `Origin`, `Referer` o
`User-Agent` sin una decisión posterior.

## Consecuencias

- El login nativo puede recibir tokens antes de tener autenticación.
- Un browser no obtiene tokens en el body solo por falsificar el header
  declarativo.
- La detección usa señales heurísticas y debe conservar pruebas de navegador,
  Expo, okhttp y requests sin agente.
- Los cambios futuros del identificador móvil deben coordinar backend y
  `ApiClient` en la misma release.

## Evidencia

- `ClientTypeResolver` implementa las tres capas.
- `API_REFERENCE.md` documenta los headers y el resultado web/nativo.
- `ApiClient` móvil envía la declaración y un agente propio.
