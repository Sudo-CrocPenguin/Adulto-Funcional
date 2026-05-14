/**
 * Mapeadores entre entidades JPA y modelos de dominio del módulo de cuentas.
 *
 * <p>
 * Contiene el componente
 * {@link org.adultofuncional.main.account.infrastructure.persistence.mapper.AccountMapper},
 * responsable de convertir entre
 * {@link org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity}
 * (JPA),
 * {@link org.adultofuncional.main.account.domain.model.Account} (dominio) y
 * {@link org.adultofuncional.main.account.application.dto.AccountResponse}
 * (DTO de respuesta). Los mapeadores utilizan los métodos de fábrica del
 * dominio ({@code reconstitute}) para reconstituir instancias desde
 * persistencia.
 *
 * <h2>Seguridad</h2>
 * <ul>
 * <li><strong>Campos sensibles:</strong> El método {@code toResponse} nunca
 * expone {@code account_password} ni {@code account_master_key} al cliente,
 * garantizando que los datos sensibles permanezcan confinados a las capas de
 * dominio e infraestructura.</li>
 * </ul>
 *
 * @author Lydis Ester Jaraba
 * @since 0.0.1
 * @see org.adultofuncional.main.account.infrastructure.persistence.mapper.AccountMapper
 * @see org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity
 * @see org.adultofuncional.main.account.domain.model.Account
 * @see org.adultofuncional.main.account.application.dto.AccountResponse
 */
package org.adultofuncional.main.account.infrastructure.persistence.mapper;
