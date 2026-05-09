/**
 * Adaptadores de repositorio del módulo de cuentas de usuario.
 *
 * <p>
 * Contiene la implementación concreta del puerto
 * {@link org.adultofuncional.main.account.domain.repository.AccountRepository},
 * que conecta el dominio con Spring Data JPA y MariaDB. Utiliza
 * {@link org.adultofuncional.main.account.infrastructure.persistence.repository.SpringAccountJpaRepository}
 * y el
 * {@link org.adultofuncional.main.account.infrastructure.persistence.mapper.AccountMapper}
 * para traducir entre entidades JPA y el modelo de dominio.
 *
 * <h2>Adaptador incluido</h2>
 * <ul>
 * <li>{@link org.adultofuncional.main.account.infrastructure.repository.AccountRepositoryImpl}
 * —
 * Implementa todas las operaciones definidas en el puerto de dominio:
 * guardar, buscar por ID, buscar por email, listar todas, verificar
 * existencia y eliminar con cascada.</li>
 * </ul>
 *
 * @author Jeronimo Ospina Zapata
 * @since 0.0.1
 * @see org.adultofuncional.main.account.infrastructure.repository.AccountRepositoryImpl
 * @see org.adultofuncional.main.account.domain.repository.AccountRepository
 * @see org.adultofuncional.main.account.infrastructure.persistence.repository.SpringAccountJpaRepository
 */
package org.adultofuncional.main.account.infrastructure.repository;
