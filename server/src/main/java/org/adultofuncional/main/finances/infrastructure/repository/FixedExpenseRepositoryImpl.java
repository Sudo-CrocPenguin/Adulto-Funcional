package org.adultofuncional.main.finances.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.adultofuncional.main.finances.domain.model.FixedExpense;
import org.adultofuncional.main.finances.domain.repository.FixedExpenseRepository;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.FixedExpensesEntity;
import org.adultofuncional.main.finances.infrastructure.persistence.mapper.FixedExpenseMapper;
import org.adultofuncional.main.finances.infrastructure.persistence.repository.SpringFixedExpenseJpaRepository;
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
 * <li>{@link #findById(UUID)} — busca un gasto fijo por ID y lo convierte
 * a dominio.</li>
 * <li>{@link #findAllByAccountId(UUID)} — lista todos los gastos fijos
 * asociados a una cuenta.</li>
 * <li>{@link #save(FixedExpense)} — persiste un gasto fijo nuevo o actualizado,
 * devolviendo el modelo de dominio resultante.</li>
 * <li>{@link #deleteById(UUID)} — elimina un gasto fijo por su ID.</li>
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

  private final SpringFixedExpenseJpaRepository fixedExpenseJpaRepository;
  private final FixedExpenseMapper fixedExpenseMapper;

  /**
   * Busca un gasto fijo por su identificador único.
   *
   * <p>
   * Consulta el repositorio Spring Data JPA y convierte la entidad resultante
   * al modelo de dominio mediante
   * {@link FixedExpenseMapper#toDomain(FixedExpensesEntity)}.
   *
   * @param id UUID del gasto fijo. No debe ser {@code null}.
   * @return {@link Optional} con el gasto fijo si existe;
   *         {@code Optional.empty()} en caso contrario.
   */
  @Override
  public Optional<FixedExpense> findById(UUID id) {
    return fixedExpenseJpaRepository.findById(id).map(fixedExpenseMapper::toDomain);
  }

  /**
   * Lista todos los gastos fijos asociados a una cuenta específica.
   *
   * <p>
   * Utiliza el método {@code findByAccount_AccountId} de Spring Data JPA
   * para recuperar las entidades y luego las convierte una a una al modelo
   * de dominio {@link FixedExpense} mediante el mapper.
   *
   * @param accountId UUID de la cuenta propietaria. No debe ser {@code null}.
   * @return lista de gastos fijos de la cuenta (vacía si no hay registros).
   */
  @Override
  public List<FixedExpense> findAllByAccountId(UUID accountId) {
    return fixedExpenseJpaRepository.findByAccount_AccountId(accountId)
        .stream().map(fixedExpenseMapper::toDomain).toList();
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
   * Elimina un gasto fijo por su identificador único.
   *
   * <p>
   * Si no existe ningún gasto fijo con el ID dado, la operación no tiene efecto
   * (comportamiento silencioso de Spring Data JPA). La validación de existencia
   * previa se realiza en la capa de aplicación.
   *
   * @param id UUID del gasto fijo a eliminar. No debe ser {@code null}.
   */
  @Override
  public void deleteById(UUID id) {
    fixedExpenseJpaRepository.deleteById(id);
  }
}
