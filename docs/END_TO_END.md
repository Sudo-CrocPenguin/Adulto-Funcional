# Ejecución end-to-end

Esta guía conecta desarrollo, backend, datos, aplicación móvil y distribución.
Su objetivo es que una instalación pueda reproducirse sin conocer el historial
del proyecto.

## Límites de cada entorno

| Entorno | Responsabilidad | No aloja |
|---|---|---|
| PC de desarrollo | Código, Git, pruebas y Metro | Producción permanente |
| `server1` | Spring Boot, MariaDB y Redis | Metro, Expo y frontend web |
| Expo/EAS | Builds y bundles OTA móviles | API y base de datos |
| Navegador web | Futuro cliente React | Backend ni datos locales de producción |

## 1. Obtener el código

```bash
git clone <URL-DEL-REPOSITORIO>
cd Adulto-Funcional
git switch develop
```

Para una reproducción de producción se debe usar un tag exacto, por ejemplo:

```bash
git switch --detach v0.2.0
```

No copies archivos `.env` entre entornos. Cada entorno conserva sus propios
secretos fuera de Git.

## 2. Levantar el backend local

Requisitos: Docker Engine con Compose v2 y puertos locales disponibles.

```bash
cd server
cp .env.example .env
```

Completa todos los valores vacíos y genera secretos diferentes:

```bash
openssl rand -base64 32
```

Inicia y verifica:

```bash
docker compose up -d --build
docker compose ps
curl http://127.0.0.1:8080/actuator/health
```

El resultado esperado del healthcheck es `{"status":"UP"}`. MariaDB y Redis
no publican puertos al host.

## 3. Conectar la aplicación móvil

Desde la raíz:

```bash
cd "front end/movil"
npm ci
cp .env.example .env
```

Para el servidor desplegado:

```dotenv
EXPO_PUBLIC_API_URL=https://api-adulto-funcional.38-225-48-28.sslip.io
```

El teléfono debe:

1. tener acceso normal a Internet;
2. poder abrir
   `https://api-adulto-funcional.38-225-48-28.sslip.io/actuator/health`;
3. tener Expo Go compatible con SDK 54 para desarrollo;
4. poder alcanzar la PC por LAN o túnel para cargar Metro.

Inicia Expo:

```bash
npm start
```

Si la LAN bloquea Metro:

```bash
npm run start:tunnel
```

Expo Go permite probar la interfaz y la API, pero no reproduce el control OTA
obligatorio de un binario distribuido.

## 4. Flujo mínimo autenticado

1. Registrar una cuenta descartable desde la app nativa.
2. Confirmar que abre Inicio y muestra datos vacíos o reales sin `401`.
3. Cerrar y abrir la app; si la sesión es persistente, debe restaurarse por
   refresh.
4. Crear un compromiso con fecha futura.
5. Crear ingreso y egreso y comprobar el saldo.
6. Crear un gasto fijo y registrar su pago; debe aparecer un egreso financiero
   y avanzar el próximo vencimiento.
7. Configurar Master Key, crear una credencial, bloquear y volver a desbloquear.
8. Editar el perfil y comprobar que la sesión conserva los datos actualizados.
9. Eliminar la cuenta descartable desde seguridad cuando el flujo esté
   habilitado en la pantalla correspondiente o mediante la API.

Los casos completos están en [TEST_MATRIX.md](TEST_MATRIX.md).

## 5. Despliegue del backend

El procedimiento vigente está en
[HOMELAB_DEPLOYMENT.md](../server/docs/HOMELAB_DEPLOYMENT.md). Resumen:

```bash
rsync -az \
  --exclude .git \
  --exclude .env \
  --exclude target \
  server/ server1:/home/admin1/apps/adulto-funcional/server/

ssh server1 \
  'cd "$HOME/apps/adulto-funcional/server" && docker compose -f docker-compose.yml -f docker-compose.coolify.yml up -d --build'
```

El `.env` remoto debe incluir `COMPOSE_PROJECT_NAME=adulto-funcional-prod` y
permanece fuera de la sincronización. Un despliegue formal debe originarse en
un commit o tag conocido, no en cambios locales sin registrar.

## 6. Build y actualización móvil

Una instalación inicial necesita un binario:

```bash
cd "front end/movil"
npx eas-cli build --platform android --profile production-apk
```

Una actualización JavaScript compatible puede publicarse así:

```bash
npx eas-cli update \
  --channel production \
  --environment production \
  --message "descripcion verificable"
```

Los cambios nativos, permisos o dependencias nativas requieren incrementar la
versión y crear otro binario. Consulta
[ACTUALIZACIONES.md](<../front end/movil/docs/ACTUALIZACIONES.md>).

## 7. Criterio de entrega

La entrega está completa únicamente cuando:

- todas las validaciones aplicables de
  [RELEASE_CHECKLIST.md](RELEASE_CHECKLIST.md) están aprobadas;
- la API y sus tres contenedores están saludables;
- el teléfono alcanza la API pública por HTTPS sin ZeroTier;
- el flujo autenticado mínimo fue comprobado con datos descartables;
- existe un binario instalable compatible con el runtime publicado;
- cualquier publicación OTA manual quedó registrada y verificada;
- se registró la versión en `CHANGELOG.md` y mediante un tag.

## Diagnóstico rápido

| Síntoma | Comprobación |
|---|---|
| La app no conecta | DNS, certificado, healthcheck y `EXPO_PUBLIC_API_URL` |
| Registro funciona en móvil pero no en web Expo | El navegador usa transporte cookie/CSRF; Expo web es revisión visual |
| La app queda en actualización | Conectividad con EAS, canal/runtime y última publicación |
| El backend no inicia | `docker compose ps`, logs, secretos y migraciones Flyway |
| `401` después de reiniciar | Refresh almacenado, rotación y reloj del dispositivo |
| El pago aparece duplicado | No reintentar tras respuesta incierta; revisar movimientos antes de repetir |
