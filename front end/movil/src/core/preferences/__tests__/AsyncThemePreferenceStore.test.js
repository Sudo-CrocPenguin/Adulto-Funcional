jest.mock('@react-native-async-storage/async-storage', () => ({
  __esModule: true,
  default: {
    getItem: jest.fn(),
    setItem: jest.fn(),
  },
}));

import { AsyncThemePreferenceStore } from '../AsyncThemePreferenceStore';

function setup(storedMode = null) {
  const storage = {
    getItem: jest.fn().mockResolvedValue(storedMode),
    setItem: jest.fn().mockResolvedValue(),
  };
  return {
    storage,
    store: new AsyncThemePreferenceStore(storage),
  };
}

describe('AsyncThemePreferenceStore', () => {
  it('recupera únicamente modos compatibles', async () => {
    await expect(setup('dark').store.getMode()).resolves.toBe('dark');
    await expect(setup('desconocido').store.getMode()).resolves.toBeNull();
  });

  it('guarda el modo sin mezclarlo con credenciales seguras', async () => {
    const { storage, store } = setup();

    await store.saveMode('light');

    expect(storage.setItem).toHaveBeenCalledWith(
      'adulto_funcional.theme_mode',
      'light',
    );
  });

  it('rechaza preferencias de tema no soportadas', async () => {
    await expect(setup().store.saveMode('automatic')).rejects.toThrow(
      'no es compatible',
    );
  });
});
