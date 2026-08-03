package org.adultofuncional.main.security.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.adultofuncional.main.security.infrastructure.persistence.entity.PasswordEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
   * Busca la bóveda completa exclusivamente para operaciones de recifrado.
   *
   * @param accountId UUID de la cuenta propietaria.
   * @return lista de entidades {@code PasswordEntity} de esa cuenta;
   *         puede estar vacía si no hay contraseñas registradas.
   */
  List<PasswordEntity> findByAccount_AccountId(UUID accountId);

  /** Consulta el listado público con ownership, búsqueda y límites en SQL. */
  @EntityGraph(attributePaths = "account")
  @Query("""
      SELECT credential
      FROM PasswordEntity credential
      WHERE credential.account.accountId = :accountId
        AND (:searchTerm IS NULL OR LOWER(credential.passwordApplicationName)
             LIKE LOWER(CONCAT('%', :searchTerm, '%')))
      """)
  Page<PasswordEntity> findPageByAccountId(
      @Param("accountId") UUID accountId,
      @Param("searchTerm") String searchTerm,
      Pageable pageable);

    /**
     * Busca una credencial por su identificador y la cuenta propietaria.
     *
     * <p>
     * Garantiza que la credencial pertenezca a la cuenta autenticada,
     * evitando acceso cruzado entre cuentas.
     *
     * @param passwordId UUID de la credencial.
     * @param accountId  UUID de la cuenta propietaria.
     * @return {@link Optional} con la entidad si existe y pertenece a la cuenta;
     *         {@code Optional.empty()} en caso contrario.
     */
  Optional<PasswordEntity> findByPasswordIdAndAccount_AccountId(UUID passwordId, UUID accountId);

  /**
   * Elimina de forma atómica una credencial limitada por cuenta propietaria.
   */
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(value = """
      DELETE FROM passwords
      WHERE password_id = :passwordId
        AND passwords_fk_account_id = :accountId
      """, nativeQuery = true)
  int deleteByPasswordIdAndAccountId(
      @Param("passwordId") UUID passwordId,
      @Param("accountId") UUID accountId);

  /**
     * Verifica si ya existe una credencial para una cuenta y aplicación específicas.
     *
     * <p>
     * Usado en {@code CreatePasswordUseCase} para evitar nombres de aplicación
     * duplicados dentro de la misma cuenta.
     *
     * @param accountId           UUID de la cuenta propietaria.
     * @param passwordApplicationName nombre de la aplicación a verificar.
     * @return {@code true} si ya existe una credencial con ese nombre en esa cuenta.
     */
  boolean existsByAccount_AccountIdAndPasswordApplicationName(UUID accountId, String passwordApplicationName);
    
}
