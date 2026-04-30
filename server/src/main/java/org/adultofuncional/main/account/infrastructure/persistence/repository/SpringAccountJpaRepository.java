package org.adultofuncional.main.account.infrastructure.persistence.repository;

import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA que opera sobre {@link AccountEntity}.
 *
 * <p>
 * Extiende {@link JpaRepository} — Spring genera automáticamente un proxy
 * que traduce las llamadas a SQL contra MariaDB. Actúa como adaptador de
 * infraestructura para el puerto
 * {@link org.adultofuncional.main.account.domain.repository.AccountRepository}.
 *
 * <p>
 * Métodos disponibles:
 * <ul>
 * <li>CRUD heredado: {@code save}, {@code findById}, {@code findAll},
 * {@code deleteById}</li>
 * <li>{@code findByAccountEmail} — consulta sobre la columna UNIQUE
 * {@code account_email}</li>
 * </ul>
 *
 * @author Daniel Salazar
 * @since 0.0.1
 */
public interface SpringAccountJpaRepository extends JpaRepository<AccountEntity, UUID> {

  /**
   * Busca una cuenta por su correo electrónico exacto.
   *
   * <p>
   * Spring Data genera la query sobre {@code account_email} (VARCHAR(255)
   * UNIQUE).
   * La comparación es case-sensitive según el collation de MariaDB.
   *
   * @param email correo electrónico a buscar
   * @return {@link Optional} con la entidad si existe, vacío si no
   */
  Optional<AccountEntity> findByAccount_email(String email);
}
