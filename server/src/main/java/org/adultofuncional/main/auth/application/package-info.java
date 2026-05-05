/**
 * Capa de aplicación del módulo de autenticación.
 *
 * <p>
 * Contiene los casos de uso y DTOs que orquestan la lógica de
 * inicio de sesión y registro. Los casos de uso coordinan el dominio
 * y la infraestructura sin exponer detalles de persistencia.
 *
 * <h2>Casos de uso</h2>
 * <ul>
 * <li>{@code LoginUseCase} — Autentica al usuario por email y contraseña.
 * No distingue entre email inexistente y contraseña incorrecta (lanza
 * {@code UnauthorizedException} genérica) para evitar enumeración de
 * usuarios.</li>
 * <li>{@code RegisterUseCase} — Registra una nueva cuenta, validando la
 * unicidad del email. Hashea la contraseña y la Master Key opcional con
 * Argon2, genera el modelo de dominio y emite un token JWT.</li>
 * </ul>
 *
 * <h2>DTOs</h2>
 * <ul>
 * <li>{@code AuthResponse} — Datos públicos de la cuenta más el token JWT
 * (solo para clientes nativos; los clientes web reciben una copia sin
 * token mediante {@code withoutToken()}). Nunca expone el hash de la
 * contraseña ni la Master Key.</li>
 * <li>{@code LoginRequest} — Credenciales de inicio de sesión. Validado con
 * {@code @NoHtml} en ambos campos para máxima prevención de XSS.</li>
 * <li>{@code RegisterRequest} — Datos de registro. Los campos visibles
 * (nombres, apellidos, teléfono, email) llevan {@code @NoHtml}. Las
 * contraseñas también lo incluyen por política de seguridad estricta,
 * aunque no se renderizan en el frontend.</li>
 * </ul>
 *
 * <h2>Seguridad</h2>
 * <ul>
 * <li>Las contraseñas viajan en texto plano solo en el request; se comparan
 * o almacenan como hash Argon2.</li>
 * <li>El token JWT se emite siempre en cookie HttpOnly, y adicionalmente en
 * el body solo si el cliente es nativo ({@code ClientTypeResolver}).</li>
 * <li>La protección anti‑XSS se aplica en todos los campos de entrada.</li>
 * </ul>
 *
 * @author Juan Sebastian Rios, Lydis Ester Jaraba, Miguel Angel Blandon Montes
 * @since 0.0.1
 */
package org.adultofuncional.main.auth.application;
