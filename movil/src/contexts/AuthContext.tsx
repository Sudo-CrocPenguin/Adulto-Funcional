/**
 * @file AuthContext.tsx
 * @description Contexto global de autenticación para la aplicación Adulto Funcional.
 *              Provee el estado de sesión del usuario y las acciones de autenticación
 *              (login, registro, logout y verificación de sesión) a toda la aplicación
 *              mediante la API de Context de React.
 *
 * @module src/contexts/AuthContext
 * @author Miguel Angel Blandón Montes
 *
 * @remarks
 * Este módulo expone dos elementos principales:
 * - {@link AuthProvider}: componente que envuelve la aplicación y gestiona el estado.
 * - {@link useAuth}: hook personalizado para consumir el contexto desde cualquier pantalla.
 *
 * Al montarse, `AuthProvider` verifica automáticamente si existe una sesión activa
 * mediante {@link checkAuthStatus}, evitando que el usuario tenga que iniciar sesión
 * cada vez que abre la aplicación.
 *
 * @example
 * // Envolver la app con el proveedor (normalmente en el layout raíz):
 * <AuthProvider>
 *   <App />
 * </AuthProvider>
 *
 * // Consumir el contexto desde cualquier componente hijo:
 * const { user, login, logout, isAuthenticated } = useAuth();
 */

import React, { createContext, ReactNode, useContext, useEffect, useState } from 'react';
import * as authApi from '../api/authApi';
import { AuthResponse, LoginRequest, RegisterRequest } from '../types/auth.types';

/**
 * @interface AuthContextType
 * @description Define la forma del contexto de autenticación que será
 *              accesible en toda la aplicación a través de {@link useAuth}.
 */
interface AuthContextType {
  /** Datos parciales del usuario autenticado, o `null` si no hay sesión activa */
  user: Partial<AuthResponse> | null;

  /** Indica si hay una operación de autenticación en curso */
  isLoading: boolean;

  /** Indica si el usuario tiene una sesión activa */
  isAuthenticated: boolean;

  /**
   * Inicia sesión con las credenciales proporcionadas.
   * @param credentials - Objeto con email y contraseña del usuario.
   */
  login: (credentials: LoginRequest) => Promise<void>;

  /**
   * Registra una nueva cuenta de usuario.
   * @param userData - Objeto con los datos del nuevo usuario.
   */
  register: (userData: RegisterRequest) => Promise<void>;

  /** Cierra la sesión del usuario actual y limpia el estado. */
  logout: () => Promise<void>;

  /** Verifica si existe una sesión activa consultando el almacenamiento local. */
  checkAuthStatus: () => Promise<void>;

  /** Mensaje del último error ocurrido, o `null` si no hay errores. */
  error: string | null;
}

/**
 * @constant AuthContext
 * @description Instancia del contexto de autenticación. Su valor inicial es
 *              `undefined` para forzar el uso dentro de un {@link AuthProvider}.
 */
const AuthContext = createContext<AuthContextType | undefined>(undefined);

/**
 * @function useAuth
 * @description Hook personalizado para consumir el contexto de autenticación.
 *              Debe usarse únicamente dentro del árbol de componentes de {@link AuthProvider}.
 *
 * @returns {AuthContextType} El contexto de autenticación con estado y acciones.
 *
 * @throws {Error} Si se usa fuera de un {@link AuthProvider}.
 *
 * @example
 * const { user, isAuthenticated, logout } = useAuth();
 */
export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider');
  return context;
};

/**
 * @function AuthProvider
 * @description Componente proveedor del contexto de autenticación. Gestiona el estado
 *              global de sesión y expone las acciones de autenticación a sus componentes hijos.
 *              Al montarse, ejecuta {@link checkAuthStatus} para restaurar sesiones previas.
 *
 * @param {object}    props          - Props del componente.
 * @param {ReactNode} props.children - Árbol de componentes que tendrán acceso al contexto.
 *
 * @returns {JSX.Element} El proveedor del contexto envolviendo a `children`.
 */
export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {

  /** @description Datos del usuario autenticado actualmente */
  const [user, setUser] = useState<Partial<AuthResponse> | null>(null);

  /** @description Estado de carga inicial en `true` hasta completar `checkAuthStatus` */
  const [isLoading, setIsLoading] = useState(true);

  /** @description Indica si el usuario tiene una sesión activa */
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  /** @description Último error de autenticación ocurrido */
  const [error, setError] = useState<string | null>(null);

  /**
   * @function checkAuthStatus
   * @description Verifica si existe una sesión activa en el almacenamiento local.
   *              Si la hay, carga los datos del usuario. En caso contrario, limpia
   *              el estado. Se ejecuta automáticamente al montar el proveedor.
   *
   * @returns {Promise<void>}
   */
  const checkAuthStatus = async (): Promise<void> => {
    try {
      setIsLoading(true);
      const auth = await authApi.isAuthenticated();
      setIsAuthenticated(auth);

      if (auth) {
        const userData = await authApi.getUserData();
        setUser(userData);
      } else {
        setUser(null);
      }
    } catch (err: any) {
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  /**
   * @function login
   * @description Autentica al usuario con sus credenciales, actualiza el estado
   *              con los datos recibidos del servidor y marca la sesión como activa.
   *
   * @param {LoginRequest} credentials - Objeto con email y contraseña del usuario.
   * @returns {Promise<void>}
   *
   * @throws Propaga el error para que la pantalla de login pueda manejarlo localmente.
   */
  const login = async (credentials: LoginRequest): Promise<void> => {
    try {
      setError(null);
      setIsLoading(true);
      const response = await authApi.login(credentials);
      setUser({
        accountId: response.accountId,
        email: response.email,
        names: response.names,
        lastnames: response.lastnames,
        phone: response.phone,
        hasMasterKey: response.hasMasterKey,
      });
      setIsAuthenticated(true);
    } catch (err: any) {
      setError(err.message);
      throw err;
    } finally {
      setIsLoading(false);
    }
  };

  /**
   * @function register
   * @description Registra una nueva cuenta de usuario, actualiza el estado con
   *              los datos recibidos del servidor y marca la sesión como activa.
   *
   * @param {RegisterRequest} userData - Objeto con los datos del nuevo usuario.
   * @returns {Promise<void>}
   *
   * @throws Propaga el error para que la pantalla de registro pueda manejarlo localmente.
   */
  const register = async (userData: RegisterRequest): Promise<void> => {
    try {
      setError(null);
      setIsLoading(true);
      const response = await authApi.register(userData);
      setUser({
        accountId: response.accountId,
        email: response.email,
        names: response.names,
        lastnames: response.lastnames,
        phone: response.phone,
        hasMasterKey: response.hasMasterKey,
      });
      setIsAuthenticated(true);
    } catch (err: any) {
      setError(err.message);
      throw err;
    } finally {
      setIsLoading(false);
    }
  };

  /**
   * @function logout
   * @description Cierra la sesión del usuario actual llamando a la API,
   *              limpia los datos del usuario y marca la sesión como inactiva.
   *
   * @returns {Promise<void>}
   */
  const logout = async (): Promise<void> => {
    await authApi.logout();
    setUser(null);
    setIsAuthenticated(false);
  };

  /**
   * @description Efecto que verifica el estado de autenticación al montar el proveedor,
   *              permitiendo restaurar sesiones previas sin requerir un nuevo inicio de sesión.
   */
  useEffect(() => {
    checkAuthStatus();
  }, []);

  return (
    <AuthContext.Provider
      value={{
        user,
        isLoading,
        isAuthenticated,
        login,
        register,
        logout,
        checkAuthStatus,
        error,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};