/**
 * Servicio de autenticación.
 * Contiene las funciones que se comunican con el backend
 * para registro e inicio de sesión.
 * Base URL: http://localhost:8080/api/auth
 */

const BASE_URL = 'http://localhost:8080/api/auth'

/**
 * Datos que se envían al backend para registrar un nuevo usuario.
 */

export interface RegisterRequest {
    names: string
    lastnames: string
    phone: string
    email: string
    password: string
    masterKey?: string
}

/**
 * Datos que se envían al backend para iniciar sesión
 */

export interface LoginRequest {
    email: string
    password: string
}

/**
 * Datos del usuario autenticado recibidos del backend.
 */

export interface AuthResponse {
    token: string
    tokenType: string
    expiresIn: number
    accountId: string
    names: string
    lastnames: string
    email: string
    phone: string
    createdAt: string
    hasMasterKey: boolean
}

/**
 * Formato estandar de respuesta del backend
 */

interface ApiResponse<T> {
    status: number
    message: string
    data: T
}

/**
 * Registra un nuevo usuario en el sistema
 * @param request - Datos del formulario de registro
 * @returns AuthResponse con token y datos del usuario creado
 * @throws Error con el mensaje del backend si falla
 */

export async function register(request: RegisterRequest): Promise<AuthResponse> {
    const response = await fetch(`${BASE_URL}/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(request),
    })

    const body: ApiResponse<AuthResponse> = await response.json()

    if (!response.ok) {
        throw new Error(body.message || 'Error al registrar la cuenta.')
    }

    return body.data
}

/**
 * Inicia sesión con email y contraseña.
 * @param request - Credenciales del usuario
 * @returns AuthResponse con token y datos del usuario
 * @throws Error con el mensaje del backend si falla
 */

export async function login(request: LoginRequest): Promise<AuthResponse> {
    const response = await fetch(`${BASE_URL}/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(request),
    })

    const body: ApiResponse<AuthResponse> = await response.json()

    if (!response.ok) {
        throw new Error(body.message || 'Correo o contraseña incorrectos.')
    }

    return body.data
}