/**
 * Mapeadores entre entidades JPA y modelos de dominio del módulo de seguridad.
 *
 * <p>
 * Contiene el componente
 * {@link org.adultofuncional.main.security.infrastructure.persistence.mapper.PasswordMapper},
 * responsable de convertir entre
 * {@link org.adultofuncional.main.security.infrastructure.persistence.entity.PasswordEntity}
 * (JPA) y {@link org.adultofuncional.main.security.domain.model.Password}
 * (dominio). Los mapeadores utilizan los métodos de fábrica del dominio
 * ({@code reconstitute} y {@code create}) y construyen las entidades JPA
 * necesarias para la persistencia.
 *
 * <h2>Seguridad</h2>
 * <ul>
 * <li><strong>Datos sensibles:</strong> El campo cifrado
 * ({@code encryptedPassword} / {@code passwordCiphertext}) se transfiere
 * entre capas pero nunca se expone en respuestas al cliente sin descifrado
 * previo.</li>
 * </ul>
 *
 * @author Lidys Jaraba
 * @since 0.0.1
 * @see org.adultofuncional.main.security.infrastructure.persistence.entity.PasswordEntity
 * @see org.adultofuncional.main.security.domain.model.Password
 */
package org.adultofuncional.main.security.infrastructure.persistence.mapper;
