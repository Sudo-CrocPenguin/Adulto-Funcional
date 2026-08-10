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
});

export const themePalettes = Object.freeze({
  light: Object.freeze({
    ...colors,
    cardMuted: '#F4F6F9',
    divider: '#DCE1E6',
    overlay: 'rgba(17, 24, 39, 0.28)',
    shadow: '#111827',
  }),
  dark: Object.freeze({
    ...colors,
    background: '#0B1220',
    border: '#334155',
    brandSoft: '#16243A',
    cardMuted: '#202B3D',
    divider: '#3A4658',
    errorSoft: '#3B1D24',
    fieldBackground: '#202B3D',
    navigationMuted: '#8996A5',
    overlay: 'rgba(0, 0, 0, 0.58)',
    shadow: '#000000',
    successSoft: '#153529',
    surface: '#172033',
    text: '#F8FAFC',
    textMuted: '#C3CEDB',
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
    isDark: mode === THEME_MODES.dark,
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
