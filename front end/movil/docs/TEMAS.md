# Temas visuales

## Opciones disponibles

Adulto Funcional ofrece tres temas persistentes:

| Tema | Propósito |
|---|---|
| Claro | Lectura luminosa y neutral para uso diurno |
| Oscuro | Superficies azul pizarra con contraste moderado |
| Neón | Experiencia futurista oscura con acentos vibrantes y brillo controlado |

El selector está en el engranaje del encabezado autenticado. La elección se
guarda en `AsyncStorage` bajo `adulto_funcional.theme_mode`; es una preferencia
visual no sensible y no se mezcla con tokens o credenciales.

## Tema Neón

Neón toma referencias generales de interfaces cyberpunk y circuitos de luz sin
cambiar la identidad, distribución ni patrones de interacción de la
aplicación. La jerarquía existente se conserva: encabezado, tarjetas,
formularios, navegación inferior y botones mantienen tamaños, posiciones y
comportamientos.

### Paleta principal

| Rol | Color | Uso |
|---|---|---|
| Fondo | `#03040A` | lienzo negro azulado |
| Superficie | `#0A0C17` | tarjetas y hojas modales |
| Cian eléctrico | `#00E5FF` | navegación, guía y énfasis primario |
| Magenta | `#FF2BD6` | acciones principales y brillo |
| Violeta | `#B66CFF` | identidad y acciones secundarias |
| Verde | `#39FF88` | estados correctos y datos positivos |
| Rojo | `#FF466D` | errores y acciones destructivas |
| Amarillo ácido | `#F8FF65` | alertas, rachas y prioridad media |
| Texto | `#F7F4FF` | contenido principal |
| Texto secundario | `#C8BDE0` | descripciones y metadatos |

Los textos sobre cian, magenta, violeta y rojo usan `#03040A` para conservar
contraste. Las pruebas automatizadas verifican un mínimo AA de 4.5:1 en texto,
superficies y acciones principales.

### Brillo y color semántico

- El brillo magenta aparece únicamente en encabezado, navegación, opción
  activa y controles destacados.
- Las superficies siguen siendo oscuras y sólidas; no se añaden fondos
  animados ni texturas que compitan con los datos.
- Verde, rojo y amarillo conservan significado funcional. No se usan solo como
  decoración.
- Compromisos, fortaleza de contraseñas, actividad y gráficos consumen la
  paleta activa en lugar de colores claros fijos.
- Los 20 gráficos financieros reciben una serie de diez acentos neón para
  mantener categorías diferenciables sobre fondos oscuros.

## Implementación

`AppThemeContext` expone `mode`, `palette`, `isDark`, `isNeon` y `selectMode`.
Neón se considera oscuro para la barra de estado. Las paletas comparten el
mismo contrato, de modo que cada componente consume roles semánticos y no
necesita conocer colores concretos.

```text
ThemeSettingsSheet
  -> selectMode('neon')
    -> AppThemeContext
      -> themePalettes.neon
      -> AsyncThemePreferenceStore.saveMode('neon')
```

No se agregaron dependencias nativas ni se modificó el layout. El tema es
compatible con Expo Go, Android y web dentro de Expo SDK 54.

## Comprobación

```bash
npm test -- --runInBand
npx expo-doctor
npx expo export --platform web
npx expo export --platform android
```
