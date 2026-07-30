/**
 * Capa de persistencia del módulo de seguridad (gestor de contraseñas).
 *
 * <p>
 * Agrupa los componentes JPA que traducen el modelo de dominio a la base de
 * datos relacional. Incluye la entidad JPA, los repositorios Spring Data y
 * los mapeadores que convierten entre las representaciones de persistencia y
 * dominio.
 *
 * <h2>Subpaquetes</h2>
 * <ul>
 * <li>{@code entity} —
 * {@link org.adultofuncional.main.security.infrastructure.persistence.entity.PasswordEntity}</li>
 * <li>{@code repository} —
 * {@link org.adultofuncional.main.security.infrastructure.persistence.repository.PasswordJpaRepository}</li>
 * <li>{@code mapper} —
 * {@link org.adultofuncional.main.security.infrastructure.persistence.mapper.PasswordMapper}</li>
 * </ul>
 *
 * @author Daniel Salazar
 * @since 1.0
 * @see org.adultofuncional.main.security.infrastructure.persistence.entity
 * @see org.adultofuncional.main.security.infrastructure.persistence.repository
 * @see org.adultofuncional.main.security.infrastructure.persistence.mapper
 */
package org.adultofuncional.main.security.infrastructure.persistence;
