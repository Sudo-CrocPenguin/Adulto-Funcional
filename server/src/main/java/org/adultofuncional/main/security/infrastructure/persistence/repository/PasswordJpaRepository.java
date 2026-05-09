package org.adultofuncional.main.security.infrastructure.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.adultofuncional.main.security.infrastructure.persistence.entity.PasswordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data JPA para la entidad {@link PasswordEntity}.
 *
 * <p>
 * Proporciona métodos de acceso a la tabla {@code passwords} sin exponer
 * la implementación concreta al dominio. Este repositorio es utilizado por
 * el adaptador
 * {@link org.adultofuncional.main.security.infrastructure.repository.PasswordRepositoryImpl}
 * para traducir las operaciones del puerto
 * {@link org.adultofuncional.main.security.domain.repository.PasswordRepository}
 * a consultas JPA.
 *
 * @author Daniel Salazar
 * @since 1.0
 * @see PasswordEntity
 */
public interface PasswordJpaRepository extends JpaRepository<PasswordEntity, UUID> {

  /**
   * Busca todas las credenciales asociadas a una cuenta específica.
   *
   * @param accountId UUID de la cuenta propietaria.
   * @return lista de entidades {@code PasswordEntity} de esa cuenta;
   *         puede estar vacía si no hay contraseñas registradas.
   */
  List<PasswordEntity> findByAccount_AccountId(UUID accountId);
}
