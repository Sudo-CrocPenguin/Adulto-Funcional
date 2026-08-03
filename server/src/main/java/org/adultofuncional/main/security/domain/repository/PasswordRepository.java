package org.adultofuncional.main.security.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.adultofuncional.main.security.domain.model.Password;
import org.adultofuncional.main.shared.pagination.PageQuery;
import org.adultofuncional.main.shared.pagination.PageResult;

/**
 * Puerto de dominio para la persistencia de credenciales almacenadas.
 *
 * <p>
 * Define las operaciones de acceso a datos que los casos de uso requieren
 * sobre la entidad {@link Password}. La implementación concreta reside en la
 * capa de infraestructura (adaptador JPA) y se inyecta en tiempo de ejecución,
 * manteniendo el dominio desacoplado de los detalles de almacenamiento.
 *
 * <p>
 * <strong>Operaciones expuestas:</strong>
 * <ul>
 * <li>Búsqueda individual limitada por ID y cuenta propietaria.</li>
 * <li>Listado de todas las credenciales de una cuenta.</li>
 * <li>Persistencia de nuevas credenciales o actualización de existentes.</li>
 * <li>Eliminación limitada por ID y cuenta propietaria.</li>
 * </ul>
 *
 * @author Daniel Salazar, Juan Sebastian Rios
 * @since 1.0
 * @see Password
 * @see org.adultofuncional.main.security.infrastructure.repository.PasswordRepositoryImpl
 */
public interface PasswordRepository {

  /**
   * Lista todas las credenciales para el recifrado transaccional de Master Key.
   *
   * @param accountId UUID de la cuenta propietaria. No debe ser {@code null}.
   * @return bóveda completa; no debe usarse desde endpoints de listado.
   */
  List<Password> findAllByAccountId(UUID accountId);

  /** Página pública acotada; la lectura completa se reserva para recifrado. */
  PageResult<Password> findPageByAccountId(
      UUID accountId,
      String searchTerm,
      PageQuery pageQuery);

  /**
   * Persiste una credencial nueva o actualiza una existente.
   *
   * <p>
   * Si la credencial no tiene un ID asignado previamente, el repositorio la
   * insertará como nuevo registro. Si ya existe, la actualizará.
   *
   * @param password la credencial a guardar. No debe ser {@code null}.
   * @return la credencial persistida con su estado final.
   */
  Password save(Password password);

  /**
   * Busca una credencial por su identificador y la cuenta propietaria.
   *
   * @param passwordId UUID de la credencial. No debe ser {@code null}.
   * @param accountId  UUID de la cuenta propietaria. No debe ser {@code null}.
   * @return {@link Optional} con la credencial si existe y pertenece a la cuenta;
   *         {@code Optional.empty()} en caso contrario.
   */
  Optional<Password> findByIdAndAccountId(UUID passwordId, UUID accountId);

  /**
   * Elimina una credencial únicamente cuando pertenece a la cuenta indicada.
   *
   * @param passwordId UUID de la credencial
   * @param accountId  UUID de la cuenta propietaria
   * @return {@code true} cuando se eliminó una fila
   */
  boolean deleteByIdAndAccountId(UUID passwordId, UUID accountId);

  /**
   * Verifica si existe una credencial para una cuenta y aplicación específicas.
   *
   * @param accountId       UUID de la cuenta propietaria. No debe ser {@code null}.
   * @param applicationName nombre de la aplicación. No debe ser {@code null}.
   * @return {@code true} si ya existe una credencial para esa aplicación en esa cuenta.
   */
  boolean existsByAccountIdAndApplicationName(UUID accountId, String applicationName);
  
}
