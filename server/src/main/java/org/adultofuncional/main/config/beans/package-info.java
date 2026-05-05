/**
 * Configuración de beans generales de la aplicación.
 *
 * <p>
 * Contiene definiciones de beans de Spring que no pertenecen a un módulo
 * de negocio específico, sino que son transversales a toda la aplicación.
 *
 * <h2>Beans disponibles</h2>
 * <ul>
 * <li>{@link org.adultofuncional.main.config.beans.AppConfig#passwordEncoder()
 * PasswordEncoder} — codificador de contraseñas con Argon2. Usa los
 * parámetros por defecto de Spring Security 5.8. Es consumido por los
 * casos de uso de autenticación ({@code LoginUseCase},
 * {@code RegisterUseCase}) y por cualquier otro componente que requiera
 * verificar o generar hashes.</li>
 * </ul>
 *
 * @see org.adultofuncional.main.config.beans.AppConfig
 * @author Juan Sebastian Rios
 * @since 0.0.1
 */
package org.adultofuncional.main.config.beans;
