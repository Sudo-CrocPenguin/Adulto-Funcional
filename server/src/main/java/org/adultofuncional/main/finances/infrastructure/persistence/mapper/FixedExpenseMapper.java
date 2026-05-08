package org.adultofuncional.main.finances.infrastructure.persistence.mapper;

import java.util.UUID;

import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
import org.adultofuncional.main.finances.domain.enums.Frequency;
import org.adultofuncional.main.finances.domain.enums.Status;
import org.adultofuncional.main.finances.domain.model.FixedExpense;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.CategoryEntity;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.FixedExpensesEntity;
import org.springframework.stereotype.Component;

/**
 * Componente que convierte entre las distintas representaciones de un gasto
 * fijo.
 *
 * <p>
 * Traduce entre:
 * <ul>
 * <li>{@link FixedExpensesEntity} (JPA) ↔ {@link FixedExpense} (dominio)</li>
 * </ul>
 *
 * <p>
 * El modelo de dominio {@link FixedExpense} ya contiene el {@code accountId},
 * por lo que {@link #toEntity(FixedExpense)} lo obtiene directamente del
 * propio modelo para construir la referencia JPA.
 *
 * @author Lidys Jaraba
 * @since 0.0.1
 * @see FixedExpense
 * @see FixedExpensesEntity
 */
@Component
public class FixedExpenseMapper {

  /**
   * Convierte una {@link FixedExpensesEntity} al modelo de dominio
   * {@link FixedExpense}.
   *
   * <p>
   * Usa el método de fábrica {@code FixedExpense.reconstitute()} para respetar
   * el constructor privado del modelo de dominio.
   *
   * <p>
   * Si la categoría es {@code null} (opcional), el {@code categoryId}
   * se establece como {@code null} en el dominio.
   *
   * @param entity entidad JPA; si es {@code null} retorna {@code null}
   * @return modelo de dominio reconstituido o {@code null}
   */
  public FixedExpense toDomain(FixedExpensesEntity entity) {
    if (entity == null)
      return null;

    UUID categoryId = entity.getCategory() != null
        ? entity.getCategory().getCategoryId()
        : null;
    UUID accountId = entity.getAccount() != null
        ? entity.getAccount().getAccountId()
        : null;

    return FixedExpense.reconstitute(
        entity.getFixedExpenseId(),
        entity.getFixedExpenseName(),
        entity.getFixedExpenseAmount(),
        categoryId,
        accountId, // ← antes faltaba
        Frequency.valueOf(entity.getFixedExpenseFrequency()),
        Status.valueOf(entity.getFixedExpenseStatus()),
        entity.getFixedExpenseStartDate(),
        entity.getFixedExpenseNextDueDate(),
        entity.getFixedExpenseReminderDays());
  }

  /**
   * Convierte el modelo de dominio {@link FixedExpense} a
   * {@link FixedExpensesEntity}.
   *
   * <p>
   * Obtiene el {@code accountId} directamente del modelo de dominio
    * {@link FixedExpense} para construir la referencia JPA.
   * Se crean referencias JPA con solo el ID para {@code account} y
   * {@code category}, suficiente para que Hibernate resuelva las FK al
   * persistir.
   *
   * <p>
   * El campo {@code fixedExpenseFrequency} y {@code fixedExpenseStatus} se
   * almacenan usando {@code name()} del enum, que coincide con los valores
   * permitidos en la base de datos.
   *
   * @param fixedExpense modelo de dominio; si es {@code null} retorna
   *                     {@code null}
   * @return entidad JPA lista para persistir
   */
  public FixedExpensesEntity toEntity(FixedExpense fixedExpense) {
    if (fixedExpense == null)
      return null;

    FixedExpensesEntity entity = new FixedExpensesEntity();
    entity.setFixedExpenseId(fixedExpense.getId());
    entity.setFixedExpenseName(fixedExpense.getName());
    entity.setFixedExpenseAmount(fixedExpense.getAmount());
    entity.setFixedExpenseFrequency(fixedExpense.getFrequency().name());
    entity.setFixedExpenseStatus(fixedExpense.getStatus().name());
    entity.setFixedExpenseStartDate(fixedExpense.getStartDate());
    entity.setFixedExpenseNextDueDate(fixedExpense.getNextDueDate());
    entity.setFixedExpenseReminderDays(fixedExpense.getReminderDays());

    if (fixedExpense.getCategoryId() != null) {
      CategoryEntity categoryRef = new CategoryEntity();
      categoryRef.setCategoryId(fixedExpense.getCategoryId());
      entity.setCategory(categoryRef);
    }

    AccountEntity accountRef = new AccountEntity();
    accountRef.setAccountId(fixedExpense.getAccountId()); // ← usa el dominio
    entity.setAccount(accountRef);

    return entity;
  }
}
