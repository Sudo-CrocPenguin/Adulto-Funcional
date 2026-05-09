package org.adultofuncional.main.security.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
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
     * Verifica si existe una credencial con el ID dado que pertenezca a la cuenta.
     *
     * <p>
     * Usado antes de operaciones de eliminación para validar pertenencia
     * sin cargar la entidad completa.
     *
     * @param passwordId UUID de la credencial.
     * @param accountId  UUID de la cuenta propietaria.
     * @return {@code true} si la credencial existe y pertenece a la cuenta.
     */
  boolean existsByPasswordIdAndAccount_AccountId(UUID passwordId, UUID accountId);

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
