package org.adultofuncional.main.finances.infrastructure.persistence.mapper;

import org.adultofuncional.main.finances.domain.model.FixedExpense;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.FixedExpensesEntity;
import org.springframework.stereotype.Component;

/**
 * Componente que convertirá entre las distintas representaciones de un gasto fijo.
 *
 * <p>
 * Traducirá entre:
 * <ul>
 * <li>{@link FixedExpensesEntity} (JPA) ↔ {@link FixedExpense} (dominio)</li>
 * </ul>
 *
 * <p>
 * La implementación está pendiente por inconsistencias entre la entidad
 * y el modelo de dominio:
 * <ul>
 * <li>El dominio requiere {@code startDate} pero la tabla
 * {@code fixed_expenses} no tiene esa columna.</li>
 * <li>El dominio requiere {@code createdAt} pero la tabla
 * {@code fixed_expenses} tampoco lo tiene.</li>
 * </ul>
 *
 * @author Lydis Ester Jaraba
 * @since 0.0.1
 * @see FixedExpense
 * @see FixedExpensesEntity
 */

@Component
public class FixedExpenseMapper {
    
    // TODO: implementar toDomain(), toEntity() y toResponse() una vez que
    //el equipo resuelva las columnas faltantes en la entidad y en
    //la migración de base de datos, o ajuste el modelo de dominio.
}
