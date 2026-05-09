/**
 * DTOs de entrada y salida para el módulo de autenticación.
 *
 * <p>
 * Contiene los objetos de transferencia de datos para las operaciones de
 * login, registro y respuesta de autenticación. Estos DTOs definen los
 * contratos de comunicación entre los clientes de la API y la capa de
 * aplicación, aplicando validaciones de formato, reglas de negocio y
 * protección anti‑XSS.
 *
 * <h2>DTOs incluidos</h2>
 * <ul>
 * <li>{@link org.adultofuncional.main.auth.application.dto.AuthResponse} —
 * Respuesta de autenticación con los datos de la cuenta y el token JWT
 * (este último solo se incluye para clientes nativos; en web se omite
 * con {@code withoutToken()}).</li>
 * <li>{@link org.adultofuncional.main.auth.application.dto.LoginRequest} —
 * Credenciales de inicio de sesión (email y contraseña). Ambos campos
 * están validados con {@code @NotBlank}, {@code @Email} y {@code @Size},
 * y protegidos con {@code @NoHtml} (excepto la contraseña, que admite
 * caracteres especiales).</li>
 * <li>{@link org.adultofuncional.main.auth.application.dto.RegisterRequest} —
 * Datos para el registro de un nuevo usuario: nombres, apellidos,
 * teléfono, email, contraseña y clave maestra opcional. Todos los campos
 * visibles están anotados con {@code @NoHtml} para prevenir XSS.</li>
 * </ul>
 *
 * <h2>Seguridad</h2>
 * <ul>
 * <li><strong>Stored XSS:</strong> Los campos de texto libre en
 * {@code LoginRequest} y {@code RegisterRequest} están protegidos con
 * {@link org.adultofuncional.main.shared.security.NoHtml}, que utiliza
 * Jsoup con {@code Safelist.none()} para rechazar cualquier contenido
 * HTML malicioso.</li>
 * <li><strong>Exposición del token:</strong> El token JWT solo se incluye
 * en el body para clientes nativos; los clientes web usan la cookie
 * HttpOnly gestionada por
 * {@link org.adultofuncional.main.config.security.CookieUtils}.</li>
 * </ul>
 *
 * @author Miguel Angel Blandon Montes, Juan Sebastian Rios
 * @since 0.0.1
 * @see org.adultofuncional.main.auth.application.usecase
 * @see org.adultofuncional.main.shared.security.NoHtml
 * @see org.adultofuncional.main.config.security.CookieUtils
 */
package org.adultofuncional.main.auth.application.dto;
