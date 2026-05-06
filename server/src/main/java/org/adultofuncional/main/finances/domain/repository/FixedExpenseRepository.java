package org.adultofuncional.main.finances.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.adultofuncional.main.finances.domain.model.FixedExpense;


/**
 * Puerto del repositorio para gastos fijos.
 * Define métodos básicos que los casos de uso necesitan.
 *
 * @author Daniel Salazar
 * @since 1.0
 */

public interface FixedExpenseRepository {

        /**
     * Busca un gasto fijo por su identificador.
     * @param id el UUID del gasto fijo
     * @return Optional con el gasto fijo si existe
     */
    Optional<FixedExpense> findById(UUID id);

    /**
     * Lista todos los gastos fijos de una cuenta específica.
     * @param accountId el UUID de la cuenta propietaria
     * @return lista de gastos fijos de esa cuenta
     */
    List<FixedExpense> findAllByAccountId(UUID accountId);

    /**
     * Guarda un gasto fijo (nuevo o actualizado).
     * @param fixedExpense el gasto fijo a guardar
     * @return el gasto fijo guardado
     */
    FixedExpense save(FixedExpense fixedExpense);

    /**
     * Elimina un gasto fijo por su ID.
     * @param id el UUID del gasto fijo
     */
    void deleteById(UUID id);
}
