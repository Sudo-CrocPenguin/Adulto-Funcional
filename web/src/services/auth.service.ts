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
  sessionStorage.removeItem('token');  
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
export async function verifyMasterKey(masterKey: string): Promise<boolean> {
    const { data: body } = await api.post<ApiResponse<{ valid: boolean }>>('/security/master-key/verify', { masterKey });
    return body.data.valid;
}

/**
 * Crea o actualiza la clave maestra del usuario.
 * Se usa cuando el usuario configura su clave maestra por primera vez.
 * 
 * @param {string} masterKey - Nueva clave maestra
 * @returns {Promise<void>}
 * @throws {Error} Si el token JWT no es valido
 */
export async function createMasterKey(masterKey: string): Promise<void> {
    let accountId = sessionStorage.getItem('accountId');

    if (!accountId) {
        const user = JSON.parse(sessionStorage.getItem('user') || 'null');
        accountId = user?.accountId ?? null;
    }

    if (!accountId) throw new Error ('No se encontró el ID de cuenta');
    await api.patch(`/account/${accountId}`, { masterKey });
}

/**
 * Envia un correo con un codigo de verificacion para recuperar la clave maestra.
 * Primer paso del flujo de recuperacion de clave maestra.
 * 
 * @param {string} email - Correo electronico registrado
 * @returns {Promise<void>}
 *
 */
export async function forgotMasterKey(email: string): Promise<void> {
    await api.post('/auth/forgot-master-key', { email });
}

/**
 * Verifica el codigo de recuperacion enviado por correo.
 * Segundo paso del flujo de recuperacion de clave maestra.
 * 
 * @param {string} email - Correo electronico registrado
 * @param {string} code - Codigo de 6 digitos recibido por correo
 * @returns {Promise<string>} Token temporal para completar el reseteo
 * @throws {Error} Si el codigo es incorrecto o ha expirado
 */
export async function verifyResetCode(email: string, code: string): Promise<string> {
    const { data: body } = await api.post<ApiResponse<{ token: string }>>('/auth/verify-reset-code', { email, code });
    return body.data.token;
}

/**
 * Restablece la clave maestra usando el token obtenido tras verificar el codigo.
 * Tercer y ultimo paso del flujo de recuperacion de clave maestra.
 * 
 * @param {string} email - Correo electronico registrado
 * @param {string} code - Codigo de verificacion
 * @param {string} newMasterKey - Nueva clave maestra
 * @returns {Promise<void>}
 * @throws {Error} Si el token es invalido o ya fue usado
 */
export async function resetMasterKey(email: string, code: string, newMasterKey: string): Promise<void> {
    await api.post('/auth/reset-master-key', { email, code, newMasterKey });
}

/**
 * Reenvia el codigo de verificacion al correo del usuario.
 * Se usa cuando el codigo anterior expiro o no fue recibido.
 * 
 * @param {string} email - Correo electronico registrado
 * @returns {Promise<void>}
 * 
 */
export async function resendResetCode(email: string): Promise<void> {
    await api.post('/auth/resend-code', { email });
}