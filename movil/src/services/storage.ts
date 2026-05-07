/**
 * Servicio de almacenamiento seguro utilizando expo-secure-store.
 * Proporciona métodos para guardar, obtener y eliminar datos sensibles.
 *
 * @author Miguel Angel Blandon Montes
 */

import * as SecureStore from 'expo-secure-store';

export const storage = {
  async setItem(key: string, value: string): Promise<void> {
    await SecureStore.setItemAsync(key, value);
  },

  async getItem(key: string): Promise<string | null> {
    return await SecureStore.getItemAsync(key);
  },

  async deleteItem(key: string): Promise<void> {
    await SecureStore.deleteItemAsync(key);
  },

  async clearAll(): Promise<void> {
    // No hay método nativo para borrar todo, se pueden borrar claves conocidas.
    // Aquí implementamos según necesidades.
  },
};