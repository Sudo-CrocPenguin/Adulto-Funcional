/**
 * Módulo de seguridad — Gestor de contraseñas.
 *
 * <p>
 * Implementa la funcionalidad de almacenamiento seguro de credenciales de
 * servicios externos bajo los principios de Clean Architecture. Cada
 * credencial se cifra con AES‑256 utilizando una clave derivada de la Master
 * Key del usuario, un salt único y un vector de inicialización (IV) aleatorio.
 *
 * <h2>Responsabilidades</h2>
 * <ul>
 * <li>Almacenar credenciales de plataformas externas (nombre de aplicación,
 * usuario, contraseña cifrada, notas).</li>
 * <li>Cifrar y descifrar contraseñas con AES‑256 (clave derivada de la Master
 * Key + salt).</li>
 * <li>Listar, obtener, actualizar y eliminar credenciales de una cuenta.</li>
 * <li>Proteger el acceso al gestor mediante verificación de Master Key.</li>
 * <li>Protección contra Stored XSS mediante {@code @NoHtml} en los DTOs de
 * entrada.</li>
 * </ul>
 *
 * <h2>Estructura</h2>
 * 
 * <pre>
 * security/
 * ├── domain/            → Modelo {@code
 * Password
 * } y puerto
 * │                         {@code
 * PasswordRepository
 * }
 * ├── application/       → Casos de uso y DTOs
 * └── infrastructure/    → Controlador REST, entidad JPA, mapeador y
 *                           adaptador de repositorio
 * </pre>
 *
 * <h2>Seguridad</h2>
 * <ul>
 * <li>Acceso al gestor requiere verificar la Master Key del usuario (hash
 * Argon2 en {@code accounts.account_master_key}).</li>
 * <li>Cada credencial se cifra con una clave AES única derivada de la Master
 * Key + salt por registro.</li>
 * <li>El texto cifrado nunca se expone sin control en respuestas API.</li>
 * <li>Los DTOs de entrada validan {@code @NoHtml} para prevenir inyección de
 * scripts.</li>
 * </ul>
 *
 * <h2>Tabla asociada</h2>
 * {@code passwords} — identificador UUID v7, FK a {@code accounts}.
 *
 * @author Equipo de desarrollo Adulto Funcional
 * @since 0.0.1
 * @see org.adultofuncional.main.security.domain
 * @see org.adultofuncional.main.security.application
 * @see org.adultofuncional.main.security.infrastructure
 */
package org.adultofuncional.main.security;
