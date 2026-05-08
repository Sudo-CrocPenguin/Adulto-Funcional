package org.adultofuncional.main.agenda.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto del repositorio para operaciones de agenda (eventos).
 * <p>
 * Define métodos básicos para gestionar eventos del calendario.
 * Por ahora, trabaja con identificadores UUID para mantener simplicidad.
 * </p>
 *
 * @author Daniel Salazar
 * @since 1.0
 */
public interface AgendaRepository {

  /**
   * Busca un evento por su identificador.
   *
   * @param id el UUID del evento
   * @return un Optional con el UUID si existe, vacío si no
   */
  Optional<UUID> findById(UUID id);

  /**
   * Busca todos los eventos asociados a una cuenta.
   *
   * @param accountId el UUID de la cuenta
   * @return lista de UUIDs de eventos (puede estar vacía)
   */
  List<UUID> findAllByAccountId(UUID accountId);

  /**
   * Guarda un nuevo evento (solo devuelve su UUID).
   *
   * @param event el objeto evento (temporal, mientras no hay modelo de dominio)
   * @return el UUID generado
   */
  UUID save(Object event);

  /**
   * Elimina un evento por su identificador.
   *
   * @param id el UUID del evento
   */
  void deleteById(UUID id);
}
