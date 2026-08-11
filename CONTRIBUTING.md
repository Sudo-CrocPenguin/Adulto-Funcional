# Contribuir a Adulto Funcional

Esta guía define el flujo de ramas, commits, revisión y entrega para los tres
proyectos del repositorio. Las rutas `server`, `front end/movil` y
`front end/web` comparten el mismo historial Git.

## Principios de trabajo

1. Comprender el contrato y la documentación del módulo antes de editarlo.
2. Crear una rama temporal desde `develop`.
3. Mantener arquitectura modular, dominio explícito y orientación a objetos
   cuando aporte encapsulación real.
4. Probar cada condición terminada antes de registrarla.
5. Hacer commits progresivos, específicos y revisables.
6. Actualizar documentación, ejemplos y pruebas junto con el comportamiento.
7. No ejecutar `git push` salvo solicitud expresa del propietario.

## GitFlow

### Ramas permanentes

| Rama | Propósito |
|---|---|
| `main` | Producción estable, etiquetada y desplegable |
| `develop` | Integración de la siguiente versión |

### Ramas temporales

| Patrón | Nace de | Regresa a | Uso |
|---|---|---|---|
| `feature/*` | `develop` | `develop` | Funcionalidad nueva |
| `bugfix/*` | `develop` | `develop` | Error encontrado antes del release |
| `refactor/*` | `develop` | `develop` | Reestructuración sin cambiar comportamiento |
| `chore/*` | `develop` | `develop` | Dependencias, herramientas y mantenimiento |
| `docs/*` | `develop` | `develop` | Documentación exclusiva |
| `release/*` | `develop` | `main` y `develop` | Estabilización de una versión |
| `hotfix/*` | `main` | `main` y `develop` | Falla crítica en producción |
| `experimental/*` o `poc/*` | `develop` | Opcional | Prueba de concepto descartable |

Después de publicar una release o un hotfix, `main` debe fusionarse nuevamente
en `develop` para evitar divergencias. Cada release estable recibe una etiqueta
SemVer, por ejemplo `v0.2.0`.

## Commits convencionales

Los mensajes se escriben en español después del tipo predominante:

```text
<tipo>: <acción concreta en presente>
```

| Tipo | Cuándo usarlo |
|---|---|
| `feat` | Capacidad nueva visible para usuario o consumidor |
| `fix` | Comportamiento defectuoso corregido |
| `refactor` | Mejora interna sin alterar el contrato |
| `chore` | Dependencias, configuración y mantenimiento |
| `docs` | Solo documentación |
| `style` | Formato del código, no cambios visuales de interfaz |
| `perf` | Rendimiento o consumo de recursos |
| `test` | Pruebas nuevas o corregidas |
| `ci` | Automatización de integración o despliegue |

Ejemplos:

```text
feat: permite registrar pagos de gastos fijos
fix: conserva la sesion durante un fallo temporal de red
docs: documenta el despliegue privado por ZeroTier
test: cubre la rotacion concurrente del refresh token
```

No se agrupan cambios independientes en un único commit. Cada commit debe
representar una condición terminada, compilar por sí mismo cuando corresponda y
explicar por qué existe al revisar el historial.

## Criterio de terminación

Antes de integrar una rama:

- el código relevante compila;
- las pruebas del módulo pasan;
- `git diff --check` no reporta errores;
- no se agregaron secretos, tokens, archivos `.env` ni datos personales;
- la documentación refleja rutas, variables, limitaciones y despliegue reales;
- los ejemplos con fechas siguen siendo válidos o declaran su fecha de
  referencia;
- los enlaces locales funcionan;
- se verificó el flujo de aceptación afectado.

Usa [docs/TEST_MATRIX.md](docs/TEST_MATRIX.md) y
[docs/RELEASE_CHECKLIST.md](docs/RELEASE_CHECKLIST.md) para la revisión final.

## Cambios en contratos

Un cambio transversal de seguridad, ownership, tiempo, dinero o respuesta HTTP
requiere una decisión en `server/docs/decisions`. Una ADR aceptada no se
reescribe para ocultar el pasado: una ADR posterior la sustituye y explica la
migración.

Cuando cambia un endpoint, deben actualizarse en el mismo trabajo:

- controlador y DTO;
- referencia API y contrato de errores;
- repositorio/adaptador móvil o web;
- pruebas del backend y del cliente;
- ejemplos y limitaciones conocidas.

## Dependencias

- Móvil: usar `npx expo install` para dependencias nativas compatibles con el
  SDK; no ejecutar `npm audit fix --force`.
- Backend: usar Maven Wrapper (`./mvnw`).
- Web: conservar el lockfile cuando se formalice su instalación reproducible.

Las vulnerabilidades no se descartan solo porque sean transitivas. Deben
evaluarse según alcance, versión compatible y superficie realmente ejecutada.
