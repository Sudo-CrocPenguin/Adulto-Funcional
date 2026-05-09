package org.adultofuncional.main.security.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.adultofuncional.main.security.domain.model.Password;

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
 * <li>Búsqueda individual por ID.</li>
 * <li>Listado de todas las credenciales de una cuenta.</li>
 * <li>Persistencia de nuevas credenciales o actualización de existentes.</li>
 * <li>Eliminación por ID.</li>
 * </ul>
 *
 * @author Daniel Salazar, Juan Sebastian Rios
 * @since 1.0
 * @see Password
 * @see org.adultofuncional.main.security.infrastructure.repository.PasswordRepositoryImpl
 */
public interface PasswordRepository {

  /**
   * Busca una credencial por su identificador único.
   *
   * @param id UUID de la credencial. No debe ser {@code null}.
   * @return {@link Optional} con la credencial si existe;
   *         {@code Optional.empty()} en caso contrario.
   */
  Optional<Password> findById(UUID id);

  /**
   * Lista todas las credenciales asociadas a una cuenta específica.
   *
   * @param accountId UUID de la cuenta propietaria. No debe ser {@code null}.
   * @return lista de credenciales de la cuenta (vacía si no hay registros).
   */
  List<Password> findAllByAccountId(UUID accountId);

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
   * Elimina una credencial por su identificador único.
   *
   * <p>
   * Si no existe una credencial con el ID dado, la operación no tiene efecto
   * (comportamiento silencioso). La validación de existencia previa se
   * realiza en la capa de aplicación.
   *
   * @param id UUID de la credencial a eliminar. No debe ser {@code null}.
   */
  void deleteById(UUID id);


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
   * Verifica si existe una credencial con el ID dado que pertenezca a la cuenta.
   *
   * @param passwordId UUID de la credencial. No debe ser {@code null}.
   * @param accountId  UUID de la cuenta propietaria. No debe ser {@code null}.
   * @return {@code true} si la credencial existe y pertenece a la cuenta.
   */
  boolean existsByIdAndAccountId(UUID passwordId, UUID accountId);

  /**
   * Verifica si existe una credencial para una cuenta y aplicación específicas.
   *
   * @param accountId       UUID de la cuenta propietaria. No debe ser {@code null}.
   * @param applicationName nombre de la aplicación. No debe ser {@code null}.
   * @return {@code true} si ya existe una credencial para esa aplicación en esa cuenta.
   */
  boolean existsByAccountIdAndApplicationName(UUID accountId, String applicationName);
  
}
