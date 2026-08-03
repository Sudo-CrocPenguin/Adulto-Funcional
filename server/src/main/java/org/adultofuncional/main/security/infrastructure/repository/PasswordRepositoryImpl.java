package org.adultofuncional.main.security.infrastructure.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.adultofuncional.main.security.domain.model.Password;
import org.adultofuncional.main.security.domain.repository.PasswordRepository;
import org.adultofuncional.main.security.infrastructure.persistence.entity.PasswordEntity;
import org.adultofuncional.main.security.infrastructure.persistence.mapper.PasswordMapper;
import org.adultofuncional.main.security.infrastructure.persistence.repository.PasswordJpaRepository;
import org.adultofuncional.main.shared.pagination.PageQuery;
import org.adultofuncional.main.shared.pagination.PageResult;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador concreto del puerto {@link PasswordRepository}.
 *
 * <p>
 * Implementa las operaciones de persistencia de credenciales delegando en
 * {@link org.adultofuncional.main.security.infrastructure.persistence.repository.PasswordJpaRepository} (Spring Data JPA) y utilizando el
 * {@link PasswordMapper} para convertir entre las entidades JPA
 * ({@link PasswordEntity}) y el modelo de dominio ({@link Password}).
 *
 * <p>
 * <strong>Métodos implementados:</strong>
 * <ul>
 * <li>{@link #findByIdAndAccountId(UUID, UUID)} — busca una credencial por ID y
 * cuenta y la convierte a dominio.</li>
 * <li>{@link #findAllByAccountId(UUID)} — lectura interna para recifrado.</li>
 * <li>Listado público paginado, filtrado y ordenado en SQL.</li>
 * <li>{@link #save(Password)} — persiste una credencial nueva o actualizada,
 * devolviendo el modelo de dominio resultante.</li>
 * <li>{@link #deleteByIdAndAccountId(UUID, UUID)} — elimina una credencial por
 * ID y cuenta.</li>
 * </ul>
 *
 * @author Jeronimo Ospina Zapata
 * @since 0.0.1
 * @see PasswordRepository
 * @see PasswordJpaRepository
 * @see PasswordMapper
 */
@Repository
@RequiredArgsConstructor
public class PasswordRepositoryImpl implements PasswordRepository {

  private static final Map<String, String> SORT_FIELDS = Map.of(
      "applicationName", "passwordApplicationName",
      "lastChangeDate", "passwordLastChangeDate",
      "id", "passwordId");

  private final PasswordJpaRepository jpaRepository;
  private final PasswordMapper mapper;

  /**
   * Recupera la bóveda completa para el cambio transaccional de Master Key.
   *
   * <p>
   * Utiliza el método {@code findByAccount_AccountId} de Spring Data JPA
   * para recuperar las entidades y luego las convierte una a una al modelo
   * de dominio {@link Password} mediante el mapper.
   *
   * @param accountId UUID de la cuenta propietaria. No debe ser {@code null}.
   * @return lista de credenciales de la cuenta (vacía si no hay registros).
   */
  @Override
  public List<Password> findAllByAccountId(UUID accountId) {
    return jpaRepository.findByAccount_AccountId(accountId)
        .stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Override
  public PageResult<Password> findPageByAccountId(
      UUID accountId,
      String searchTerm,
      PageQuery pageQuery) {
    String entitySortField = SORT_FIELDS.get(pageQuery.sortBy());
    Sort.Direction direction = pageQuery.ascending() ? Sort.Direction.ASC : Sort.Direction.DESC;
    Sort sort = Sort.by(direction, entitySortField);
    if (!entitySortField.equals("passwordId")) {
      sort = sort.and(Sort.by(direction, "passwordId"));
    }
    PageRequest pageable = PageRequest.of(pageQuery.number(), pageQuery.size(), sort);
    Page<PasswordEntity> page = jpaRepository.findPageByAccountId(accountId, searchTerm, pageable);
    return new PageResult<>(
        page.getContent().stream().map(mapper::toDomain).toList(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.hasNext(),
        page.hasPrevious());
  }

  /**
   * Persiste una credencial nueva o actualiza una existente.
   *
   * <p>
   * Convierte el modelo de dominio a entidad JPA con
   * {@link PasswordMapper#toEntity(Password)}, la guarda mediante
   * Spring Data JPA y vuelve a convertir el resultado a dominio para retornar
   * la versión persistida.
   *
   * @param password la credencial a guardar. No debe ser {@code null}.
   * @return la credencial persistida como modelo de dominio.
   */
  @Override
  public Password save(Password password) {
    PasswordEntity entity = mapper.toEntity(password);
    PasswordEntity savedEntity = jpaRepository.save(entity);
    return mapper.toDomain(savedEntity);
  }

    /**
   * Busca una credencial por su identificador y la cuenta propietaria.
   *
   * @param passwordId UUID de la credencial. No debe ser {@code null}.
   * @param accountId  UUID de la cuenta propietaria. No debe ser {@code null}.
   * @return {@link Optional} con la credencial si existe y pertenece a la cuenta;
   *         {@code Optional.empty()} en caso contrario.
   */
  @Override
  public Optional<Password> findByIdAndAccountId(UUID passwordId, UUID accountId) {
      return jpaRepository.findByPasswordIdAndAccount_AccountId(passwordId, accountId)
              .map(mapper::toDomain);
  }

  /**
   * Elimina una credencial por identificador y cuenta en una sola sentencia.
   */
  @Override
  public boolean deleteByIdAndAccountId(UUID passwordId, UUID accountId) {
    return jpaRepository.deleteByPasswordIdAndAccountId(passwordId, accountId) > 0;
  }

  /**
   * Verifica si existe una credencial para una cuenta y aplicación específicas.
   *
   * @param accountId       UUID de la cuenta propietaria. No debe ser {@code null}.
   * @param applicationName nombre de la aplicación. No debe ser {@code null}.
   * @return {@code true} si ya existe una credencial para esa aplicación en esa cuenta.
   */
  @Override
  public boolean existsByAccountIdAndApplicationName(UUID accountId, String applicationName) {
      return jpaRepository.existsByAccount_AccountIdAndPasswordApplicationName(accountId, applicationName);
  }

}
