# Perfil

## Qué es y para qué sirve

Perfil reúne la identidad visible de la cuenta y un resumen de la actividad del
usuario. La pantalla permite consultar y editar los datos personales sin
duplicarlos en almacenamiento local, y muestra métricas calculadas desde los
módulos reales de compromisos, gastos fijos y contraseñas.

## Datos de la cuenta

La carga principal consulta `GET /api/account/{accountId}`. El identificador se
obtiene de la sesión autenticada y nunca se acepta desde un formulario.

Se presentan los siguientes campos:

- nombres y apellidos;
- correo electrónico;
- teléfono en formato internacional E.164;
- fecha real de creación de la cuenta;
- iniciales derivadas del nombre completo.

El backend no posee un campo ni un endpoint de avatar. Por eso la primera
versión no finge una subida de fotografía: muestra iniciales y el lápiz abre el
mismo editor de datos personales.

## Edición

`Editar perfil` abre un formulario con los valores actuales. El dominio valida
las mismas reglas que el contrato del backend:

- nombres y apellidos Unicode de máximo 50 caracteres;
- letras, marcas, espacios, apóstrofes y guiones en nombres;
- teléfono E.164, por ejemplo `+573001234567`;
- correo válido de máximo 255 caracteres;
- ausencia de HTML en los campos de texto.

Solo se envían los valores que cambiaron mediante
`PATCH /api/account/{accountId}`. Una respuesta correcta actualiza la pantalla
y sincroniza los datos de la sesión en memoria sin sustituir los access o
refresh tokens.

## Actividad

| Métrica | Fuente | Cálculo |
|---|---|---|
| Compromisos completados | `GET /api/agenda/events?status=Completado` | Total de eventos completados de todas las páginas |
| Racha máxima | Fechas de compromisos completados | Mayor secuencia histórica de días únicos consecutivos |
| Gastos fijos registrados | `GET /api/finances/fixed-expenses` | `totalElements` de la página |
| Contraseñas guardadas | Estado de Master Key y `GET /api/security/passwords` | `totalElements` únicamente si la bóveda está desbloqueada |

La API protege incluso el listado de metadatos de contraseñas con la sesión de
Master Key. Cuando la bóveda está bloqueada, Perfil muestra `—` y el texto
`Bóveda bloqueada`; no fuerza un desbloqueo ni presenta un cero falso.

## Cambio de contraseña

El contrato actual permite editar nombres, apellidos, teléfono y correo, pero
no expone una operación para cambiar la contraseña de acceso. La fila `Cambiar
contraseña` explica esta limitación y no simula una actualización local o
remota. La Master Key se administra de manera independiente en el Gestor de
Contraseñas.

## Estados de interfaz

- carga inicial con indicador;
- error recuperable con botón `Reintentar`;
- actualización mediante gesto de refresco;
- confirmación después de una edición correcta;
- errores locales por campo y errores estructurados del backend;
- formularios adaptados al teclado y a modo claro u oscuro.

## Arquitectura

```text
src/modules/profile/
  domain/          perfil, actividad, borrador y contrato de repositorio
  application/     casos de uso de carga y actualización
  infrastructure/ adaptador HTTP y composición de métricas
  presentation/   pantalla, tarjetas, editor y aviso de contraseña
```

La pantalla accede a las operaciones mediante el contenedor de dependencias.
El dominio no conoce React Native, Expo ni HTTP.

## Pruebas

Las pruebas unitarias verifican:

- normalización y actualización parcial;
- nombres Unicode, correo y teléfono E.164;
- rachas con fechas duplicadas y saltos entre días;
- contador desconocido cuando la bóveda está bloqueada;
- rutas, autorización y paginación del repositorio;
- conservación de los tokens al sincronizar el perfil con la sesión.

```bash
npm test -- --runInBand
```
