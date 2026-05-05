/**
 * Módulo de autenticación y autorización.
 *
 * <p>
 * Gestiona el acceso al sistema mediante login, registro y logout,
 * aplicando las capas de aplicación e infraestructura bajo los principios
 * de Clean Architecture. Toda la lógica de negocio se encuentra en los
 * casos de uso; los controladores son simples adaptadores HTTP.
 *
 * <h2>Responsabilidades</h2>
 * <ul>
 * <li>Autenticación stateless con JSON Web Tokens (JWT).</li>
 * <li>Registro de nuevos usuarios con verificación de unicidad del email.</li>
 * <li>Hash de contraseñas y Master Key con Argon2.</li>
 * <li>Entrega segura del token:
 * <ul>
 * <li>Siempre en cookie HttpOnly ({@code CookieUtils}).</li>
 * <li>Adicionalmente en el body solo para clientes nativos
 * ({@code ClientTypeResolver}).</li>
 * </ul>
 * </li>
 * </ul>
 *
 * <h2>Estructura</h2>
 * 
 * <pre>
 * auth/
 * ├── application/       → Casos de uso y DTOs
 * │   ├── usecase/       → {@code
 * LoginUseCase
 * }, {@code
 * RegisterUseCase
 * }
 * │   └── dto/           → {@code
 * AuthResponse
 * }, {@code
 * LoginRequest
 * }, {@code
 * RegisterRequest
 * }
 * └── infrastructure/    → Controlador REST ({@code
 * AuthController
 * })
 * </pre>
 *
 * <h2>Seguridad</h2>
 * <ul>
 * <li>Durante el login, no se distingue entre email inexistente y contraseña
 * incorrecta; ambos producen un error genérico para evitar enumeración
 * de usuarios.</li>
 * <li>El registro lanza {@code ConflictException} (HTTP 409) si el email ya
 * está registrado, diferenciando claramente el conflicto de un error
 * de formato.</li>
 * <li>Nunca se exponen el hash de contraseña ni la Master Key en las
 * respuestas.</li>
 * <li>Los DTOs de entrada incluyen {@code @NoHtml} para proteger contra
 * Stored XSS, incluso en campos que no se renderizan (postura de seguridad
 * estricta).</li>
 * </ul>
 *
 * <h2>Tabla asociada</h2>
 * {@code accounts} — columnas {@code account_password} y
 * {@code account_master_key}
 * (hashes Argon2).
 *
 * @author Jeronimo Ospina Zapata, Lydis Ester Jaraba, Juan Sebastian Rios,
 *         Miguel Angel Blandon Montes, Daniel Salazar
 * @since 0.0.1
 */
package org.adultofuncional.main.auth;
