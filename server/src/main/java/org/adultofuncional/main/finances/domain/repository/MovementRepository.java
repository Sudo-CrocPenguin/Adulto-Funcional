package org.adultofuncional.main.finances.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.adultofuncional.main.finances.domain.model.Movement;

/**
 * Puerto del repositorio para movimientos.
 * Define métodos básicos que los casos de uso necesitan.
 *
 * @author Daniel Salazar
 * @since 1.0
 */
public interface MovementRepository {

    /**
     * Busca un movimiento por su identificador.
     * @param id el UUID del movimiento
     * @return Optional con el movimiento si existe
     */
    Optional<Movement> findById(UUID id);

    /**
     * Lista todos los movimientos de una cuenta específica.
     * @param accountId el UUID de la cuenta propietaria
     * @return lista de movimientos de esa cuenta
     */
    List<Movement> findAllByAccountId(UUID accountId);

    /**
     * Guarda un movimiento (nuevo o actualizado).
     * @param movement el movimiento a guardar
     * @return el movimiento guardado
     */
    Movement save(Movement movement);

    /**
     * Elimina un movimiento por su ID.
     * @param id el UUID del movimiento
     */
    void deleteById(UUID id);
}