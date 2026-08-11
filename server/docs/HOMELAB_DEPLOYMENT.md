# Despliegue privado en el homelab

## Propósito y alcance

Este documento describe el despliegue cliente-servidor de Adulto Funcional.
Cada entorno tiene una sola responsabilidad:

```text
PC de desarrollo
  ├─ código fuente, pruebas y Metro para Expo Go
  ├─ publica el frontend móvil en Expo/EAS
  └─ despliega el backend en server1

Expo/EAS
  └─ compila y distribuye únicamente el frontend móvil y sus OTA

server1
  └─ ejecuta únicamente API Spring Boot + MariaDB + Redis con Docker

Teléfono
  ├─ obtiene el frontend desde Expo/EAS
  └─ consume la API de server1 mediante ZeroTier
```

El frontend web no forma parte de este despliegue. Expo no aloja el backend ni
la base de datos, y `server1` no ejecuta Metro ni el frontend móvil.

## Estado desplegado

| Recurso | Valor |
|---|---|
| Host SSH | `ssh server1` |
| Ruta remota | `/home/admin1/apps/adulto-funcional/server` |
| Proyecto Compose | `adulto-funcional-prod` |
| URL privada de la API | `http://10.119.54.220:8090` |
| Healthcheck | `http://10.119.54.220:8090/actuator/health` |
| Base de datos | MariaDB 11.8, solo en la red interna de Docker |
| Estado efímero | Redis 7.4, solo en la red interna de Docker |
| Esquema | Flyway V1–V14 |

La base se creó desde cero. No se retuvieron cuentas, movimientos, eventos,
sesiones, contenedores, volúmenes ni respaldos del despliegue anterior. Una
instalación limpia contiene únicamente las 14 categorías globales `SYSTEM`
creadas por Flyway.

## Acceso privado

No se necesita comprar un dominio para esta etapa. El teléfono debe estar
unido a la misma red ZeroTier que `server1` y consumir
`http://10.119.54.220:8090`.

El puerto 8090 no está publicado por la IP pública del homelab. No debe crearse
un port-forward en el router para ese puerto. Aunque la API usa HTTP, el enlace
entre los miembros de la red privada viaja dentro del túnel cifrado de
ZeroTier. Si en el futuro se ofrece acceso público, debe agregarse un dominio,
HTTPS válido y un proxy inverso antes de exponer la API.

## Configuración y secretos

La configuración productiva vive únicamente en:

```text
/home/admin1/apps/adulto-funcional/server/.env
```

El archivo tiene permisos `600` y no se versiona. Contiene contraseñas
independientes para MariaDB y Redis, secretos criptográficos diferentes para
JWT y sesiones de Master Key, el puerto 8090 y los orígenes CORS autorizados.
Nunca se deben copiar estos valores a Expo ni a variables `EXPO_PUBLIC_*`.

Como mínimo, las variables operativas que distinguen este host son:

```dotenv
COMPOSE_PROJECT_NAME=adulto-funcional-prod
SERVER_HOST_ADDRESS=0.0.0.0
SERVER_HOST_PORT=8090
```

`COMPOSE_PROJECT_NAME` es necesario para que los comandos `docker compose`
usen siempre los contenedores y el volumen existentes. Si falta o cambia, Docker
puede crear otro proyecto aparentemente vacío sin borrar el anterior.

## Operación cotidiana

Estado y salud:

```bash
ssh server1
cd "$HOME/apps/adulto-funcional/server"
docker compose ps
curl http://127.0.0.1:8090/actuator/health
```

Logs de la API:

```bash
docker compose logs --tail=200 -f app
```

Recrear la API después de desplegar una versión del backend:

```bash
docker compose up -d --build app
docker compose ps
```

Detener y volver a iniciar sin borrar datos:

```bash
docker compose stop
docker compose start
```

No uses `docker compose down -v` en operación normal: `-v` elimina de forma
irreversible la base de datos.

## Actualizar el backend desde la PC de desarrollo

El despliegue debe partir de un commit o tag conocido y de un árbol limpio.
Desde la raíz del repositorio local:

```bash
git status --short
git rev-parse HEAD
```

`git status --short` debe quedar vacío. Registra el hash en la evidencia de la
entrega y después sincroniza solo la carpeta `server`:

```bash
rsync -az \
  --exclude .git \
  --exclude .env \
  --exclude target \
  server/ server1:/home/admin1/apps/adulto-funcional/server/

ssh server1 \
  'cd "$HOME/apps/adulto-funcional/server" && docker compose up -d --build'
```

El archivo `.env` remoto permanece fuera de la sincronización. Después se debe
validar `docker compose ps`, el healthcheck y un flujo autenticado descartable.
El frontend móvil no se copia al servidor.

`rsync` refleja el árbol local, no un artefacto inmutable. Para una release
formal se recomienda hacer checkout del tag en un directorio limpio antes de
sincronizar. No se debe desplegar desde archivos sin commit.

## Reinicio automático

Docker está habilitado en systemd y los tres servicios usan
`restart: unless-stopped`. Por eso, después de un reinicio de `server1`, Docker
levanta MariaDB, Redis y la API; Compose espera que MariaDB y Redis estén sanos
antes de iniciar Spring Boot.

La configuración comprobada es:

```text
docker.service: enabled
adulto-funcional-prod-app-1: unless-stopped
adulto-funcional-prod-mariadb-1: unless-stopped
adulto-funcional-prod-redis-1: unless-stopped
```

Un reinicio controlado del stack conservó los datos, reaplicó la validación de
Flyway y devolvió los tres contenedores al estado `healthy`.

## Respaldo y recuperación

El borrón inicial no conservó ningún respaldo de la base anterior. A partir de
los datos nuevos, un respaldo manual puede crearse así:

```bash
ssh server1
cd "$HOME/apps/adulto-funcional/server"
mkdir -p "$HOME/backups/adulto-funcional"
chmod 700 "$HOME/backups/adulto-funcional"

backup_file="$HOME/backups/adulto-funcional/$(date -u +%Y%m%dT%H%M%SZ).sql.gz"
docker compose exec -T mariadb sh -c \
  'mariadb-dump -uroot -p"$MARIADB_ROOT_PASSWORD" --single-transaction --routines --triggers "$MARIADB_DATABASE"' \
  | gzip -9 > "$backup_file"
chmod 600 "$backup_file"
sha256sum "$backup_file" > "$backup_file.sha256"
chmod 600 "$backup_file.sha256"
gzip -t "$backup_file"
sha256sum -c "$backup_file.sha256"
```

Los respaldos deben copiarse además a otro equipo; un archivo guardado solo en
el mismo disco del servidor no protege frente a la pérdida de ese disco.

### Estado actual y retención

El procedimiento es manual: no existe todavía un timer ni una restauración
ensayada para los datos nuevos. Antes de usar datos reales de terceros se debe
asignar responsable, almacenamiento externo y una agenda. Referencia inicial:

| Copia | Retención sugerida | Ubicación |
|---|---:|---|
| Diaria | 7 días | `server1` y copia externa |
| Semanal | 4 semanas | equipo o almacenamiento externo |
| Mensual | 6 meses | ubicación externa protegida |

La política definitiva depende de los requisitos legales y capacidad del
homelab. Una tarea automática debe fallar visiblemente cuando el dump,
checksum, copia externa o espacio disponible no puedan verificarse.

### Ensayo de restauración aislada

Nunca ensayes sobre la base productiva. El siguiente procedimiento crea una
base temporal dentro del mismo MariaDB, verifica el historial y la elimina al
terminar:

```bash
ssh server1
cd "$HOME/apps/adulto-funcional/server"
backup_file="$HOME/backups/adulto-funcional/<ARCHIVO>.sql.gz"

gzip -t "$backup_file"
sha256sum -c "$backup_file.sha256"

docker compose exec -T mariadb sh -c \
  'mariadb -uroot -p"$MARIADB_ROOT_PASSWORD" -e \
  "CREATE DATABASE adulto_funcional_restore CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"'

gzip -dc "$backup_file" | docker compose exec -T mariadb sh -c \
  'mariadb -uroot -p"$MARIADB_ROOT_PASSWORD" adulto_funcional_restore'

docker compose exec -T mariadb sh -c \
  'mariadb -uroot -p"$MARIADB_ROOT_PASSWORD" adulto_funcional_restore -e \
  "SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank"'

docker compose exec -T mariadb sh -c \
  'mariadb -uroot -p"$MARIADB_ROOT_PASSWORD" -e \
  "DROP DATABASE adulto_funcional_restore"'
```

Usa un nombre distinto si la base temporal ya existe. El ensayo debe registrar
fecha, archivo, checksum, duración y resultado sin guardar contraseñas en la
evidencia.

## Verificación mínima

Una entrega del backend se considera operativa cuando:

1. `docker compose ps` muestra los tres servicios como `healthy`.
2. `GET /actuator/health` devuelve `{"status":"UP"}` desde `server1` y desde
   un miembro de ZeroTier.
3. Flyway reporta 14 migraciones exitosas y ninguna fallida.
4. Registro nativo, consulta autenticada, refresh y eliminación responden
   `201`, `200`, `200` y `200` respectivamente.
5. La aplicación Expo/EAS usa exactamente `http://10.119.54.220:8090`.
6. El hash desplegado coincide con la evidencia de la entrega.
7. Existe un respaldo verificable y una restauración aislada reciente cuando
   la base contiene datos que no pueden recrearse.
