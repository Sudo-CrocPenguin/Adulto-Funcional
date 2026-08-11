import * as SecureStore from 'expo-secure-store';

const REFRESH_TOKEN_KEY = 'adulto_funcional.refresh_token';

export class SecureSessionStore {
  async saveRefreshToken(refreshToken) {
    if (!refreshToken) {
      throw new Error('La sesión nativa no incluyó un refresh token.');
    }

    await SecureStore.setItemAsync(REFRESH_TOKEN_KEY, refreshToken, {
      keychainAccessible: SecureStore.WHEN_UNLOCKED_THIS_DEVICE_ONLY,
    });
  }

  getRefreshToken() {
    return SecureStore.getItemAsync(REFRESH_TOKEN_KEY);
  }

  clear() {
    return SecureStore.deleteItemAsync(REFRESH_TOKEN_KEY);
  }
}

