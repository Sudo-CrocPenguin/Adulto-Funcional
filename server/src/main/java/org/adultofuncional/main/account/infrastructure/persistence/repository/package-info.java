/**
 * Repositorios Spring Data JPA del módulo de cuentas.
 *
 * <p>
 * Contiene la interfaz
 * {@link org.adultofuncional.main.account.infrastructure.persistence.repository.SpringAccountJpaRepository},
 * que extiende {@link org.springframework.data.jpa.repository.JpaRepository} y
 * define consultas específicas para la entidad
 * {@link org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity}.
 * Estas interfaces son utilizadas por los adaptadores de repositorio en la capa
 * de infraestructura, en particular por
 * {@link org.adultofuncional.main.account.infrastructure.repository.AccountRepositoryImpl}.
 *
 * <h2>Interfaz incluida</h2>
 * <ul>
 * <li>{@link org.adultofuncional.main.account.infrastructure.persistence.repository.SpringAccountJpaRepository}
 * —
 * Proporciona operaciones CRUD heredadas y una consulta personalizada
 * {@code findByAccountEmail} sobre la columna única
 * {@code account_email}.</li>
 * </ul>
 *
 * <h2>Consideraciones</h2>
 * <ul>
 * <li>Spring Data genera automáticamente la implementación de los métodos
 * heredados y de las consultas derivadas del nombre del método.</li>
 * <li>Estas interfaces no deben contener lógica de negocio; su propósito es
 * únicamente el acceso a datos.</li>
 * </ul>
 *
 * @author Daniel Salazar
 * @since 0.0.1
 * @see org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity
 * @see org.adultofuncional.main.account.infrastructure.repository.AccountRepositoryImpl
 */
package org.adultofuncional.main.account.infrastructure.persistence.repository;
