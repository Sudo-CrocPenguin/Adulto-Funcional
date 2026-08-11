# Referencia de la API

## Convenciones

- URL local: `http://localhost:8080`.
- Formato: JSON UTF-8.
- Tamaño máximo del cuerpo: 1 MiB por defecto.
- Identificadores: UUID v7 en formato canónico.
- Fechas: ISO 8601 `YYYY-MM-DD`.
- Horas civiles: ISO 8601 sin offset, por ejemplo `2099-08-04T09:30:00`.
- Instantes: ISO 8601 UTC, por ejemplo `2099-08-04T14:30:00Z`.
- Los PATCH modifican únicamente campos no nulos; un string vacío no significa
  borrar y se rechaza donde existe una regla de no-vacío.

El servidor expone 41 endpoints propios y el healthcheck de Actuator.

## Autenticación

### Rutas públicas

- `GET /api/auth/csrf`
- `POST /api/auth/login`
- `POST /api/auth/register`
- `POST /api/auth/refresh`
- `GET /actuator/health`

Todas las demás rutas requieren un access token válido.

### Navegador

El access token se transporta en la cookie `token`, con `HttpOnly`, `Path=/`,
`SameSite` configurable y `Secure` obligatorio en producción. El refresh token
usa `refresh_token`, `HttpOnly` y `Path=/api/auth/refresh`.

Para una operación no segura autenticada por cookie:

1. Obtener `GET /api/auth/csrf` conservando cookies.
2. Leer `data.token` o la cookie `XSRF-TOKEN`.
3. Enviar el valor en `X-XSRF-TOKEN`.

Ejemplo conceptual con curl:

```bash
curl -sS -c cookies.txt http://localhost:8080/api/auth/csrf

curl -sS -b cookies.txt -c cookies.txt \
  -H 'Content-Type: application/json' \
  -H 'X-XSRF-TOKEN: <token-csrf>' \
  -d '{"email":"ana@example.com","password":"contraseña"}' \
  http://localhost:8080/api/auth/login
```

En producción las cookies `Secure` requieren HTTPS.

### Cliente nativo

Un cliente nativo envía:

```http
X-Client-Type: mobile
User-Agent: MiApp/1.0
```

También se aceptan señales conocidas como `okhttp`, `Alamofire` o el agente de
desktop. El header declarativo no actúa solo: debe coincidir con una señal
pasiva no-browser.

Login, registro y refresh devuelven `token` y `refreshToken` en el body. El
access token se presenta como Bearer:

```http
Authorization: Bearer eyJ...
```

Una operación autenticada por Bearer válido no exige CSRF.

## Sobres de respuesta

### Éxito

```json
{
  "status": 200,
  "message": "Operación exitosa",
  "data": {}
}
```

### Listado

```json
{
  "status": 200,
  "message": "Recursos listados exitosamente",
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 0,
    "totalPages": 0,
    "hasNext": false,
    "hasPrevious": false
  },
  "data": []
}
```

### Error

```json
{
  "status": 400,
  "code": "VALIDATION_FAILED",
  "message": "La solicitud contiene datos inválidos",
  "fieldErrors": [
    {
      "field": "email",
      "code": "Email",
      "message": "El formato del email no es válido"
    }
  ],
  "traceId": "4c1f5a5e1fc34e0e",
  "data": null
}
```

El catálogo completo está en [API_ERROR_CONTRACT.md](./API_ERROR_CONTRACT.md).

## Paginación común

Los endpoints de listado aceptan:

| Parámetro | Predeterminado | Regla |
|---|---:|---|
| `page` | `0` | entero base cero, no negativo |
| `size` | `20` | entre 1 y 100 |
| `sortBy` | depende del recurso | solo campos permitidos |
| `sortDirection` | depende del recurso | `ASC` o `DESC` |

El servidor añade el UUID al orden para desempatar de manera determinista.

## Autenticación y sesiones

| Método | Ruta | Acceso | Entrada | Resultado |
|---|---|---|---|---|
| `GET` | `/api/auth/csrf` | Público | — | token, header y parámetro CSRF |
| `POST` | `/api/auth/login` | Público | `LoginRequest` | sesión y datos de cuenta |
| `POST` | `/api/auth/register` | Público | `RegisterRequest` | `201`, cuenta y sesión |
| `POST` | `/api/auth/refresh` | Público | body nativo opcional o cookie | rota el par de tokens |
| `POST` | `/api/auth/logout` | Autenticado | — | revoca sesión actual y limpia cookies |
| `DELETE` | `/api/auth/sessions/current` | Autenticado | — | revoca sesión actual |
| `DELETE` | `/api/auth/sessions` | Autenticado | — | revoca todas las sesiones de la cuenta |

### `LoginRequest`

```json
{
  "email": "ana@example.com",
  "password": "secreto"
}
```

- `email`: obligatorio, válido, máximo 255.
- `password`: obligatorio, máximo 128. Login conserva compatibilidad con
  cuentas creadas bajo políticas anteriores.

El mensaje de fallo no distingue email inexistente de contraseña incorrecta y
el caso de uso ejecuta Argon2 ficticio cuando la cuenta no existe.

### `RegisterRequest`

```json
{
  "names": "Ana María",
  "lastnames": "O'Connor Ruiz",
  "phone": "+573001234567",
  "email": "ana@example.com",
  "password": "una frase secreta extensa",
  "masterKey": "otra frase independiente"
}
```

- Nombres y apellidos: Unicode, máximo 50, letras, espacios, apóstrofes y
  guiones.
- Teléfono: E.164.
- Email: cualquier dominio válido, máximo 255.
- Contraseña: 15–128 caracteres.
- Master Key: opcional, 15–128 si se envía.

### Refresh

Nativo:

```json
{
  "refreshToken": "<token-opaco>"
}
```

Web: body vacío y cookie `refresh_token`. El token es de un solo uso. Dos
renovaciones concurrentes dentro de la ventana devuelven
`409/REFRESH_ALREADY_ROTATED`; reutilizarlo después revoca la familia con
`401/REFRESH_TOKEN_REUSED`.

### `AuthResponse.data`

| Campo | Web | Nativo |
|---|---|---|
| `token` | `null` | access JWT |
| `refreshToken` | `null` | refresh opaco |
| `tokenType` | `null` | `Bearer` |
| `expiresIn` | milisegundos | milisegundos |
| `refreshExpiresIn` | milisegundos | milisegundos |
| `sessionId` | UUID | UUID |
| `roles` | autoridades | autoridades |
| `accountId`, perfil, `createdAt` | presente | presente |
| `hasMasterKey` | presente | presente |

Las respuestas de autenticación incluyen `Cache-Control: no-store`.

## Cuentas

| Método | Ruta | Entrada | Resultado |
|---|---|---|---|
| `GET` | `/api/account/{id}` | — | perfil propio |
| `PATCH` | `/api/account/{id}` | `UpdateAccountRequest` | perfil actualizado |
| `DELETE` | `/api/account/{id}` | `DeleteAccountRequest` | elimina cuenta y dependencias |

El `{id}` debe coincidir con `sub` antes de consultar. Un ID ajeno responde
igual que uno inexistente.

`UpdateAccountRequest` admite campos opcionales `names`, `lastnames`, `phone` y
`email`, con las mismas políticas de Unicode, E.164 y email del registro.

Eliminar requiere reautenticación:

```json
{
  "currentPassword": "contraseña actual"
}
```

La operación revoca sesiones, elimina estado Redis y borra las dependencias por
cascada. No es recuperable por la API.

## Movimientos

| Método | Ruta | Entrada/consulta | Resultado |
|---|---|---|---|
| `POST` | `/api/finances/movements` | `CreateMovementRequest` | `201`, movimiento |
| `GET` | `/api/finances/movements/{id}` | — | movimiento propio |
| `GET` | `/api/finances/movements` | filtros | página |
| `PATCH` | `/api/finances/movements/{id}` | `UpdateMovementRequest` | movimiento actualizado |
| `DELETE` | `/api/finances/movements/{id}` | — | confirmación |

Creación:

```json
{
  "movementType": "EXPENSE",
  "amount": 125000.50,
  "movementDate": "2099-08-03",
  "description": "Mercado semanal",
  "categoryId": "01988e6b-0c00-7000-8000-000000000001"
}
```

- `movementType`: `INCOME` o `EXPENSE`.
- `amount`: positivo, hasta 8 enteros y 2 decimales.
- `movementDate` y categoría `FINANCES`: obligatorias.
- `description`: opcional, sin HTML.

Filtros: `startDate`, `endDate`, `movementType`, `categoryId`, `searchTerm` y
paginación. `startDate <= endDate`.

Orden permitido: `movementDate`, `amount`, `movementType`, `registerDate`,
`id`. Predeterminado: `movementDate DESC`.

Respuesta: `id`, `movementType`, `amount`, `registerDate` UTC, `description`,
`movementDate` y categoría completa.

## Categorías

| Método | Ruta | Entrada/consulta | Resultado |
|---|---|---|---|
| `POST` | `/api/finances/categories` | `CreateCategoryRequest` | `201`, categoría PERSONAL |
| `GET` | `/api/finances/categories/{id}` | — | categoría accesible |
| `GET` | `/api/finances/categories` | filtros | SYSTEM + personales propias |
| `PATCH` | `/api/finances/categories/{id}` | `UpdateCategoryRequest` | renombra PERSONAL |
| `DELETE` | `/api/finances/categories/{id}` | — | elimina PERSONAL sin referencias |

Creación:

```json
{
  "name": "Mascotas",
  "type": "FINANCES"
}
```

`type` acepta `FINANCES` o `AGENDA`. El servidor asigna `scope=PERSONAL` y el
propietario desde el JWT. El nombre es único por propietario, alcance y tipo
después de normalización NFKC.

Las categorías `SYSTEM` son visibles pero inmutables para usuarios. En PATCH,
`type` puede omitirse o repetir el actual; no puede cambiarlo.

Filtros: `type`, `searchTerm` y paginación. Orden permitido: `name`, `type`,
`scope`, `id`. Predeterminado: `name ASC`.

## Gastos fijos

| Método | Ruta | Entrada/consulta | Resultado |
|---|---|---|---|
| `POST` | `/api/finances/fixed-expenses` | `CreateFixedExpenseRequest` | `201`, gasto |
| `GET` | `/api/finances/fixed-expenses/{id}` | — | gasto propio |
| `GET` | `/api/finances/fixed-expenses` | filtros | página |
| `PATCH` | `/api/finances/fixed-expenses/{id}` | `UpdateFixedExpenseRequest` | gasto actualizado |
| `DELETE` | `/api/finances/fixed-expenses/{id}` | — | confirmación |

Ejemplo con fechas deliberadamente lejanas para evitar que el contrato de
creación caduque; reemplázalas por fechas válidas del caso real:

```json
{
  "name": "Arriendo",
  "frequency": "MONTHLY",
  "amount": 1500000.00,
  "status": "ACTIVE",
  "startDate": "2099-08-01",
  "reminderDays": 3,
  "nextDueDate": "2099-09-01",
  "categoryId": "01988e6b-0c00-7000-8000-000000000003"
}
```

- Frecuencias: `WEEKLY`, `BIWEEKLY`, `MONTHLY`, `QUARTERLY`, `SEMIANNUAL`,
  `ANNUAL`.
- Estados: `ACTIVE`, `INACTIVE`.
- `startDate` es opcional y usa la fecha del servidor si falta.
- `reminderDays` es opcional y usa cero si falta.
- `nextDueDate` debe ser posterior al día actual y no puede ser anterior a
  `startDate`.
- Categoría `FINANCES` accesible y monto con precisión monetaria obligatorios.

Filtros: `status`, `categoryId`, `searchTerm` y paginación. Orden permitido:
`nextDueDate`, `amount`, `name`, `status`, `frequency`, `id`. Predeterminado:
`nextDueDate ASC`.

## Eventos

| Método | Ruta | Entrada/consulta | Resultado |
|---|---|---|---|
| `POST` | `/api/agenda/events` | `EventRequest` | `201`, evento |
| `GET` | `/api/agenda/events/{eventId}` | — | evento propio |
| `GET` | `/api/agenda/events` | filtros | página |
| `PATCH` | `/api/agenda/events/{eventId}` | `EventUpdateRequest` | evento actualizado |
| `DELETE` | `/api/agenda/events/{eventId}` | — | confirmación |

Ejemplo con una fecha futura deliberadamente lejana. En una prueba real se debe
usar la zona y una fecha futura coherente con el reloj del servidor:

```json
{
  "title": "Cita médica",
  "priority": "Alta",
  "eventDate": "2099-08-10",
  "zoneId": "America/Bogota",
  "frequency": 0,
  "reminder": "2099-08-10T08:30:00",
  "startHour": "2099-08-10T09:00:00",
  "endHour": "2099-08-10T10:00:00",
  "description": "Control anual",
  "status": "Pendiente",
  "categoryId": "01988e6b-0c00-7000-8000-000000000011"
}
```

- Prioridad: `Baja`, `Media`, `Alta`.
- Estado: `Pendiente`, `Completado`, `Cancelado`, `Pospuesto`.
- Frecuencia: `0`, `1`, `7`, `30`, `365` días.
- `zoneId`: IANA; usa la zona de aplicación si se omite.
- `eventDate` debe coincidir con inicio y fin civiles.
- `reminder < startHour < endHour` en tiempo civil e instantes.
- Categoría `AGENDA` accesible obligatoria.

La respuesta añade `reminderInstant`, `startInstant` y `endInstant` UTC sin
retirar las horas civiles ni la zona.

Filtros: `status`, `priority`, `categoryId`, `startDate`, `endDate` y
paginación. Orden permitido: `eventDate`, `startHour`, `priority`, `status`,
`title`, `id`. Predeterminado: `startHour ASC`.

## Master Key

Contrato canónico:

| Método | Ruta | Entrada | Resultado |
|---|---|---|---|
| `GET` | `/api/security/master-key/status` | — | configuración y sesión |
| `POST` | `/api/security/master-key` | configuración reautenticada | `201` |
| `POST` | `/api/security/master-key/verify` | Master Key | desbloquea sesión |
| `PATCH` | `/api/security/master-key` | rotación reautenticada | recifra bóveda |
| `DELETE` | `/api/security/master-key/session` | — | bloquea sesión actual |

Estado:

```json
{
  "configured": true,
  "verified": true,
  "expiresAt": "2099-08-03T20:00:00Z"
}
```

Configurar por primera vez:

```json
{
  "currentPassword": "contraseña de cuenta",
  "newMasterKey": "frase maestra independiente"
}
```

Verificar:

```json
{
  "masterKey": "frase maestra independiente"
}
```

Rotar:

```json
{
  "currentPassword": "contraseña de cuenta",
  "currentMasterKey": "frase maestra actual",
  "newMasterKey": "nueva frase maestra"
}
```

Configurar y rotar exigen la contraseña de cuenta. La nueva Master Key debe
tener 15–128 caracteres. Rotar recifra todas las credenciales de forma
transaccional e invalida los desbloqueos existentes.

Compatibilidad temporal: `POST /api/security/passwords/master-key/verify`
acepta el mismo `VerifyMasterKeyRequest`. Los clientes nuevos deben usar la
ruta canónica.

## Bóveda de credenciales

Todos los endpoints exigen autenticación y Master Key verificada en la sesión
actual.

| Método | Ruta | Entrada/consulta | Resultado |
|---|---|---|---|
| `POST` | `/api/security/passwords` | `PasswordRequest` | `201`, metadatos |
| `GET` | `/api/security/passwords` | filtros | página sin texto plano |
| `GET` | `/api/security/passwords/{id}` | — | credencial descifrada |
| `PATCH` | `/api/security/passwords/{id}` | `PasswordUpdateRequest` | metadatos actualizados |
| `DELETE` | `/api/security/passwords/{id}` | — | confirmación |

Creación:

```json
{
  "applicationName": "GitHub",
  "password": "secreto almacenado",
  "lastChangeDate": "2099-08-03"
}
```

- Aplicación: obligatoria, máximo 35 y única por cuenta.
- Secreto: obligatorio, máximo 2032 bytes UTF-8.
- Fecha: opcional; usa la fecha del servidor si falta.

PATCH admite esos tres campos opcionales. Si cambia la contraseña se genera
nuevo salt, IV, ciphertext v2 y fecha cuando corresponda.

Filtros: `searchTerm` y paginación. Orden permitido: `applicationName`,
`lastChangeDate`, `id`. Predeterminado: `applicationName ASC`.

El listado y las respuestas de escritura devuelven `password: null`. Solo el
GET individual expone el valor descifrado. Nunca se exponen salt, IV,
ciphertext, versión criptográfica ni Master Key.

## Headers relevantes

| Header | Dirección | Propósito |
|---|---|---|
| `Authorization` | request | Bearer nativo |
| `X-Client-Type` | request | intención `mobile`/`desktop` |
| `Origin` | request | validación CORS |
| `X-XSRF-TOKEN` | request | CSRF de navegador |
| `X-Trace-Id` | ambas | correlación; se genera si falta/no es válido |
| `Retry-After` | response | espera de rate limiting |
| `WWW-Authenticate` | response 401 | esquema Bearer |
| `Allow` | response 405 | métodos soportados |
| `Cache-Control` | response | `no-store` para datos sensibles |

CORS permite `GET`, `POST`, `PUT`, `PATCH`, `DELETE` y `OPTIONS`; admite
credenciales, expone `X-Trace-Id` y rechaza orígenes no configurados con
`403/CORS_REQUEST_REJECTED` uniforme.

## Ownership y concurrencia

- La cuenta siempre se deriva del JWT; ningún endpoint acepta `accountId` en el
  body como fuente de autorización.
- Un UUID ajeno devuelve `404/RESOURCE_NOT_FOUND`.
- Una escritura concurrente detectada devuelve
  `409/CONCURRENT_MODIFICATION`.
- Duplicados y recursos referenciados devuelven códigos `409` del contrato.

## Healthcheck

```http
GET /actuator/health
```

```json
{"status":"UP"}
```

Es público y no expone detalles internos adicionales.
