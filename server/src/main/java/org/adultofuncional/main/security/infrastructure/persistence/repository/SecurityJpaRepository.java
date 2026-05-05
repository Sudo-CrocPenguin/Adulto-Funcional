package org.adultofuncional.main.security.infrastructure.persistence.repository;

import org.adultofuncional.main.security.infrastructure.persistence.entity.PasswordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para la entidad {@link PasswordEntity}.
 * <p>
 * Proporciona métodos de acceso a la tabla {@code passwords} sin exponer
 * la implementación concreta al dominio. Este repositorio es utilizado
 * por el adaptador {@link JpaSecurityRepositoryAdapter}.
 * </p>
 *
 * @author Daniel Salazar
 * @see PasswordEntity
 * @since 1.0
 */
public interface SecurityJpaRepository extends JpaRepository<PasswordEntity, UUID> {


    /**
     * Busca contraseñas por el ID de la cuenta.
     * @param accountId el ID de la cuenta (UUID)
     * @return lista de contraseñas de esa cuenta
     */
    List<PasswordEntity> findByAccount_IdAccount(UUID accountId);

    /**
     * Busca una contraseña por su ID
     * @param passwordId el ID de la contraseña
     * @return la entidad si existe
     */
    PasswordEntity findByPasswordId(UUID passwordId);
}