/**
 * @file storage.ts
 * @description Servicio de almacenamiento persistente multiplataforma para la aplicación
 *              Adulto Funcional. Abstrae la diferencia entre almacenamiento seguro en
 *              dispositivos móviles (expo-secure-store) y almacenamiento en navegador
 *              (localStorage) para entornos web.
 *
 * @module src/services/storage
 * @author Miguel Angel Blandón Montes
 *
 * @remarks
 * En plataformas móviles (iOS / Android) los valores se guardan cifrados usando
 * `expo-secure-store`, lo que protege datos sensibles como tokens de sesión.
 * En entornos web se utiliza `localStorage` como alternativa compatible.
 *
 * @example
 * import { storage } from '../services/storage';
 *
 * await storage.setItem('token', 'abc123');
 * const token = await storage.getItem('token');
 * await storage.deleteItem('token');
 */

import * as SecureStore from 'expo-secure-store';
import { Platform } from 'react-native';

/**
 * @constant isWeb
 * @description Indica si la aplicación se está ejecutando en un entorno web.
 *              Se usa para seleccionar la estrategia de almacenamiento correcta.
 */
const isWeb = Platform.OS === 'web';

/**
 * @namespace storage
 * @description Objeto de servicio con métodos para leer, escribir y eliminar
 *              valores del almacenamiento persistente de forma multiplataforma.
 *
 * @remarks
 * Todos los métodos son asíncronos para mantener una interfaz uniforme,
 * incluso cuando la operación subyacente (localStorage) es síncrona.
 */
export const storage = {

  /**
   * @method setItem
   * @description Guarda un valor en el almacenamiento asociado a la clave indicada.
   *              En web usa `localStorage`; en móvil usa `SecureStore`.
   *
   * @param {string} key   - Identificador único bajo el cual se almacenará el valor.
   * @param {string} value - Valor a almacenar (debe ser un string).
   * @returns {Promise<void>}
   *
   * @example
   * await storage.setItem('authToken', 'eyJhbGci...');
   */
  async setItem(key: string, value: string): Promise<void> {
    if (isWeb) {
      localStorage.setItem(key, value);
    } else {
      await SecureStore.setItemAsync(key, value);
    }
  },

  /**
   * @method getItem
   * @description Recupera el valor almacenado bajo la clave indicada.
   *              Retorna `null` si la clave no existe.
   *              En web usa `localStorage`; en móvil usa `SecureStore`.
   *
   * @param {string} key - Identificador de la clave a consultar.
   * @returns {Promise<string | null>} El valor almacenado o `null` si no existe.
   *
   * @example
   * const token = await storage.getItem('authToken');
   * if (token) { ... }
   */
  async getItem(key: string): Promise<string | null> {
    if (isWeb) {
      return localStorage.getItem(key);
    }
    return await SecureStore.getItemAsync(key);
  },

  /**
   * @method deleteItem
   * @description Elimina del almacenamiento el valor asociado a la clave indicada.
   *              En web usa `localStorage`; en móvil usa `SecureStore`.
   *
   * @param {string} key - Identificador de la clave a eliminar.
   * @returns {Promise<void>}
   *
   * @example
   * await storage.deleteItem('authToken');
   */
  async deleteItem(key: string): Promise<void> {
    if (isWeb) {
      localStorage.removeItem(key);
    } else {
      await SecureStore.deleteItemAsync(key);
    }
  },
};