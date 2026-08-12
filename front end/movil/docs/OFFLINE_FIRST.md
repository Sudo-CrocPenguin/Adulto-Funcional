# Operación offline y sincronización diferida

## Estado

Esta política está **propuesta y aún no implementada**. La versión 0.3.0 sigue
un modelo cliente-servidor en línea. El documento registra el objetivo de que
una cuenta conocida continúe usando la aplicación mientras `server1` está
apagado y sincronice los cambios cuando el homelab vuelva a estar disponible.

## Comportamiento actual

MariaDB usa el volumen Docker `mariadb_data`, por lo que un apagado normal del
servidor no elimina cuentas ni información persistida. Redis no persiste en
disco de forma intencional: al reiniciar se pierden desbloqueos temporales de
Master Key y contadores efímeros, no los datos de negocio.

El teléfono solo guarda el refresh token mediante `expo-secure-store` y la
preferencia de tema mediante `AsyncStorage`. Access token, perfil, compromisos,
movimientos, gastos fijos y datos de la bóveda no tienen una copia local
duradera. Todos los repositorios funcionales son adaptadores HTTP.

Por tanto, con `server1` apagado:

- no se puede registrar una cuenta ni iniciar sesión por primera vez;
- al reabrir la app, la restauración intenta rotar el refresh token contra la
  API y termina en la pantalla de acceso si la conexión falla;
- una sesión que siga en memoria deja de poder leer o escribir al consultar la
  API y su access token expira normalmente a los 15 minutos;
- no se encolan movimientos, compromisos ni cambios para enviarlos después;
- los procesos programados del backend no se ejecutan hasta volver a encenderlo;
- `MandatoryUpdateGate` puede bloquear el inicio si EAS no es alcanzable,
  aunque este servicio es independiente del homelab.

Apagar el equipo cortando la energía sí puede corromper el sistema de archivos
o la base de datos. Debe usarse un apagado ordenado y mantenerse el esquema de
backups; un volumen persistente no sustituye un respaldo.

## Objetivo offline-first

Después de completar una autenticación online al menos una vez, el dispositivo
debe poder abrir la cuenta conocida, leer su última copia local y registrar
cambios aunque la API no responda. El servidor conserva la autoridad final y
recibe las operaciones pendientes cuando vuelve a estar disponible.

```text
Pantallas y casos de uso
        │
        ▼
Repositorio local cifrado ──► estado visible inmediato
        │
        ▼
Outbox de operaciones pendientes
        │
        ├── servidor apagado: conserva y muestra "Pendiente"
        │
        └── servidor disponible: sincroniza por HTTPS
                                 │
                                 ▼
                         MariaDB confirma la operación
                                 │
                                 ▼
                         outbox marca "Sincronizado"
```

## Reglas propuestas

### Sesión local

- El primer registro y el primer login siempre requieren el servidor.
- Después de un login exitoso se guarda un perfil mínimo de cuenta separado de
  los tokens. No se almacena la contraseña principal.
- La entrada offline se protege con las capacidades de bloqueo del dispositivo
  o un mecanismo local explícito. No se presenta como una sesión validada por
  el servidor.
- Al recuperar conexión se rota el refresh token. Si el servidor lo rechaza de
  forma terminal, se bloquea la sincronización y se solicita autenticación sin
  borrar silenciosamente las operaciones pendientes.

### Persistencia local

- Una base local transaccional mantiene las últimas entidades confirmadas y la
  outbox. `AsyncStorage` no se usa como base de datos de negocio.
- Cada operación se identifica en el teléfono antes de sincronizarse y conserva
  tipo, entidad, payload, cuenta, fecha, intentos y estado.
- Las operaciones pendientes no se eliminan por antigüedad. Solo desaparecen
  después de confirmación inequívoca del servidor o de una acción informada del
  usuario.
- La retención de copias ya sincronizadas debe definirse antes de implementar;
  puede ser temporal y configurable sin afectar los pendientes.

### Sincronización

- Se intenta al abrir la app, volver a primer plano, detectar conectividad y por
  acción manual. No se promete ejecución continua cuando Android suspende la
  aplicación.
- Reintentos usan espera creciente y no crean duplicados. Cada mutación necesita
  una clave de idempotencia aceptada y persistida por el backend.
- Crear localmente usa identificadores compatibles con el servidor para que las
  relaciones no cambien al sincronizar.
- Los borrados generan tombstones hasta recibir confirmación.
- La interfaz distingue `Pendiente`, `Sincronizando`, `Sincronizado`, `Error` y
  `Conflicto`; nunca muestra un cambio local como confirmado remotamente.

### Conflictos

- Los movimientos financieros nuevos son operaciones únicas; no se resuelven
  sobrescribiendo por fecha.
- Perfil, compromisos y gastos fijos necesitan versión/revisión del servidor y
  control optimista. Una respuesta de conflicto se muestra al usuario.
- El pago de un gasto fijo debe convertirse primero en una operación atómica de
  backend. Actualmente son dos solicitudes y no es seguro reintentarlas como
  una única acción offline.
- Categorías `SYSTEM` son autoridad del servidor y se replican como solo lectura.

### Bóveda

La bóveda requiere una fase de seguridad propia. Guardar secretos offline solo
es aceptable con cifrado local autenticado, una clave protegida por el sistema
operativo y borrado controlado. Hasta completar ese diseño, la bóveda debe
informar que necesita conexión; no se deben copiar secretos a almacenamiento
general ni incluirlos en una outbox sin cifrar.

### Actualizaciones

La comprobación de EAS debe dejar de bloquear cuando el dispositivo no tiene
conectividad. Puede instalar una actualización cuando EAS responde, pero ante
un fallo de red debe abrir el último bundle válido y volver a intentar después.
Eliminar el workflow de GitHub no modifica por sí solo esta compuerta.

## Cambios necesarios

1. Diseñar el esquema local, cifrado, migraciones y limpieza por cuenta.
2. Separar repositorios locales, remotos y sincronizadores detrás de los casos
   de uso existentes.
3. Incorporar outbox, estados visibles y reintentos idempotentes.
4. Añadir al backend claves de idempotencia, revisiones y un pago de gasto fijo
   atómico.
5. Implementar sesión offline segura sin persistir access token ni contraseña.
6. Cambiar `MandatoryUpdateGate` a una política tolerante a fallos de red.
7. Probar apagado durante cada operación, duplicados, conflictos, reinstalación,
   expiración de refresh, cambio de cuenta y pérdida del dispositivo.

El hostname público actual incluye la IP en `sslip.io`. Si el proveedor cambia
esa IP mientras el homelab está apagado, encender el servidor no bastará: será
necesario actualizar el hostname/configuración o migrar antes a dominio propio
con DNS dinámico.
