import { Platform } from 'react-native';
import * as SecureStore from 'expo-secure-store';

const TOKEN_KEY = 'adulto-funcional.jwt';
const API_BASE_URL_KEY = 'adulto-funcional.api-base-url';
const DEFAULT_API_BASE_URL = 'http://localhost:8080';

class SecureSessionStorage {
  async getToken(): Promise<string | null> {
    return this.getItem(TOKEN_KEY);
  }

  async saveToken(token: string): Promise<void> {
    await this.setItem(TOKEN_KEY, token);
  }

  async clearToken(): Promise<void> {
    await this.deleteItem(TOKEN_KEY);
  }

  async getApiBaseUrl(): Promise<string> {
    return (await this.getItem(API_BASE_URL_KEY)) ?? DEFAULT_API_BASE_URL;
  }

  async saveApiBaseUrl(apiBaseUrl: string): Promise<void> {
    await this.setItem(API_BASE_URL_KEY, apiBaseUrl.replace(/\/$/, ''));
  }

  private async getItem(key: string): Promise<string | null> {
    if (Platform.OS === 'web') {
      return globalThis.localStorage?.getItem(key) ?? null;
    }

    return SecureStore.getItemAsync(key);
  }

  private async setItem(key: string, value: string): Promise<void> {
    if (Platform.OS === 'web') {
      globalThis.localStorage?.setItem(key, value);
      return;
    }

    await SecureStore.setItemAsync(key, value, {
      keychainAccessible: SecureStore.WHEN_UNLOCKED_THIS_DEVICE_ONLY,
    });
  }

  private async deleteItem(key: string): Promise<void> {
    if (Platform.OS === 'web') {
      globalThis.localStorage?.removeItem(key);
      return;
    }

    await SecureStore.deleteItemAsync(key);
  }
}

export const secureSessionStorage = new SecureSessionStorage();
