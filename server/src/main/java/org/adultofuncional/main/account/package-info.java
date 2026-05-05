/**
 * Módulo de gestión de cuentas de usuario.
 *
 * <p>
 * Implementa la funcionalidad completa de cuentas bajo los principios de
 * Clean Architecture, distribuyendo las responsabilidades en las capas de
 * dominio, aplicación e infraestructura.
 *
 * <h3>Responsabilidades</h3>
 * <ul>
 * <li>Registro de nuevas cuentas (a través del módulo de autenticación).</li>
 * <li>Actualización de datos personales (nombres, apellidos, teléfono,
 * email).</li>
 * <li>Consulta de información no sensible (sin {@code password_hash} ni
 * {@code master_key_hash}).</li>
 * <li>Eliminación de cuenta y todos sus datos asociados en cascada (pendiente
 * de implementación).</li>
 * <li>Validación de unicidad de correo electrónico.</li>
 * <li>Validación de ownership: un usuario solo puede acceder o modificar su
 * propia cuenta.</li>
 * <li>Protección contra Stored XSS mediante la anotación {@code @NoHtml} en los
 * DTOs de entrada.</li>
 * </ul>
 *
 * <h3>Estructura</h3>
 * 
 * <pre>
 * account/
 * ├── domain/            → Modelo {@code
 * Account
 * }, invariantes y puerto {@code
 * AccountRepository
 * }
 * ├── application/       → Casos de uso ({@code
 * GetAccountUseCase
 * }, {@code
 * UpdateAccountUseCase
 * })
 * │                        y DTOs ({@code
 * AccountResponse
 * }, {@code
 * UpdateAccountRequest
 * })
 * └── infrastructure/    → Controlador REST, entidad JPA, mapeador y repositorio concreto
 * </pre>
 *
 * <h3>Seguridad</h3>
 * <ul>
 * <li>Todos los endpoints requieren autenticación JWT.</li>
 * <li>La validación de ownership se ejecuta antes de cualquier operación.</li>
 * <li>Los campos sensibles ({@code password}, {@code masterKey}) nunca se
 * exponen en las respuestas.</li>
 * <li>Los datos de entrada se validan con {@code @NoHtml} para rechazar
 * HTML/scripts maliciosos.</li>
 * </ul>
 *
 * <h3>Tabla asociada</h3>
 * {@code accounts} — identificador UUID v7, email único, hashes Argon2.
 *
 * @author Equipo de desarrollo Adulto Funcional
 * @since 0.0.1
 */
package org.adultofuncional.main.account;
