# Historial de cambios

Los cambios relevantes se documentan siguiendo
[Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/) y versionado
semántico. El historial Git conserva el detalle progresivo de cada condición.

## No publicado

### Cambiado

- La aplicación móvil consume la API pública por HTTPS y ya no requiere
  ZeroTier para funcionar.
- La versión nativa móvil avanza a `0.3.0` (`versionCode`/`buildNumber` 3) y
  deshabilita el tráfico HTTP claro en Android.
- El APK 0.2 existente recibe una actualización OTA compatible que migra su
  conexión a HTTPS mientras se completa el nuevo binario.
- Spring Boot se publica mediante Traefik sin exponer MariaDB ni Redis.

### Seguridad

- El puerto directo de la API queda limitado a loopback; el único ingreso
  público del backend es el proxy TLS por 443.

### Documentación

- Auditoría y corrección integral de documentación end-to-end.
- Guías transversales de ejecución, pruebas, release y contribución.

## 0.2.0 - 2026-08-11

### Añadido

- Primera versión funcional de la aplicación Expo/React Native.
- Autenticación nativa, restauración de sesión y almacenamiento seguro.
- Inicio, compromisos, finanzas, gastos fijos, análisis, bóveda y perfil.
- Tema claro/oscuro y navegación autenticada completa.
- Icono, adaptive icon, splash y favicon oficiales.
- Actualizaciones obligatorias con EAS Update y workflow para `main`.
- Despliegue privado de Spring Boot, MariaDB y Redis en `server1`.
- Conexión móvil con la API mediante ZeroTier.

### Seguridad

- Refresh token rotativo y access token solo en memoria del cliente móvil.
- Secretos de la bóveda visibles temporalmente y nunca incluidos en listados.
- Aislamiento por cuenta y por sesión de Master Key.

### Limitaciones conocidas

- Recuperación de contraseña de acceso no disponible.
- Recuperación de una Master Key olvidada no disponible.
- Cliente web todavía en estado de scaffold.
- Distribución móvil privada por HTTP sobre ZeroTier; una publicación pública o
  iOS de producción requiere HTTPS válido.
- El workflow OTA necesita `EXPO_TOKEN` configurado en GitHub antes de poder
  publicar automáticamente.
- Una prueba de integración del backend usa una fecha fija del 10 de agosto de
  2026 y falla al ejecutarse después de esa fecha.

[0.2.0]: https://github.com/Sudo-CrocPenguin/Adulto-Funcional/tree/v0.2.0
