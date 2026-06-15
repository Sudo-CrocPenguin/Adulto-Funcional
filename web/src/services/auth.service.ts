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
    token?: string | null
    tokenType?: string | null
    expiresIn: number
    accountId: string
    names: string
    lastnames: string
    email: string
    phone: string
    createdAt: string
    hasMasterKey: boolean
}

export interface MasterKeyStatusResponse {
    hasMasterKey: boolean
    verified: boolean
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

    return body.data;
}

/**
 * Cierra la sesion del usuario.
 * Invalida el token en el backend y lo elimina del sessionStorage.
 * 
 * @returns {Promise<void>}
 * 
 * @example
 * await logout();
 */
export async function logout(): Promise<void> {
  await api.post('/auth/logout');   
}

/**
 * Envia un correo de recuperacion de contrasena al email proporcionado.
 * Por seguridad, siempre muestra mensaje de exito sin revelar si el correo existe.
 * 
 * @param {string} email - Correo electronico registrado
 * @returns {Promise<void>}
 * 
 */
export async function forgotPassword(email: string): Promise<void> {
    await api.post('/auth/forgot-password', { email });
}

/**
 * Restablece la contrasena usando un token de recuperacion.
 * El token se recibe por correo electronico tras llamar a forgotPassword().
 * 
 * @param {string} token - Token de recuperacion enviado por correo
 * @param {string} newPassword - Nueva contrasena (minimo 8 caracteres, una mayuscula, una minuscula, un numero)
 * @returns {Promise<void>}
 * @throws {Error} Si el token es invalido o ha expirado
 * 
 */
export async function resetPassword(token: string, newPassword: string): Promise<void> {
    await api.post('/auth/reset-password', { token, newPassword });
}

/**
 * Verifica si la clave maestra ingresada es correcta.
 * Se usa en la pantalla de acceso al gestor de contrasenas.
 * 
 * @param {string} masterKey - Clave maestra a verificar
 * @returns {Promise<boolean>} true si la clave es correcta, false si no
 * @throws {Error} Si el token JWT no es valido
 * 
 */
export async function getMasterKeyStatus(): Promise<MasterKeyStatusResponse> {
    const { data: body } = await api.get<ApiResponse<MasterKeyStatusResponse>>('/security/master-key/status');
    return body.data;
}

/**
 * Verifica si la clave maestra ingresada es correcta.
 * Se usa en la pantalla de acceso al gestor de contrasenas.
 * 
 * @param {string} masterKey - Clave maestra a verificar
 * @returns {Promise<MasterKeyStatusResponse>} estado actualizado del gestor
 * @throws {Error} Si el token JWT no es valido o la clave es incorrecta
 * 
 */
export async function verifyMasterKey(masterKey: string): Promise<MasterKeyStatusResponse> {
    const { data: body } = await api.post<ApiResponse<MasterKeyStatusResponse>>('/security/master-key/verify', { masterKey });
    return body.data;
}

/**
 * Crea o actualiza la clave maestra del usuario.
 * Se usa cuando el usuario configura su clave maestra por primera vez.
 * 
 * @param {string} masterKey - Nueva clave maestra
 * @returns {Promise<void>}
 * @throws {Error} Si el token JWT no es valido
 */
export async function createMasterKey(masterKey: string): Promise<MasterKeyStatusResponse> {
    const { data: body } = await api.post<ApiResponse<MasterKeyStatusResponse>>('/security/master-key', { masterKey });
    return body.data;
}

/**
 * Cambia la clave maestra y permite al backend recifrar credenciales existentes.
 *
 * @param {string} currentMasterKey - Clave maestra actual
 * @param {string} newMasterKey - Nueva clave maestra
 * @returns {Promise<MasterKeyStatusResponse>} estado actualizado del gestor
 */
export async function changeMasterKey(
    currentMasterKey: string,
    newMasterKey: string,
): Promise<MasterKeyStatusResponse> {
    const { data: body } = await api.patch<ApiResponse<MasterKeyStatusResponse>>('/security/master-key', {
        currentMasterKey,
        newMasterKey,
    });
    return body.data;
}

/**
 * Cierra la sesion interna de clave maestra en el backend.
 * No cierra la sesion general del usuario.
 */
export async function clearMasterKeySession(): Promise<MasterKeyStatusResponse> {
    const { data: body } = await api.delete<ApiResponse<MasterKeyStatusResponse>>('/security/master-key/session');
    return body.data;
}
