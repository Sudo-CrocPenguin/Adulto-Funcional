package org.adultofuncional.main.finances.infrastructure.repository;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.adultofuncional.main.finances.domain.enums.MovementType;
import org.adultofuncional.main.finances.domain.model.Movement;
import org.adultofuncional.main.finances.domain.repository.MovementRepository;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.MovementEntity;
import org.adultofuncional.main.finances.infrastructure.persistence.mapper.MovementMapper;
import org.adultofuncional.main.finances.infrastructure.persistence.repository.SpringMovementJpaRepository;
import org.adultofuncional.main.shared.pagination.PageQuery;
import org.adultofuncional.main.shared.pagination.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador concreto del puerto {@link MovementRepository}.
 *
 * <p>
 * Implementa las operaciones de persistencia de movimientos delegando en
 * {@link SpringMovementJpaRepository} (Spring Data JPA) y utilizando el
 * {@link MovementMapper} para convertir entre las entidades JPA
 * ({@link MovementEntity}) y el modelo de dominio ({@link Movement}).
 *
 * <p>
 * <strong>Métodos implementados:</strong>
 * <ul>
 * <li>{@link #findByIdAndAccountId(UUID, UUID)} — busca un movimiento por ID y
 * cuenta y lo convierte a dominio.</li>
 * <li>Consulta páginas filtradas y ordenadas dentro de una cuenta.</li>
 * <li>{@link #save(Movement)} — persiste un movimiento nuevo o actualizado,
 * devolviendo el modelo de dominio resultante.</li>
 * <li>{@link #deleteByIdAndAccountId(UUID, UUID)} — elimina un movimiento por
 * ID y cuenta.</li>
 * </ul>
 *
 * @author Lidys Jaraba
 * @since 0.0.1
 * @see MovementRepository
 * @see SpringMovementJpaRepository
 * @see MovementMapper
 */
@Repository
@RequiredArgsConstructor
public class MovementRepositoryImpl implements MovementRepository {

  private static final Map<String, String> SORT_FIELDS = Map.of(
      "movementDate", "movementDate",
      "amount", "movementAmount",
      "movementType", "movementType",
      "registerDate", "movementRegisterDate",
      "id", "movementId");

  private final SpringMovementJpaRepository jpaRepository;
  private final MovementMapper mapper;

  /**
   * Busca un movimiento por identificador y cuenta antes de materializarlo.
   */
  @Override
  public Optional<Movement> findByIdAndAccountId(UUID id, UUID accountId) {
    return jpaRepository.findByMovementIdAndAccount_AccountId(id, accountId)
        .map(mapper::toDomain);
  }

  /**
   * Consulta una página acotada de movimientos en persistencia.
   *
   * <p>
   * Traduce el campo lógico de orden a una propiedad JPA y añade el UUID como
   * desempate determinista. Los filtros y el {@code LIMIT/OFFSET} se ejecutan
   * en MariaDB.
   *
   * @param accountId UUID de la cuenta propietaria. No debe ser {@code null}.
   * @return lista de movimientos de la cuenta (vacía si no hay registros).
   */
  @Override
  public PageResult<Movement> findPageByAccountId(
      UUID accountId,
      LocalDate startDate,
      LocalDate endDate,
      MovementType movementType,
      UUID categoryId,
      String searchTerm,
      PageQuery pageQuery) {
    String entitySortField = SORT_FIELDS.get(pageQuery.sortBy());
    Sort.Direction direction = pageQuery.ascending() ? Sort.Direction.ASC : Sort.Direction.DESC;
    Sort sort = Sort.by(direction, entitySortField);
    if (!entitySortField.equals("movementId")) {
      sort = sort.and(Sort.by(direction, "movementId"));
    }
    PageRequest pageable = PageRequest.of(pageQuery.number(), pageQuery.size(), sort);
    Page<MovementEntity> page = jpaRepository.findPageByAccountId(
        accountId,
        startDate,
        endDate,
        movementType == null ? null : movementType.name(),
        categoryId,
        searchTerm,
        pageable);
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
   * Persiste un movimiento nuevo o actualiza uno existente.
   *
   * <p>
   * Convierte el modelo de dominio a entidad JPA con
   * {@link MovementMapper#toEntity(Movement)}, la guarda mediante Spring Data JPA
   * y vuelve a convertir el resultado a dominio para retornar la versión
   * persistida (incluyendo el ID si fue generado).
   *
   * @param movement el movimiento a guardar. No debe ser {@code null}.
   * @return el movimiento persistido como modelo de dominio.
   */
  @Override
  public Movement save(Movement movement) {
    MovementEntity entity = mapper.toEntity(movement);
    MovementEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  /**
   * Elimina un movimiento por identificador y cuenta en una sola sentencia.
   */
  @Override
  public boolean deleteByIdAndAccountId(UUID id, UUID accountId) {
    return jpaRepository.deleteByMovementIdAndAccountId(id, accountId) > 0;
  }
}
