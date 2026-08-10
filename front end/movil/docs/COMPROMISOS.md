# Compromisos móvil

## Qué es

La sección de Compromisos es el cliente móvil del módulo de agenda personal.
Permite consultar los eventos de la cuenta autenticada, distinguir su estado,
visualizar la racha de actividad y crear un compromiso válido mediante la API
existente.

La implementación reproduce la referencia visual con encabezado autenticado,
tarjeta de racha, filtros, tarjetas por prioridad, navegación inferior y un
formulario modal abierto desde el botón flotante `+`.

## Para qué sirve

La pantalla permite:

- consultar todos los compromisos asociados a la cuenta;
- filtrar localmente por pendientes o completados;
- reconocer categoría, frecuencia, prioridad y fecha de cada elemento;
- calcular la racha de días consecutivos con compromisos completados;
- crear compromisos con categoría, recurrencia, prioridad y recordatorio;
- actualizar el listado inmediatamente después de una creación exitosa;
- conservar las acciones compartidas de notificaciones y tema.

## Cómo funciona

La presentación depende de casos de uso y no construye solicitudes HTTP. La
composición inyecta `LoadCommitmentsUseCase` y `CreateCommitmentUseCase`, ambos
sobre el contrato `CommitmentRepository`.

```text
CommitmentsScreen
  -> LoadCommitmentsUseCase
    -> HttpCommitmentRepository
      -> GET /api/agenda/events
      -> GET /api/finances/categories?type=AGENDA
    -> CommitmentCollection
      -> filtros y cálculo de racha

NewCommitmentSheet
  -> CreateCommitmentUseCase
    -> CommitmentDraft
      -> validación y horario civil
    -> HttpCommitmentRepository
      -> POST /api/agenda/events
```

Todos los endpoints reciben `Authorization: Bearer <access-token>` y
`X-Client-Type: mobile` a través del cliente HTTP compartido. Los listados se
recorren página por página con el máximo de 100 elementos aceptado por la API.

## Listado y filtros

La API entrega eventos ordenados por `startHour ASC`. La aplicación los
representa como entidades `Commitment` inmutables y aplica estas pestañas:

| Pestaña | Regla |
|---|---|
| Todas | conserva todos los estados recibidos |
| Pendientes | `status === "Pendiente"` |
| Completadas | `status === "Completado"` |

Los eventos cancelados o pospuestos aparecen únicamente en `Todas`. Las
tarjetas usan el color de la prioridad como acento: rojo para Alta, ámbar para
Media y verde para Baja. Un compromiso completado se muestra atenuado y con el
título tachado.

## Racha

`CommitmentCollection` toma las fechas únicas de compromisos completados. La
racha comienza hoy si hay actividad completada hoy; de lo contrario puede
comenzar ayer. Retrocede hasta el primer día sin actividad y se limita a 30
días para coincidir con los hitos 7, 15, 23 y 30 de la interfaz.

## Creación

El formulario muestra los campos del diseño y añade las horas de inicio y fin
exigidas por el backend. Fecha y hora usan el selector nativo compatible con
Expo Go; la versión web ofrece entradas `AAAA-MM-DD` y `HH:mm`.

### Catálogos

| Campo | Valores enviados |
|---|---|
| Categoría | UUID de una categoría accesible con `type=AGENDA` |
| Frecuencia | Una vez `0`, Diario `1`, Semanal `7`, Mensual `30`, Anual `365` |
| Prioridad | `Alta`, `Media`, `Baja` |
| Recordatorio | 15, 30, 60 o 1440 minutos antes del inicio |
| Estado inicial | `Pendiente` |

### Reglas locales

Antes de enviar la solicitud, `CommitmentDraft` comprueba que:

- el nombre exista, no supere 35 caracteres y no contenga HTML;
- la categoría sea obligatoria;
- frecuencia, prioridad y recordatorio pertenezcan a sus catálogos;
- la fecha sea actual o futura;
- la hora final sea posterior a la hora inicial.

La fecha seleccionada se combina con las horas como tiempo civil, el
recordatorio se calcula hacia atrás desde el inicio y se incluye la zona IANA
del dispositivo. Ejemplo de solicitud:

```json
{
  "title": "Reunión de equipo",
  "categoryId": "01988e6b-0c00-7000-8000-000000000011",
  "priority": "Alta",
  "eventDate": "2026-08-11",
  "zoneId": "America/Bogota",
  "frequency": 7,
  "reminder": "2026-08-11T08:00:00",
  "startHour": "2026-08-11T09:00:00",
  "endHour": "2026-08-11T10:00:00",
  "status": "Pendiente"
}
```

Los errores de campo del backend se proyectan nuevamente sobre el formulario.
Si no existen categorías `AGENDA` accesibles, la pantalla explica la condición
y desactiva `Guardar` para evitar una solicitud inválida.

## Navegación y estados

La barra inferior navega entre Inicio y Compromisos mediante el stack nativo.
Las demás secciones conservan el aviso de próxima etapa. Compromisos admite
carga inicial, reintento, colección vacía y actualización al deslizar.

La campana presenta como aviso contextual el siguiente compromiso pendiente
del listado. El engranaje reutiliza la configuración compartida de tema claro
u oscuro.

## Límites actuales

- La pantalla implementa las acciones mostradas en el diseño: consulta,
  filtros y creación. Edición, cambio de estado y eliminación se incorporarán
  cuando se definan sus interacciones visuales.
- La API no expone un endpoint de racha; el cálculo se realiza sobre los
  eventos paginados recibidos.
- La creación necesita al menos una categoría de tipo `AGENDA` existente en la
  cuenta o en el catálogo del sistema.

No se modificó ni se amplió el backend para implementar esta sección.
