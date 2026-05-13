import api from './api.config'

/**
 * Servicio de autenticación.
 * Contiene las funciones que se comunican con el backend
 * para registro e inicio de sesión.
 * Base URL:https://audry-subsphenoidal-bovinely.ngrok-free.dev/api/auth
 */

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
 * El JWT viene con HttpOnly cookie y también en el body
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
    const { data: body} = await api.post<ApiResponse<AuthResponse>>('/auth/register', request);

    if (body.status !== 200 && body.status !== 201) {
        throw new Error(body.message || 'Error al registrar la cuennta.');
    }

    //Guarda token en sessionStorage para peticiones posteriores
    if (body.data.token) {
        sessionStorage.setItem('token', body.data.token);
        sessionStorage.setItem('accountId', body.data.accountId);
        sessionStorage.setItem('names', body.data.names);
    }

    return body.data;

}


/**
 * Inicia sesión con email y contraseña.
 * @param request - Credenciales del usuario
 * @returns AuthResponse con token y datos del usuario
 * @throws Error con el mensaje del backend si falla
 */

export async function login(request: LoginRequest): Promise<AuthResponse> {
    const { data: body } = await api.post<ApiResponse<AuthResponse>>('/auth/login', request);

    if (body.status !== 200) {
        throw new Error(body.message || 'Correo o contraseña incorrectos.');
    }

    //Guarda token en sessionStorage para peticiones posteriores
    if (body.data.token) {
        sessionStorage.setItem('token', body.data.token);
        sessionStorage.setItem('accountId', body.data.accountId);
        sessionStorage.setItem('names', body.data.names);
    }

    return body.data;
}

export async function logout(): Promise<void> {
  await api.post('/auth/logout');   
  sessionStorage.removeItem('token');  
}