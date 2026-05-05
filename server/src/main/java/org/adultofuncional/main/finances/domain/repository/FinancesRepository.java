package org.adultofuncional.main.finances.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto del repositorio para operaciones financieras.
 * <p>
 * Define métodos básicos para gestionar movimientos, categorías o gastos fijos.
 * Por ahora, trabaja con identificadores UUID para mantener simplicidad.
 * </p>
 *
 * @author Adulto Funcional Team
 * @since 1.0
 */
public interface FinancesRepository {

    /**
     * Busca un movimiento financiero por su identificador.
     *
     * @param id el UUID del movimiento
     * @return un Optional con el UUID si existe, vacío si no
     */
    Optional<UUID> findById(UUID id);

    /**
     * Busca todos los movimientos asociados a una cuenta.
     *
     * @param accountId el UUID de la cuenta
     * @return lista de UUIDs de movimientos (puede estar vacía)
     */
    List<UUID> findAllByAccountId(UUID accountId);

    /**
     * Guarda un nuevo movimiento (solo devuelve su UUID).
     *
     * @param movement el objeto movimiento (aún sin modelo, pero se acepta un Map o similar)
     * @return el UUID generado
     */
    UUID save(Object movement);  // Temporal, mientras no hay modelo de dominio

    /**
     * Elimina un movimiento por su identificador.
     *
     * @param id el UUID del movimiento
     */
    void deleteById(UUID id);
}