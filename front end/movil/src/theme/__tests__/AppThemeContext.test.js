import { THEME_MODES, themePalettes } from '../AppThemeContext';

function channel(value) {
  const normalized = value / 255;
  return normalized <= 0.03928
    ? normalized / 12.92
    : ((normalized + 0.055) / 1.055) ** 2.4;
}

function luminance(hex) {
  const value = hex.replace('#', '');
  const channels = [0, 2, 4].map((offset) => (
    channel(Number.parseInt(value.slice(offset, offset + 2), 16))
  ));
  return channels[0] * 0.2126 + channels[1] * 0.7152 + channels[2] * 0.0722;
}

function contrast(left, right) {
  const brightest = Math.max(luminance(left), luminance(right));
  const darkest = Math.min(luminance(left), luminance(right));
  return (brightest + 0.05) / (darkest + 0.05);
}

describe('tema Neón', () => {
  it('se registra como tercera opción sin reemplazar Claro ni Oscuro', () => {
    expect(THEME_MODES).toEqual({
      dark: 'dark',
      light: 'light',
      neon: 'neon',
    });
    expect(Object.keys(themePalettes)).toEqual(['light', 'dark', 'neon']);
    expect(Object.keys(themePalettes.light).sort()).toEqual(
      Object.keys(themePalettes.dark).sort(),
    );
    expect(Object.keys(themePalettes.light).sort()).toEqual(
      Object.keys(themePalettes.neon).sort(),
    );
  });

  it('incluye acentos neón distintos y una serie completa para gráficos', () => {
    const palette = themePalettes.neon;

    expect(new Set([
      palette.brandDeep,
      palette.brandSecondary,
      palette.error,
      palette.success,
      palette.warning,
    ]).size).toBe(5);
    expect(palette.chartColors).toHaveLength(10);
    expect(palette.glowOpacity).toBeGreaterThan(0);
  });

  it('mantiene contraste AA en texto, superficies y acciones principales', () => {
    const palette = themePalettes.neon;

    expect(contrast(palette.text, palette.background)).toBeGreaterThanOrEqual(4.5);
    expect(contrast(palette.text, palette.surface)).toBeGreaterThanOrEqual(4.5);
    expect(contrast(palette.surfaceOnBrand, palette.brand)).toBeGreaterThanOrEqual(4.5);
    expect(contrast(palette.surfaceOnBrand, palette.brandDeep)).toBeGreaterThanOrEqual(4.5);
    expect(contrast(palette.surfaceOnBrand, palette.brandSecondary)).toBeGreaterThanOrEqual(4.5);
    expect(contrast(palette.surfaceOnBrand, palette.error)).toBeGreaterThanOrEqual(4.5);
  });
});
