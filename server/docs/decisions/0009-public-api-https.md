# ADR 0009: API pública mediante proxy HTTPS

- Estado: aceptada
- Implementación: completa
- Fecha: 2026-08-11
- Alcance: despliegue, transporte móvil y límites de red

## Contexto

La versión 0.2.0 conectaba el teléfono con `server1` mediante HTTP dentro de
ZeroTier. Esa topología protegía el transporte, pero exigía instalar, autorizar
y mantener activa una VPN adicional. Una instalación móvil no funcionaba por
sí sola desde Internet.

`server1` ya ejecuta Traefik mediante Coolify, el puerto público 443 alcanza el
proxy y MariaDB y Redis viven únicamente en redes Docker internas. El puerto
80 no es alcanzable desde Internet, por lo que el desafío ACME HTTP no sirve
para emitir el certificado de esta API.

## Decisión

La API se publica exclusivamente detrás de Traefik con estas reglas:

1. El hostname se configura en `API_PUBLIC_HOST`; no se incrusta en el
   contenedor de Spring Boot.
2. Traefik termina TLS en el entrypoint `https`, obtiene un certificado de
   Let's Encrypt mediante el desafío TLS-ALPN por el puerto 443 y reenvía al
   puerto 8080 de la aplicación dentro de la red Docker `coolify`.
3. El puerto directo de Spring Boot se enlaza a `127.0.0.1` para diagnóstico
   local. No se publica por la interfaz LAN, ZeroTier ni Internet.
4. MariaDB y Redis permanecen sin puertos del host y no se conectan a la red
   del proxy.
5. El móvil productivo usa `https://<API_PUBLIC_HOST>` y Android declara
   `usesCleartextTraffic=false`.
6. ZeroTier deja de ser un requisito del cliente. Puede conservarse como canal
   administrativo del servidor, pero no participa en el contrato de la app.

El hostname inicial es
`api-adulto-funcional.38-225-48-28.sslip.io`, que resuelve la IP pública dentro
del propio nombre. Es una solución sin compra de dominio. Si cambia la IP
pública, se debe actualizar `API_PUBLIC_HOST`, la variable EAS
`EXPO_PUBLIC_API_URL` y generar/publicar la entrega móvil correspondiente. Un
dominio propio con DNS dinámico puede sustituirlo sin cambiar el código.

## Seguridad y operación

- Spring Boot conserva `APP_COOKIE_SECURE=true`; HSTS y los headers de
  seguridad siguen activos sobre HTTPS.
- El proxy no altera `X-Client-Type`, `User-Agent`, `Origin`, `Referer`,
  `Authorization`, CSRF ni `X-Trace-Id`.
- CORS continúa usando orígenes web exactos. La aplicación nativa no depende
  de CORS porque no envía un origen de navegador.
- El healthcheck público permite comprobar disponibilidad, pero no expone
  detalles de MariaDB ni Redis.
- La renovación ACME depende de que el puerto 443 siga llegando a Traefik.

## Consecuencias

Una instalación móvil funciona con cualquier conexión a Internet sin una VPN
adicional. A cambio, la API entra en la superficie pública y exige mantener TLS,
proxy, actualizaciones, límites de autenticación, observabilidad y respaldo.
La dependencia temporal de `sslip.io` y de una IP pública potencialmente
dinámica queda explícita hasta adoptar un dominio administrado.
