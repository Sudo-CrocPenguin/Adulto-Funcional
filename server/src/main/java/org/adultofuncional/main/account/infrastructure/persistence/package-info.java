/**
 * Capa de persistencia del módulo de cuentas de usuario.
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
 * {@link org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity}</li>
 * <li>{@code mapper} —
 * {@link org.adultofuncional.main.account.infrastructure.persistence.mapper.AccountMapper}</li>
 * <li>{@code repository} —
 * {@link org.adultofuncional.main.account.infrastructure.persistence.repository.SpringAccountJpaRepository}</li>
 * </ul>
 *
 * @author Daniel Salazar, Lydis Ester Jaraba, Juan Sebastian Rios
 * @since 0.0.1
 * @see org.adultofuncional.main.account.infrastructure.persistence.entity
 * @see org.adultofuncional.main.account.infrastructure.persistence.mapper
 * @see org.adultofuncional.main.account.infrastructure.persistence.repository
 */
package org.adultofuncional.main.account.infrastructure.persistence;
