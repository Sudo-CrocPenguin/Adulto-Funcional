package org.adultofuncional.main.finances.infrastructure.persistence.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.adultofuncional.main.finances.infrastructure.persistence.entity.FixedExpensesEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

/**
 * Repositorio Spring Data JPA para la entidad {@link FixedExpensesEntity}.
 * <p>
 * Proporciona métodos de acceso a la tabla {@code fixed_expenses} sin exponer
 * la implementación concreta al dominio. Este repositorio es utilizado
 * por el adaptador correspondiente en la capa de infraestructura.
 * </p>
 *
 * @author Daniel Salazar
 * @see FixedExpensesEntity
 * @since 1.0
 */
public interface SpringFixedExpenseJpaRepository extends JpaRepository<FixedExpensesEntity, UUID> {

  /**
   * Busca un gasto fijo limitando la consulta por identificador y propietario.
   *
   * @param fixedExpenseId identificador del gasto fijo
   * @param accountId      identificador de la cuenta propietaria
   * @return entidad únicamente cuando ambos identificadores coinciden
   */
  Optional<FixedExpensesEntity> findByFixedExpenseIdAndAccount_AccountId(
      UUID fixedExpenseId,
      UUID accountId);

  /**
   * Elimina de forma atómica un gasto fijo limitado por cuenta propietaria.
   *
   * @param fixedExpenseId identificador del gasto fijo
   * @param accountId      identificador de la cuenta propietaria
   * @return cantidad de filas eliminadas
   */
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(value = """
      DELETE FROM fixed_expenses
      WHERE fixed_expense_id = :fixedExpenseId
        AND fixed_expense_fk_account_id = :accountId
      """, nativeQuery = true)
  int deleteByFixedExpenseIdAndAccountId(
      @Param("fixedExpenseId") UUID fixedExpenseId,
      @Param("accountId") UUID accountId);

  /**
   * Busca todos los gastos fijos asociados a una cuenta específica.
   *
   * @param accountId el identificador de la cuenta (UUID)
   * @return lista de entidades {@code FixedExpensesEntity} de esa cuenta,
   *         puede estar vacía si no hay gastos fijos registrados
   */
  @EntityGraph(attributePaths = {"account", "category"})
  @Query("""
      SELECT expense
      FROM FixedExpensesEntity expense
      WHERE expense.account.accountId = :accountId
        AND (:status IS NULL OR expense.fixedExpenseStatus = :status)
        AND (:categoryId IS NULL OR expense.category.categoryId = :categoryId)
        AND (:searchTerm IS NULL OR LOWER(expense.fixedExpenseName)
             LIKE LOWER(CONCAT('%', :searchTerm, '%')))
      """)
  Page<FixedExpensesEntity> findPageByAccountId(
      @Param("accountId") UUID accountId,
      @Param("status") String status,
      @Param("categoryId") UUID categoryId,
      @Param("searchTerm") String searchTerm,
      Pageable pageable);

  /** Selecciona y bloquea un lote vencido para evitar doble avance concurrente. */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @EntityGraph(attributePaths = {"account", "category"})
  @Query("""
      SELECT expense
      FROM FixedExpensesEntity expense
      WHERE expense.fixedExpenseStatus = 'ACTIVE'
        AND expense.fixedExpenseNextDueDate <= :cutoff
      ORDER BY expense.fixedExpenseNextDueDate ASC, expense.fixedExpenseId ASC
      """)
  List<FixedExpensesEntity> findDueForUpdate(
      @Param("cutoff") LocalDate cutoff,
      Pageable pageable);
}
