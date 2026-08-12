# Datos y privacidad

Este documento describe técnicamente qué datos usa Adulto Funcional y dónde se
almacenan. No sustituye una política de privacidad legal ni concede una licencia
de distribución.

## Datos tratados

| Grupo | Ejemplos | Persistencia principal |
|---|---|---|
| Cuenta | nombres, apellidos, correo y teléfono | MariaDB en `server1` |
| Autenticación | hash de contraseña, sesiones y refresh tokens hasheados | MariaDB |
| Finanzas | movimientos, montos, categorías y gastos fijos | MariaDB |
| Agenda | títulos, descripciones, fechas y recordatorios | MariaDB |
| Bóveda | nombre de aplicación y secreto cifrado | MariaDB |
| Master Key | verificador y material derivado; nunca texto plano | MariaDB y sesión temporal Redis |
| Sesión móvil | refresh token persistente opcional | almacenamiento seguro del dispositivo |
| Preferencias | tema claro u oscuro | almacenamiento general del dispositivo |

El access token móvil vive únicamente en memoria. El frontend no debe guardar
Master Key, contraseñas descifradas ni access tokens en AsyncStorage, archivos,
analítica o logs.

## Cifrado y transporte

- Las contraseñas de acceso se almacenan con Argon2.
- Las credenciales de la bóveda se cifran con AES-256-GCM y material derivado
  de la Master Key.
- Redis conserva únicamente estado temporal de desbloqueo por cuenta y sesión.
- La aplicación transmite datos a la API pública mediante HTTPS con certificado
  válido terminado por Traefik.
- MariaDB y Redis permanecen en redes Docker internas y no aceptan conexiones
  desde Internet.

## Acceso y aislamiento

La cuenta autenticada se obtiene del principal del servidor. Los repositorios
acotan recursos privados por `accountId`; un identificador ajeno se responde
como no encontrado. Las categorías globales `SYSTEM` son visibles para todos,
pero no contienen información personal.

## Eliminación

Eliminar una cuenta borra la raíz y las claves foráneas eliminan los recursos
dependientes. También se limpia el estado temporal de Master Key. Los respaldos
externos, cuando existan, pueden conservar datos hasta cumplir su retención;
esa retención todavía debe formalizarse antes de usar datos reales de terceros.

## Logs y soporte

Los logs pueden conservar identificadores técnicos y `traceId`, pero no deben
incluir:

- contraseñas de acceso o Master Key;
- access/refresh tokens;
- cookies o cabeceras de autorización;
- secretos descifrados de la bóveda;
- contenido completo de solicitudes sensibles.

Un reporte de soporte debe usar `traceId`, fecha y operación, no copiar secretos
ni archivos `.env`.

## Pendientes para distribución pública

Antes de distribuir la aplicación fuera del entorno privado se deben definir:

- responsable legal y datos de contacto;
- finalidad y base jurídica de cada tratamiento;
- términos, retención, exportación y eliminación;
- proveedores externos y transferencias aplicables;
- procedimiento de incidentes y solicitudes del titular;
- política publicada y aceptada en las tiendas correspondientes.
