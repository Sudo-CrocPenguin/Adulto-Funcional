import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from 'react';

import { colors } from '../shared/theme/tokens';

const AppThemeContext = createContext(null);

export const THEME_MODES = Object.freeze({
  dark: 'dark',
  light: 'light',
  neon: 'neon',
});

const DEFAULT_CHART_COLORS = Object.freeze([
  '#27C2C8',
  '#3EAEC9',
  '#308EBA',
  '#315D96',
  '#77C66E',
  '#F4A93D',
  '#F16D75',
  '#8B6FC7',
  '#5B8DEF',
  '#D58BBD',
]);

const DEFAULT_STATISTICS_COLORS = Object.freeze([
  '#66D6D8',
  '#43B3CF',
  '#3296BE',
  '#35669D',
]);

const NEON_CHART_COLORS = Object.freeze([
  '#00E5FF',
  '#39FF88',
  '#FF2BD6',
  '#B66CFF',
  '#FF466D',
  '#F8FF65',
  '#00FFB3',
  '#8A5CFF',
  '#FF6EC7',
  '#38BDF8',
]);

export const themePalettes = Object.freeze({
  light: Object.freeze({
    ...colors,
    cardMuted: '#F4F6F9',
    chartColors: DEFAULT_CHART_COLORS,
    divider: '#DCE1E6',
    glow: '#35598D',
    glowOpacity: 0,
    isNeon: false,
    overlay: 'rgba(17, 24, 39, 0.28)',
    shadow: '#111827',
    statisticsColors: DEFAULT_STATISTICS_COLORS,
    surfaceOnBrand: '#FFFFFF',
    warningSoft: '#FFF3CD',
    warningText: '#9A6B00',
  }),
  dark: Object.freeze({
    ...colors,
    background: '#0B1220',
    border: '#334155',
    brandSoft: '#16243A',
    cardMuted: '#202B3D',
    chartColors: DEFAULT_CHART_COLORS,
    divider: '#3A4658',
    errorSoft: '#3B1D24',
    fieldBackground: '#202B3D',
    glow: '#416DAA',
    glowOpacity: 0,
    isNeon: false,
    navigationMuted: '#8996A5',
    overlay: 'rgba(0, 0, 0, 0.58)',
    shadow: '#000000',
    statisticsColors: DEFAULT_STATISTICS_COLORS,
    successSoft: '#153529',
    surface: '#172033',
    surfaceOnBrand: '#FFFFFF',
    text: '#F8FAFC',
    textMuted: '#C3CEDB',
    warningSoft: '#3A2C10',
    warningText: '#F4C56A',
  }),
  neon: Object.freeze({
    ...colors,
    avatar: '#25133C',
    background: '#03040A',
    border: '#4C2A70',
    brand: '#B66CFF',
    brandDeep: '#00E5FF',
    brandPressed: '#C026D3',
    brandSecondary: '#FF2BD6',
    brandSoft: '#130B2A',
    cardMuted: '#151129',
    chartColors: NEON_CHART_COLORS,
    divider: '#512675',
    error: '#FF466D',
    errorSoft: '#310815',
    fieldBackground: '#101020',
    glow: '#FF2BD6',
    glowOpacity: 0.42,
    isNeon: true,
    link: '#00E5FF',
    lock: '#FF2BD6',
    navigationMuted: '#9B8FAB',
    overlay: 'rgba(1, 1, 8, 0.82)',
    shadow: '#FF2BD6',
    shield: '#00E5FF',
    success: '#39FF88',
    successSoft: '#08261A',
    statisticsColors: Object.freeze(NEON_CHART_COLORS.slice(0, 4)),
    surface: '#0A0C17',
    surfaceOnBrand: '#03040A',
    text: '#F7F4FF',
    textMuted: '#C8BDE0',
    warning: '#F8FF65',
    warningSoft: '#292B08',
    warningText: '#F8FF65',
  }),
});

export function AppThemeProvider({ children, preferenceStore }) {
  const [mode, setMode] = useState(THEME_MODES.light);

  useEffect(() => {
    let mounted = true;

    preferenceStore.getMode()
      .then((storedMode) => {
        if (mounted && storedMode) {
          setMode(storedMode);
        }
      })
      .catch(() => undefined);

    return () => {
      mounted = false;
    };
  }, [preferenceStore]);

  const selectMode = useCallback((nextMode) => {
    if (!themePalettes[nextMode]) {
      return;
    }

    setMode(nextMode);
    preferenceStore.saveMode(nextMode).catch(() => undefined);
  }, [preferenceStore]);

  const value = useMemo(() => ({
    isDark: mode !== THEME_MODES.light,
    isNeon: mode === THEME_MODES.neon,
    mode,
    palette: themePalettes[mode],
    selectMode,
  }), [mode, selectMode]);

  return (
    <AppThemeContext.Provider value={value}>
      {children}
    </AppThemeContext.Provider>
  );
}

export function useAppTheme() {
  const context = useContext(AppThemeContext);

  if (!context) {
    throw new Error('El tema de la aplicación no está disponible.');
  }

  return context;
}
