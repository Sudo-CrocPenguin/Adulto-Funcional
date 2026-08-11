# Identidad visual móvil

## Qué es y para qué sirve

La aplicación usa como marca el símbolo de una persona protegida por un escudo
y un candado. La composición comunica el propósito de Adulto Funcional:
organización personal con control, privacidad y seguridad.

El activo maestro está en:

```text
assets/adulto-funcional-logo-master.png
```

Fue preparado a partir del logo suministrado para esta aplicación. Conserva la
silueta gris, los hombros azules, el escudo verde con bordes blancos y dorados,
y el candado dorado. No contiene texto para conservar legibilidad en tamaños
pequeños.

## Cómo lo consume Expo

| Archivo | Uso | Tamaño |
|---|---|---:|
| `assets/icon.png` | Icono general y logo del encabezado de autenticación | 1024 × 1024 |
| `assets/adaptive-icon.png` | Icono adaptable de Android | 1024 × 1024 |
| `assets/splash-icon.png` | Imagen de arranque | 1024 × 1024 |
| `assets/favicon.png` | Identificador auxiliar para el modo web de desarrollo | 48 × 48 |

Todos derivan del mismo maestro para evitar diferencias de color o proporción.
El símbolo permanece dentro de la zona segura central, de modo que las máscaras
circulares, cuadradas o redondeadas de Android no corten la cabeza, los hombros
ni el escudo.

## Reglas de mantenimiento

- No añadas texto al icono.
- Conserva el fondo blanco y los colores oficiales.
- No apliques manualmente una máscara redonda: Android y iOS aplican la suya.
- Mantén el maestro cuadrado y con margen equilibrado.
- Después de cambiar el icono, incrementa la versión nativa y genera otro
  build. Una OTA de EAS no sustituye el icono instalado por el sistema.
- Ejecuta `npx expo-doctor` y revisa `npx expo config --type public` antes de
  publicar el nuevo binario.
