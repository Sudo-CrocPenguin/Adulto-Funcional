package org.adultofuncional.main.agenda.domain.repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.adultofuncional.main.agenda.domain.model.Event;
import org.adultofuncional.main.shared.pagination.PageQuery;
import org.adultofuncional.main.shared.pagination.PageResult;

/**
 * Puerto de dominio para la persistencia de eventos de agenda.
 *
 * <p>
 * Define las operaciones de acceso a datos que los casos de uso requieren
 * sobre la entidad {@link Event}. La implementación concreta reside en la capa
 * de infraestructura (adaptador JPA) y se inyecta en tiempo de ejecución,
 * manteniendo el dominio desacoplado de los detalles de almacenamiento.
 *
 * <p>
 * <strong>Operaciones expuestas:</strong>
 * <ul>
 * <li>Búsqueda por ID y cuenta propietaria (validación de propiedad).</li>
 * <li>Listado de todos los eventos de una cuenta.</li>
 * <li>Persistencia de nuevos eventos o actualización de existentes.</li>
 * <li>Eliminación limitada por ID y cuenta propietaria.</li>
 * </ul>
 *
 * @author Daniel Salazar
 * @since 1.0
 * @see Event
 * @see org.adultofuncional.main.agenda.infrastructure.repository.EventRepositoryImpl
 */
public interface EventRepository {

  /**
   * Busca un evento por su identificador y la cuenta propietaria.
   *
   * <p>
   * Utilizado para garantizar que un evento pertenece a la cuenta que
   * intenta acceder a él, evitando fugas de información entre cuentas.
   *
   * @param eventId   UUID del evento. No debe ser {@code null}.
   * @param accountId UUID de la cuenta propietaria. No debe ser {@code null}.
   * @return {@link Optional} con el evento si existe y pertenece a la cuenta;
   *         {@code Optional.empty()} en caso contrario.
   */
  Optional<Event> findByIdAndAccountId(UUID eventId, UUID accountId);

  /**
   * Consulta una página de eventos de una cuenta específica.
   *
   * <p>
   * Ownership, rango, filtros, orden y límite se ejecutan en persistencia para
   * no materializar la agenda completa.
   *
   * @param accountId UUID de la cuenta propietaria. No debe ser {@code null}.
   * @return página de eventos y sus totales.
   */
  PageResult<Event> findPageByAccountId(
      UUID accountId,
      String status,
      String priority,
      UUID categoryId,
      LocalDate startDate,
      LocalDate endDate,
      PageQuery pageQuery);

  /**
   * Persiste un evento nuevo o actualiza uno existente.
   *
   * <p>
   * Si el evento no tiene un ID asignado previamente, el repositorio lo
   * insertará como nuevo registro. Si ya existe, lo actualizará.
   *
   * @param event el evento a guardar. No debe ser {@code null}.
   * @return el evento persistido con su estado final (incluyendo el ID si
   *         fue generado).
   */
  Event save(Event event);

  /**
   * Elimina un evento únicamente cuando pertenece a la cuenta indicada.
   *
   * @param eventId   UUID del evento
   * @param accountId UUID de la cuenta propietaria
   * @return {@code true} cuando se eliminó una fila
   */
  boolean deleteByIdAndAccountId(UUID eventId, UUID accountId);
}
