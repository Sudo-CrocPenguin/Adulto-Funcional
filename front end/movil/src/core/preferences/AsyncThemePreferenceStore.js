import AsyncStorage from '@react-native-async-storage/async-storage';

const THEME_KEY = 'adulto_funcional.theme_mode';
const SUPPORTED_MODES = new Set(['light', 'dark', 'neon']);

export class AsyncThemePreferenceStore {
  constructor(storage = AsyncStorage) {
    this.storage = storage;
  }

  async getMode() {
    const storedMode = await this.storage.getItem(THEME_KEY);
    return SUPPORTED_MODES.has(storedMode) ? storedMode : null;
  }

  async saveMode(mode) {
    if (!SUPPORTED_MODES.has(mode)) {
      throw new Error(`El modo de tema ${mode} no es compatible.`);
    }

    await this.storage.setItem(THEME_KEY, mode);
  }
}
