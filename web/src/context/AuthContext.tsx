/**
 * Contexto global de autenticación.
 * Provee el estado de sesión del usuario a toda la aplicación:
 * token JWT, datos del usuario, y funciones de login/logout.
 */

import { createContext, useContext, useState, type ReactNode } from "react";

/**
 * Datos del usuario autenticado.
 * Corresponde a los campos de AuthResponse del backend.
 */
interface AuthUser {
    accountId: string
    names: string
    lastnames: string
    email: string
    phone: string
    hasMasterKey: boolean
}

/**
 * Estructura del contexto de autenticación.
 */
interface AuthContextType {

    /** Token JWT de la sesión activa. Null si no hay sesión. */
    token: string | null
    /** Datos del usuario autenticado. Null si no hay sesión. */
    user: AuthUser | null
     /** Indica si hay una sesión activa. */
    isAuthenticated: boolean

    /**
     * Inicia sesión guardando el token y los datos del usuario.
     * TODO: llamar después de recibir respuesta exitosa del backend.
     */
    login: (token: string, user: AuthUser) => void

    /**
     * Cierra sesión limpiando el token y los datos del usuario.
     */
    logout: () => void
}
/** Contexto de autenticación */
const AuthContext = createContext<AuthContextType | null>(null)

/**
 * Proveedor del contexto de autenticación.
 * Debe envolver toda la aplicación en main.tsx para que
 * cualquier componente pueda acceder al estado de sesión.
 */
export function AuthProvider({ children }: { children: ReactNode }) {

    const [token, setToken] = useState<string | null>(null)
    const [user, setUser] = useState<AuthUser | null>(null)

    /**
     * Guarda el token y los datos del usuario al autenticarse.
     * @param token - JWT recibido del backend
     * @param user - Datos del usuario recibidos del backend
     */

    const login = (token: string, user: AuthUser) => {
        setToken(token)
        setUser(user)
    }

     /**
     * Limpia el token y los datos del usuario al cerrar sesión.
     */
    const logout = () => {
        setToken(null)
        setUser(null)
    }

    return (

        <AuthContext.Provider value={{
            token,
            user,
            isAuthenticated: !!token,
            login,
            logout,
        }}>
            {children}
        </AuthContext.Provider>
    )
}

/**
 * Hook para consumir el contexto de autenticación.
 * Debe usarse dentro de un componente envuelto por AuthProvider.
 * @returns El contexto de autenticación con token, user y funciones.
 * @throws Error si se usa fuera del AuthProvider.
 */

export function useAuth(): AuthContextType {
    const context = useContext(AuthContext)

    if (!context) {
        throw new Error('userAuth debe usarse dentro de AuthProvider')
    }

    return context
}