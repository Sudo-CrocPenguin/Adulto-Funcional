import AsyncStorage from '@react-native-async-storage/async-storage';

const LAST_LOGIN_KEY = 'last_login_date';
const CURRENT_STREAK_KEY = 'current_streak';
const MAX_STREAK_KEY = 'max_streak';

/**
 * Calcula y actualiza la racha global (días consecutivos de inicio de sesión).
 * Se debe llamar cada vez que el usuario inicia sesión o la app se abre autenticada.
 * @returns Objeto con la racha actual y la máxima histórica.
 */
export const updateStreak = async (): Promise<{ current: number; max: number }> => {
  const today = new Date().toISOString().split('T')[0];
  const lastLogin = await AsyncStorage.getItem(LAST_LOGIN_KEY);
  const currentStreak = parseInt(await AsyncStorage.getItem(CURRENT_STREAK_KEY) || '0', 10);
  const maxStreak = parseInt(await AsyncStorage.getItem(MAX_STREAK_KEY) || '0', 10);

  let newCurrent = currentStreak;
  if (lastLogin === today) {
    // Ya actualizó hoy, no hacer nada
    return { current: currentStreak, max: maxStreak };
  } else if (lastLogin === getYesterday()) {
    // Día consecutivo
    newCurrent = currentStreak + 1;
  } else {
    // Se rompió la racha
    newCurrent = 1;
  }

  const newMax = Math.max(newCurrent, maxStreak);

  await AsyncStorage.setItem(LAST_LOGIN_KEY, today);
  await AsyncStorage.setItem(CURRENT_STREAK_KEY, newCurrent.toString());
  await AsyncStorage.setItem(MAX_STREAK_KEY, newMax.toString());

  return { current: newCurrent, max: newMax };
};

/**
 * Obtiene la racha actual sin modificarla.
 */
export const getCurrentStreak = async (): Promise<number> => {
  const streak = await AsyncStorage.getItem(CURRENT_STREAK_KEY);
  return streak ? parseInt(streak, 10) : 0;
};

/**
 * Obtiene la racha máxima histórica.
 */
export const getMaxStreak = async (): Promise<number> => {
  const max = await AsyncStorage.getItem(MAX_STREAK_KEY);
  return max ? parseInt(max, 10) : 0;
};

const getYesterday = (): string => {
  const date = new Date();
  date.setDate(date.getDate() - 1);
  return date.toISOString().split('T')[0];
};
