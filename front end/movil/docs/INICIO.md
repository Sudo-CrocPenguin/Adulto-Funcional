# Inicio móvil

## Qué es

`HomeScreen` es la primera pantalla del área autenticada de Adulto Funcional.
Resume en un solo lugar el estado financiero, la agenda de compromisos, los
gastos recurrentes y la bóveda de credenciales del usuario que inició sesión.

No contiene cifras de demostración. Cada valor procede de endpoints existentes
del backend o de un cálculo determinista sobre sus respuestas.

## Para qué sirve

El inicio permite responder rápidamente estas preguntas:

- ¿Cuál es el balance de mis movimientos registrados?
- ¿Cuántos compromisos siguen pendientes?
- ¿Cuántos gastos fijos activos se aproximan?
- ¿Cuántas credenciales puedo consultar en la sesión desbloqueada?
- ¿Cuál es mi racha actual de compromisos completados?
- ¿Qué gasto fijo y compromiso ocurren primero?
- ¿Cómo se distribuyeron mis movimientos durante los últimos tres meses?

También define la navegación inferior que se conectará progresivamente a las
pantallas de compromisos, finanzas, gastos fijos, contraseñas y perfil.

## Cómo funciona

La presentación ejecuta `LoadDashboardUseCase` con la sesión autenticada. El
caso de uso depende del contrato `DashboardRepository`; su implementación HTTP
consulta la API con `Authorization: Bearer <access-token>`. El dominio recibe
los recursos obtenidos y crea un `DashboardSnapshot` inmutable.

```text
HomeScreen
  -> LoadDashboardUseCase
    -> HttpDashboardRepository
      -> ApiClient
        -> API autenticada
    -> DashboardSnapshot
      -> cálculos de balance, racha y estadísticas
```

La pantalla admite carga inicial, actualización al deslizar, error recuperable
y colecciones vacías. Si una actualización falla después de haber cargado
datos, conserva la última información y muestra el error de forma no invasiva.

## Origen de los datos

| Elemento visual | Fuente | Regla |
|---|---|---|
| Saldo actual | `/api/finances/movements` | ingresos menos egresos de todas las páginas |
| Compromisos pendientes | `/api/agenda/events?status=Pendiente` | `page.totalElements` |
| Próximos gastos | `/api/finances/fixed-expenses?status=ACTIVE` | `page.totalElements` |
| Contraseñas | `/api/security/passwords` | total consultado solo con Master Key verificada |
| Próximo gasto fijo | gastos fijos activos | primer elemento ordenado por `nextDueDate ASC` |
| Próximo compromiso | eventos pendientes desde hoy | primer elemento ordenado por `startHour ASC` |
| Badge superior | compromisos pendientes | reutiliza el total porque no existe API de notificaciones |
| Estado de bóveda | `/api/security/master-key/status` | decide si el total de credenciales puede consultarse |

Los listados que participan en sumas o rachas se recorren página por página con
el tamaño máximo aceptado por la API. De esta forma los cálculos no dependen
únicamente de los primeros veinte resultados.

## Reglas de cálculo

### Balance

```text
saldo = suma(INCOME) - suma(EXPENSE)
```

Los montos inválidos se interpretan como cero para evitar que una respuesta
incompleta convierta todo el resumen en `NaN`.

### Racha de compromisos

La racha usa fechas únicas de eventos `Completado` durante una ventana de 30
días. Empieza hoy si existe al menos un evento completado hoy; de lo contrario,
puede empezar ayer. Retrocede un día a la vez hasta encontrar el primer día sin
actividad. Se limita a 30 para coincidir con los hitos visuales 7, 15, 23 y 30.

### Estadísticas

El reporte toma los movimientos comprendidos entre hoy y tres meses atrás:

- `Ingresos`: suma de movimientos `INCOME`.
- `Egresos`: suma de movimientos `EXPENSE`.
- `Ocio`: suma de movimientos cuya categoría normalizada es `Ocio`.
- `Ahorros`: suma de movimientos cuya categoría normalizada es `Ahorro` o
  `Ahorros`.

La normalización ignora mayúsculas y tildes. Las barras usan una escala relativa
al mayor valor del periodo y representan explícitamente los valores en cero.

## Seguridad y sesión

El inicio utiliza únicamente el access token en memoria. El refresh token
permanece en `expo-secure-store` y no se entrega a los componentes visuales.

El endpoint de credenciales no se consulta cuando la Master Key está bloqueada.
En ese caso el indicador muestra `—`; no equivale a cero credenciales. Esta
distinción evita revelar o inferir datos protegidos sin una sesión de bóveda
verificada.

## Límites actuales

- El backend no expone un endpoint agregado de dashboard; el cliente compone
  varias consultas autenticadas.
- No existe un módulo de notificaciones. La campana muestra compromisos
  pendientes hasta que se defina ese contrato.
- Los botones de la navegación inferior anuncian la siguiente etapa mientras
  se reciben los diseños de cada pantalla.
- La vista web de Expo sirve para revisión visual. La autenticación nativa debe
  probarse en Expo Go porque navegador y aplicación móvil usan contratos de
  transporte diferentes.

Ninguna de estas limitaciones se oculta mediante datos ficticios o respuestas
simuladas en producción.
