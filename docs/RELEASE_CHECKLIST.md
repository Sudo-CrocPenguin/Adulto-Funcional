# Checklist de release

Esta lista se aplica antes de fusionar una rama `release/*` en `main` y después
del despliegue. Un punto no aplicable debe justificarse en las notas de la
versión; no se marca como completado por defecto.

## Código e historial

- [ ] La release nació de `develop` y contiene únicamente cambios previstos.
- [ ] `main` y `develop` recibirán la misma corrección final.
- [ ] No hay cambios locales sin registrar.
- [ ] `git diff --check` termina sin errores.
- [ ] Los commits son progresivos, convencionales y comprensibles.
- [ ] La versión coincide en tag, changelog y configuración móvil aplicable.

## Documentación

- [ ] README raíz, frontend, móvil y servidor reflejan el estado real.
- [ ] API, errores, arquitectura y base de datos coinciden con el código.
- [ ] Las ADR nuevas están aceptadas o las propuestas se identifican como tal.
- [ ] Los ejemplos con fechas siguen siendo válidos.
- [ ] No existen enlaces locales rotos ni bloques Markdown sin cerrar.
- [ ] Limitaciones y operaciones no implementadas están declaradas.
- [ ] `CHANGELOG.md` contiene la versión y su fecha.

## Backend

- [ ] `./mvnw clean verify` finaliza con cero fallos.
- [ ] El SCA se ejecutó sobre el commit que será desplegado.
- [ ] Flyway valida todas las migraciones y no usa `ddl-auto=update`.
- [ ] No se modificaron migraciones ya publicadas.
- [ ] CORS, cookies, límites y cron están definidos para el entorno.
- [ ] Los secretos son distintos, suficientes y no están en Git.

## Móvil

- [ ] `npm ci` utiliza el lockfile registrado.
- [ ] Las pruebas Jest pasan.
- [ ] Expo Doctor termina sin incompatibilidades.
- [ ] `EXPO_PUBLIC_API_URL` apunta al servidor correcto en EAS.
- [ ] Cambios nativos incrementaron `expo.version`, `versionCode` y
      `buildNumber` según corresponda.
- [ ] El runtime del binario acepta el update que se publicará.
- [ ] Existe un APK/AAB/IPA instalable y su URL o identificador quedó registrado.

## EAS Update

- [ ] La sesión/token de EAS se proporcionó solo al proceso manual autorizado.
- [ ] La publicación usó `--environment production`.
- [ ] ID, runtime, canal y resultado de la publicación quedaron registrados.
- [ ] El canal `production` apunta al runtime esperado.
- [ ] Un dispositivo real descargó, reinició y abrió la versión nueva.
- [ ] Existe un procedimiento de rollback probado para el canal.

## Despliegue en `server1`

- [ ] El despliegue proviene de un commit/tag registrado.
- [ ] El `.env` remoto conserva permisos `600`.
- [ ] `COMPOSE_PROJECT_NAME=adulto-funcional-prod` está definido.
- [ ] MariaDB y Redis no publican puertos al host.
- [ ] Los tres servicios muestran `healthy`.
- [ ] Docker inicia automáticamente y los servicios usan `unless-stopped`.
- [ ] El healthcheck responde desde el host y públicamente por HTTPS.
- [ ] El puerto directo de Spring Boot está limitado a loopback.
- [ ] MariaDB y Redis continúan sin puertos publicados.
- [ ] Registro, refresh, consulta y eliminación con una cuenta descartable pasan.

## Datos y recuperación

- [ ] Existe un respaldo nuevo, cifrado o protegido y con checksum válido.
- [ ] El respaldo fue copiado fuera del disco de `server1`.
- [ ] Se documentó su retención y responsable.
- [ ] La restauración fue ensayada en un entorno aislado.
- [ ] Los datos descartables de validación fueron eliminados.

## Distribución pública

Estos puntos no son opcionales para tiendas o exposición fuera del homelab:

- [ ] HTTPS con certificado válido y sin `usesCleartextTraffic` innecesario.
- [ ] Dominio y política de rotación/renovación del certificado.
- [ ] Política de privacidad y términos aprobados por el responsable legal.
- [ ] Licencias definidas para móvil y web.
- [ ] Canal de soporte y procedimiento de eliminación/exportación de datos.
- [ ] Revisión de permisos, telemetría, retención y dependencias de terceros.

## Bloqueos conocidos al preparar 0.3.0

Estado auditado el 11 de agosto de 2026:

- El workflow OTA de 0.2.0 falló por `EXPO_TOKEN` ausente y posteriormente fue
  eliminado; la publicación actual es manual.
- `./mvnw clean verify` tenía una prueba fallida por una fecha absoluta vencida.
- El APK 0.3.0 terminó, su integridad/configuración fueron verificadas y existe
  un enlace instalable; falta recorrer la aceptación completa en un dispositivo
  físico.
- Los respaldos nuevos solo contaban con procedimiento manual, sin agenda ni
  restauración ensayada.

La versión no debe considerarse una distribución pública terminada mientras
estos bloqueos sigan vigentes.
