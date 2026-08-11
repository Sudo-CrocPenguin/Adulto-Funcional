# Finanzas, gastos fijos y análisis móvil

## Qué es

Este módulo es el cliente móvil de las finanzas personales de Adulto
Funcional. Reúne tres experiencias relacionadas:

- **Finanzas:** consulta el libro de movimientos, calcula ingresos, egresos y
  saldo, permite buscar y registrar movimientos.
- **Gastos Fijos:** organiza pagos recurrentes por vencimiento, frecuencia,
  estado y clasificación.
- **Análisis financiero:** transforma los movimientos existentes en 20
  visualizaciones para observar tendencias, distribución y progreso.

La implementación sigue la arquitectura modular del cliente. El dominio
contiene reglas y cálculos inmutables; los casos de uso coordinan las acciones;
el repositorio HTTP conoce los endpoints; React Native solo presenta el
resultado. El backend no fue modificado para construir estas pantallas.

## Para qué sirve

El usuario puede:

- conocer sus totales acumulados de ingresos y egresos;
- consultar el saldo resultante de todos sus movimientos;
- buscar movimientos por descripción o clasificación;
- registrar un ingreso o egreso con fecha, monto y clasificación;
- crear y filtrar gastos fijos;
- registrar el pago de un gasto fijo como egreso financiero;
- avanzar automáticamente el próximo vencimiento según la recurrencia;
- revisar 20 lecturas visuales derivadas de su historial real;
- utilizar las pantallas en modo claro u oscuro y en Expo Go o web.

## Flujo de arquitectura

```text
FinanceScreen
  -> LoadFinancesUseCase
    -> HttpFinanceRepository
      -> GET /api/finances/movements
      -> GET /api/finances/categories?type=FINANCES
    -> FinanceLedger
      -> ingresos, egresos, saldo y búsqueda

NewMovementSheet
  -> CreateMovementUseCase
    -> MovementDraft
    -> POST /api/finances/movements

FixedExpensesScreen
  -> LoadFixedExpensesUseCase
    -> GET /api/finances/fixed-expenses
    -> GET /api/finances/categories?type=FINANCES
    -> FixedExpenseCollection
      -> filtros y próximos vencimientos

NewFixedExpenseSheet
  -> CreateFixedExpenseUseCase
    -> FixedExpenseDraft
    -> POST /api/finances/fixed-expenses

FinanceAnalyticsScreen
  -> LoadFinancesUseCase
    -> FinanceAnalytics
      -> series y métricas derivadas
    -> componentes SVG
```

Todos los endpoints autenticados reciben `Authorization: Bearer <token>`. El
cliente HTTP añade `X-Client-Type: mobile` y conserva el contrato común de
errores del backend. Los listados se recorren página por página, en lotes de
100 elementos, para no truncar historiales extensos.

## Finanzas

`FinanceLedger` conserva los movimientos recibidos y calcula:

```text
total ingresos = suma de movimientos INCOME
total egresos  = suma de movimientos EXPENSE
saldo actual   = total ingresos - total egresos
```

Los totales son acumulados porque corresponden a todos los movimientos
paginados de la cuenta. La búsqueda es local, no sensible a mayúsculas y
compara tanto la descripción como el nombre de la clasificación.

### Nuevo movimiento

El formulario envía el contrato existente:

```json
{
  "movementType": "EXPENSE",
  "amount": 180.5,
  "movementDate": "2099-08-10",
  "description": "Supermercado",
  "categoryId": "01988e6b-0c00-7000-8000-000000000011"
}
```

`MovementDraft` valida que:

- el tipo sea `INCOME` o `EXPENSE`;
- exista una clasificación financiera;
- el monto sea positivo, con hasta ocho enteros y dos decimales;
- la fecha sea válida;
- la descripción no incluya HTML.

La **fecha del movimiento** representa cuándo ocurrió la operación y la elige
el usuario. La **fecha de registro** la asigna el backend al crear el recurso;
por eso la interfaz la muestra como automática y no envía un valor ficticio.

## Gastos fijos

La pantalla ordena los gastos por `nextDueDate` y ofrece filtros combinables:

| Filtro | Valores |
|---|---|
| Frecuencia | semanal, quincenal, mensual, trimestral, semestral o anual |
| Estado | activo o inactivo |
| Clasificación | categorías de tipo `FINANCES` |
| Pestaña | todos o activos que vencen dentro de siete días |

Las tarjetas indican próximo pago, días restantes, monto, estado y frecuencia.
El color lateral representa urgencia: rojo para vencido o el día actual, ámbar
para los próximos siete días, verde para fechas posteriores y gris para un
gasto inactivo.

### Nuevo gasto fijo

`FixedExpenseDraft` exige nombre de máximo 20 caracteres, categoría,
frecuencia, monto positivo, estado válido y fecha de corte posterior al día
actual. El ejemplo usa una fecha lejana; debe sustituirse por el vencimiento
real al hacer una prueba:

```json
{
  "name": "Gimnasio",
  "frequency": "MONTHLY",
  "amount": 45,
  "status": "ACTIVE",
  "startDate": "2099-08-10",
  "reminderDays": 0,
  "nextDueDate": "2099-08-27",
  "categoryId": "01988e6b-0c00-7000-8000-000000000011"
}
```

### Registrar un pago

El backend actual no publica una operación transaccional de pago ni un estado
`PAID`; el gasto recurrente solo puede estar `ACTIVE` o `INACTIVE`. La
aplicación evita representar un estado que no existe y ofrece la acción
explícita `Registrar pago` para los gastos activos.

`PayFixedExpenseUseCase` coordina dos solicitudes, en este orden:

```text
1. POST /api/finances/movements
   movementType = EXPENSE
   amount        = monto del gasto fijo
   categoryId    = clasificación del gasto fijo
   description   = "Pago de gasto fijo: <nombre>"

2. PATCH /api/finances/fixed-expenses/{id}
   nextDueDate   = siguiente fecha de la recurrencia
```

Semanal suma 7 días; quincenal suma 14; las frecuencias mensual, trimestral,
semestral y anual avanzan meses conservando el día cuando existe y ajustándolo
al último día válido del mes. Si el vencimiento estaba atrasado, se avanza
tantas recurrencias como sea necesario hasta obtener una fecha futura.

Estas dos solicitudes **no son una transacción atómica**. Se crea primero el
egreso para no afirmar que un pago quedó reflejado en Finanzas si esa creación
falló. Si el egreso se crea y luego falla la actualización del vencimiento, la
interfaz informa exactamente el estado parcial y recarga los gastos: no intenta
crear otro movimiento automáticamente, porque eso podría duplicar el egreso.
Una atomicidad completa requerirá que el backend incorpore una operación de
pago única.

## Análisis financiero

El botón con forma de ojo en `Saldo actual` abre una pantalla desplazable con
20 visualizaciones SVG. Son compatibles con Expo Go gracias a
`react-native-svg` 15.12.1, versión soportada por Expo SDK 54.

| N.º | Visualización | Datos utilizados |
|---:|---|---|
| 1 | Barras verticales agrupadas | ingresos y egresos de seis meses |
| 2 | Dona | egresos del mes por clasificación |
| 3 | Línea | balance después de cada movimiento |
| 4 | Barras horizontales | referencia y gasto real por categoría |
| 5 | Cascada | saldo anterior, ingresos, egresos y saldo final |
| 6 | Mapa de calor | gasto de cada día del mes |
| 7 | Sparkline | balances mensuales y racha positiva |
| 8 | Medidor circular | ahorro actual frente a meta automática |
| 9 | Barras apiladas | ingreso del mes por fuente |
| 10 | Barras agrupadas comparativas | egreso actual frente al mes anterior |
| 11 | Radar | ahorro, gasto, estabilidad, ingresos, liquidez y referencia |
| 12 | Pastel | proporción de egresos por categoría |
| 13 | Área | balance acumulado con volumen relleno |
| 14 | Sankey | fuentes de ingreso hacia destinos de gasto |
| 15 | Treemap | tamaño proporcional de categorías de gasto |
| 16 | Pareto | categorías ordenadas y porcentaje acumulado |
| 17 | Mancuerna | distancia entre referencia y gasto real |
| 18 | Bullet chart | ahorro actual frente a meta |
| 19 | Calendario de gastos | días del mes con nivel bajo, medio o alto |
| 20 | Lista de progreso | consumo por categoría frente a referencia |

### Referencias calculadas

El backend no almacena todavía presupuestos por categoría ni metas de ahorro.
Para que los gráficos sean útiles sin inventar persistencia, la interfaz
declara y muestra estas reglas:

- **referencia de presupuesto:** gasto real de la misma categoría durante el
  mes anterior;
- **meta automática de ahorro:** 20% de los ingresos del mes actual;
- **ahorro actual:** parte positiva de `ingresos - egresos` del mes;
- **racha de ahorro:** cantidad de meses consecutivos, desde el actual hacia
  atrás, cuyo balance fue positivo.

Estas referencias se recalculan al abrir o actualizar la pantalla. No bloquean
operaciones ni se guardan como configuración del usuario.

## Estados y seguridad

Las tres pantallas admiten carga inicial, reintento, colección vacía y
actualización al deslizar. Los formularios proyectan los errores de campo de la
API. Los datos permanecen asociados al token de la sesión; ningún movimiento
financiero se guarda en almacenamiento general del dispositivo.

La campana de Finanzas avisa cuando el balance acumulado es negativo. En
Gastos Fijos presenta hasta tres vencimientos activos dentro de los siguientes
siete días. El engranaje mantiene la configuración compartida de tema.

## Límites actuales

- Registrar un pago consta de dos solicitudes y no es atómico sin soporte del
  backend.
- Presupuestos, metas y deudas no existen todavía como recursos de la API; los
  gráficos que los necesitan usan las referencias calculadas y declaradas.
- La edición y eliminación de movimientos o gastos fijos no se añadieron
  porque las referencias visuales entregadas definen consulta, creación,
  filtros y pago.
- Un análisis recién creado puede mostrar estados vacíos hasta que existan
  movimientos suficientes.

No se modificó, reinició ni amplió el backend durante este desarrollo.
