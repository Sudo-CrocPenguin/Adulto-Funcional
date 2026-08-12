# Pendientes de Adulto Funcional

Este archivo registra capacidades necesarias que todavía no están
implementadas. Una casilla solo se marca cuando código, pruebas, documentación
y validación end-to-end estén terminados.

## Obligatorios

- [ ] Implementar operación offline-first en la aplicación móvil.
  - Permitir que una cuenta previamente autenticada abra y use la app mientras
    `server1` está apagado.
  - Guardar temporalmente los datos funcionales en almacenamiento local
    cifrado.
  - Encolar operaciones pendientes y sincronizarlas por HTTPS cuando el
    servidor vuelva a estar disponible.
  - Evitar duplicados mediante idempotencia y mostrar estados de sincronización
    y conflictos.
  - Mantener las operaciones sin confirmar hasta que el servidor responda; no
    eliminarlas únicamente por antigüedad.
  - Hacer que la comprobación de EAS permita abrir el último bundle válido ante
    un fallo de conectividad.
  - Resolver de forma específica la seguridad de la bóveda antes de guardar
    secretos localmente.
  - Seguir la política propuesta en
    [Operación offline y sincronización diferida](<front end/movil/docs/OFFLINE_FIRST.md>).

- [ ] Desarrollar la versión web funcional.
  - Sustituir el scaffold actual por las pantallas y flujos reales del producto.
  - Implementar autenticación web mediante cookies HttpOnly, CSRF, CORS exacto
    y HTTPS, sin transportar tokens como cliente móvil.
  - Incorporar perfil, compromisos, finanzas, gastos fijos, dashboard y bóveda
    de acuerdo con los contratos existentes.
  - Crear diseño responsive, estados de carga/error/vacío y accesibilidad.
  - Añadir lockfile, pruebas, validación end-to-end y despliegue documentado.

## Regla de mantenimiento

- Añadir aquí nuevos pendientes confirmados y enlazar su especificación cuando
  exista.
- No usar este archivo para afirmar que una función ya está disponible.
- Retirar o marcar una tarea únicamente después de cumplir el criterio de
  terminación definido en `CONTRIBUTING.md`.
