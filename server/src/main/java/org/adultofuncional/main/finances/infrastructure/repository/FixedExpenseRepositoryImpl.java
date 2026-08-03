package org.adultofuncional.main.finances.infrastructure.repository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.adultofuncional.main.finances.domain.enums.Status;
import org.adultofuncional.main.finances.domain.model.FixedExpense;
import org.adultofuncional.main.finances.domain.repository.FixedExpenseRepository;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.FixedExpensesEntity;
import org.adultofuncional.main.finances.infrastructure.persistence.mapper.FixedExpenseMapper;
import org.adultofuncional.main.finances.infrastructure.persistence.repository.SpringFixedExpenseJpaRepository;
import org.adultofuncional.main.shared.pagination.PageQuery;
import org.adultofuncional.main.shared.pagination.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador concreto del puerto {@link FixedExpenseRepository}.
 *
 * <p>
 * Implementa las operaciones de persistencia de gastos fijos delegando en
 * {@link SpringFixedExpenseJpaRepository} (Spring Data JPA) y utilizando el
 * {@link FixedExpenseMapper} para convertir entre las entidades JPA
 * ({@link FixedExpensesEntity}) y el modelo de dominio ({@link FixedExpense}).
 *
 * <p>
 * <strong>Métodos implementados:</strong>
 * <ul>
 * <li>{@link #findByIdAndAccountId(UUID, UUID)} — busca un gasto fijo por ID y
 * cuenta y lo convierte a dominio.</li>
 * <li>Consulta páginas filtradas y ordenadas dentro de una cuenta.</li>
 * <li>{@link #save(FixedExpense)} — persiste un gasto fijo nuevo o actualizado,
 * devolviendo el modelo de dominio resultante.</li>
 * <li>{@link #deleteByIdAndAccountId(UUID, UUID)} — elimina un gasto fijo por
 * ID y cuenta propietaria.</li>
 * </ul>
 *
 * @author Juan Sebastian Rios
 * @since 1.0
 * @see FixedExpenseRepository
 * @see SpringFixedExpenseJpaRepository
 * @see FixedExpenseMapper
 */
@Repository
@RequiredArgsConstructor
public class FixedExpenseRepositoryImpl implements FixedExpenseRepository {

  private static final Map<String, String> SORT_FIELDS = Map.of(
      "nextDueDate", "fixedExpenseNextDueDate",
      "amount", "fixedExpenseAmount",
      "name", "fixedExpenseName",
      "status", "fixedExpenseStatus",
      "frequency", "fixedExpenseFrequency",
      "id", "fixedExpenseId");

  private final SpringFixedExpenseJpaRepository fixedExpenseJpaRepository;
  private final FixedExpenseMapper fixedExpenseMapper;

  /**
   * Busca un gasto fijo por identificador y cuenta propietaria en una única
   * consulta, antes de materializar el modelo de dominio.
   *
   * @param id        UUID del gasto fijo
   * @param accountId UUID de la cuenta propietaria
   * @return gasto fijo cuando ambos identificadores coinciden
   */
  @Override
  public Optional<FixedExpense> findByIdAndAccountId(UUID id, UUID accountId) {
    return fixedExpenseJpaRepository
        .findByFixedExpenseIdAndAccount_AccountId(id, accountId)
        .map(fixedExpenseMapper::toDomain);
  }

  /**
   * Consulta una página acotada de gastos fijos.
   *
   * <p>
   * Traduce el campo lógico a la propiedad JPA, añade el UUID como desempate
   * y aplica filtros y límites directamente en MariaDB.
   *
   * @param accountId UUID de la cuenta propietaria. No debe ser {@code null}.
   * @return lista de gastos fijos de la cuenta (vacía si no hay registros).
   */
  @Override
  public PageResult<FixedExpense> findPageByAccountId(
      UUID accountId,
      Status status,
      UUID categoryId,
      String searchTerm,
      PageQuery pageQuery) {
    String entitySortField = SORT_FIELDS.get(pageQuery.sortBy());
    Sort.Direction direction = pageQuery.ascending() ? Sort.Direction.ASC : Sort.Direction.DESC;
    Sort sort = Sort.by(direction, entitySortField);
    if (!entitySortField.equals("fixedExpenseId")) {
      sort = sort.and(Sort.by(direction, "fixedExpenseId"));
    }
    PageRequest pageable = PageRequest.of(pageQuery.number(), pageQuery.size(), sort);
    Page<FixedExpensesEntity> page = fixedExpenseJpaRepository.findPageByAccountId(
        accountId,
        status == null ? null : status.name(),
        categoryId,
        searchTerm,
        pageable);
    return new PageResult<>(
        page.getContent().stream().map(fixedExpenseMapper::toDomain).toList(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.hasNext(),
        page.hasPrevious());
  }

  /**
   * Persiste un gasto fijo nuevo o actualiza uno existente.
   *
   * <p>
   * Convierte el modelo de dominio a entidad JPA con
   * {@link FixedExpenseMapper#toEntity(FixedExpense)}, la guarda mediante
   * Spring Data JPA y vuelve a convertir el resultado a dominio para retornar
   * la versión persistida (incluyendo el ID si fue generado).
   *
   * @param fixedExpense el gasto fijo a guardar. No debe ser {@code null}.
   * @return el gasto fijo persistido como modelo de dominio.
   */
  @Override
  public FixedExpense save(FixedExpense fixedExpense) {
    FixedExpensesEntity entity = fixedExpenseMapper.toEntity(fixedExpense);
    FixedExpensesEntity saved = fixedExpenseJpaRepository.save(entity);
    return fixedExpenseMapper.toDomain(saved);
  }

  /**
   * Elimina un gasto fijo por identificador y cuenta en una sola sentencia.
   *
   * @param id        UUID del gasto fijo
   * @param accountId UUID de la cuenta propietaria
   * @return {@code true} cuando la base de datos eliminó una fila
   */
  @Override
  public boolean deleteByIdAndAccountId(UUID id, UUID accountId) {
    return fixedExpenseJpaRepository
        .deleteByFixedExpenseIdAndAccountId(id, accountId) > 0;
  }
}
