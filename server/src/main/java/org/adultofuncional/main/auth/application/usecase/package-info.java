/**
 * Casos de uso del módulo de autenticación.
 *
 * <p>
 * Contiene los servicios de aplicación que orquestan las operaciones de
 * inicio de sesión y registro de usuarios. Cada caso de uso coordina el
 * dominio ({@link org.adultofuncional.main.account.domain.model.Account}),
 * los puertos de repositorio y los servicios de infraestructura
 * ({@link org.adultofuncional.main.config.security.JwtService},
 * {@link org.springframework.security.crypto.password.PasswordEncoder})
 * para ejecutar una única operación de negocio, manteniendo la lógica de
 * aplicación desacoplada de los controladores REST.
 *
 * <h2>Casos de uso incluidos</h2>
 * <ul>
 * <li>{@link org.adultofuncional.main.auth.application.usecase.LoginUseCase} —
 * Autentica a un usuario con su email y contraseña. Genera un token JWT
 * firmado y retorna los datos públicos de la cuenta. No distingue entre
 * email inexistente y contraseña incorrecta para evitar enumeración de
 * usuarios.</li>
 * <li>{@link org.adultofuncional.main.auth.application.usecase.RegisterUseCase}
 * —
 * Registra un nuevo usuario. Hashea la contraseña y la Master Key opcional
 * con Argon2, crea la cuenta en el dominio y genera un token JWT para
 * autenticación inmediata. Lanza
 * {@link org.adultofuncional.main.shared.exception.ConflictException}
 * si el email ya está registrado.</li>
 * </ul>
 *
 * <h2>Consideraciones de seguridad</h2>
 * <ul>
 * <li><strong>Contraseñas:</strong> Nunca se almacenan en texto plano.
 * Se hashean con Argon2 mediante {@code PasswordEncoder} antes de
 * persistir.</li>
 * <li><strong>Master Key:</strong> Es opcional. Si se proporciona, también
 * se hashea con Argon2.</li>
 * <li><strong>JWT:</strong> El token se genera con HMAC-SHA256 e incluye
 * los claims {@code sub}, {@code email} y {@code roles}.</li>
 * <li><strong>Respuesta:</strong> El token JWT se incluye en el body solo
 * para clientes nativos; los clientes web lo reciben exclusivamente en
 * una cookie HttpOnly.</li>
 * </ul>
 *
 * @author Lydis Ester Jaraba, Juan Sebastian Rios
 * @since 0.0.1
 * @see org.adultofuncional.main.auth.application.dto
 * @see org.adultofuncional.main.account.domain.model.Account
 * @see org.adultofuncional.main.config.security.JwtService
 */
package org.adultofuncional.main.auth.application.usecase;
