package org.adultofuncional.main.account.infrastructure.persistence.repository;

import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA que opera sobre {@link AccountEntity}.
 *
 * <p>Extiende {@link JpaRepository} y Spring genera automáticamente un proxy
 * en tiempo de ejecución que traduce las llamadas a operaciones SQL contra
 * MariaDB. Actúa como adaptador de infraestructura para el puerto
 * {@link org.adultofuncional.main.account.domain.repository.AccountRepository}.
 *
 * <p>Proporciona:
 * <ul>
 *   <li>Métodos CRUD heredados: {@code save}, {@code findById}, {@code findAll}, {@code deleteById}</li>
 *   <li>Consulta derivada {@code findByAccount_email(String)} sobre la columna UNIQUE {@code account_email}</li>
 * </ul>
 *
 * @author Daniel Salazar
 * @since 0.0.1
 */
public interface SpringAccountJpaRepository extends JpaRepository<AccountEntity, UUID> {

    /**
     * Busca una cuenta por su correo electrónico exacto.
     *
     * <p>Consulta generada sobre la columna {@code account_email} (VARCHAR(255) UNIQUE).
     * La comparación es case-sensitive según el collation de MariaDB.
     *
     * @param email correo electrónico a buscar
     * @return {@code Optional} con la entidad si existe, o {@code Optional.empty()} si no
     */
    Optional<AccountEntity> findByAccount_email(String email);

    Optional<AccountEntity> findByEmail(String email);
}
