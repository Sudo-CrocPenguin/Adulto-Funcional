/**
 * Capa de infraestructura del módulo de cuentas.
 *
 * <p>
 * Implementa los adaptadores que conectan el dominio con tecnologías
 * externas: Spring MVC (REST), Spring Data JPA y MariaDB.
 * Ninguna clase de esta capa contiene reglas de negocio; toda la lógica
 * se limita a traducción, persistencia y exposición HTTP.
 *
 * <h2>Componentes</h2>
 * <ul>
 * <li><strong>controller</strong> — {@code AccountController}
 * <ul>
 * <li>Expone los endpoints REST bajo {@code /api/account}.</li>
 * <li>Delega la lógica de negocio en los casos de uso.</li>
 * <li>Valida ownership mediante
 * {@link org.adultofuncional.main.shared.security.OwnershipValidator}.</li>
 * <li>Las respuestas se envuelven en
 * {@link org.adultofuncional.main.shared.response.ApiResponse}.</li>
 * <li>Los DTOs de entrada son validados con {@code @Valid} (Jakarta
 * Validation),
 * incluyendo la anotación {@code @NoHtml} para prevenir XSS almacenado.</li>
 * </ul>
 * </li>
 * <li><strong>persistence</strong>
 * <ul>
 * <li>{@code AccountEntity} — entidad JPA mapeada a la tabla
 * {@code accounts}.</li>
 * <li>{@code AccountMapper} — convierte entre {@code AccountEntity},
 * {@link org.adultofuncional.main.account.domain.model.Account} (dominio) y
 * {@link org.adultofuncional.main.account.application.dto.AccountResponse}
 * (DTO).</li>
 * <li>{@code SpringAccountJpaRepository} — interfaz Spring Data que proporciona
 * las operaciones CRUD y la búsqueda por email.</li>
 * </ul>
 * </li>
 * <li><strong>repository</strong> — {@code AccountRepositoryImpl}
 * <ul>
 * <li>Implementa el puerto
 * {@link org.adultofuncional.main.account.domain.repository.AccountRepository}.</li>
 * <li>Delega en {@code SpringAccountJpaRepository} y usa {@code AccountMapper}
 * para mantener el dominio aislado de JPA.</li>
 * </ul>
 * </li>
 * </ul>
 *
 * @author Lydis Ester Jaraba, Juan Sebastian Rios, Jeronimo Ospina Zapata,
 *         Daniel Salazar
 * @since 0.0.1
 */
package org.adultofuncional.main.account.infrastructure;
