/**
 * Componente de ruta protegida.
 * Verifica si hay una sesión activa antes de renderizar el contenido.
 * Si no hay token, redirige automáticamente al login.
 */


import { Navigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import type { ReactNode } from 'react'

interface ProtectedRouterProps {
    /** Contenido a renderizar si hay sesión activa */
    children: ReactNode
}

/**
 * Envuelve rutas privadas para protegerlas de acceso no autorizado.
 * Uso en App.tsx:
 * ```tsx
 * <Route path="/dashboard" element={
 *     <ProtectedRoute>
 *         <Layout><Dashboard /></Layout>
 *     </ProtectedRoute>
 * } />
 * ```
 * @param children - Componente a renderizar si el usuario está autenticado.
 * @returns El children si hay sesión, o redirige a /login si no hay.
 */

function ProtectedRoute({ children }: ProtectedRouterProps) {

    const { isAuthenticated} = useAuth()

    if (!isAuthenticated) {
        return <Navigate to="/login" replace />
    }

    return <>{children}</>
}

export default ProtectedRoute