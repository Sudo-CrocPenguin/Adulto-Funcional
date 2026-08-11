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

Desde la raíz del repositorio local se sincroniza solo la carpeta `server`:

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

## Respaldos futuros

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

## Verificación mínima

Una entrega del backend se considera operativa cuando:

1. `docker compose ps` muestra los tres servicios como `healthy`.
2. `GET /actuator/health` devuelve `{"status":"UP"}` desde `server1` y desde
   un miembro de ZeroTier.
3. Flyway reporta 14 migraciones exitosas y ninguna fallida.
4. Registro nativo, consulta autenticada, refresh y eliminación responden
   `201`, `200`, `200` y `200` respectivamente.
5. La aplicación Expo/EAS usa exactamente `http://10.119.54.220:8090`.

