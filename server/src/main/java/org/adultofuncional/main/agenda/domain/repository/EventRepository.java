package org.adultofuncional.main.agenda.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.adultofuncional.main.agenda.domain.model.Event;

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
 * <li>Búsqueda individual por ID.</li>
 * <li>Búsqueda por ID y cuenta propietaria (validación de propiedad).</li>
 * <li>Verificación de existencia por ID y cuenta.</li>
 * <li>Listado de todos los eventos de una cuenta.</li>
 * <li>Persistencia de nuevos eventos o actualización de existentes.</li>
 * <li>Eliminación por ID.</li>
 * </ul>
 *
 * @author Daniel Salazar
 * @since 1.0
 * @see Event
 * @see org.adultofuncional.main.agenda.infrastructure.repository.EventRepositoryImpl
 */
public interface EventRepository {

  /**
   * Busca un evento por su identificador único.
   *
   * @param id UUID del evento. No debe ser {@code null}.
   * @return {@link Optional} con el evento si existe;
   *         {@code Optional.empty()} en caso contrario.
   */
  Optional<Event> findById(UUID id);

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
   * Verifica si existe un evento con el ID dado y que pertenezca a la cuenta
   * indicada.
   *
   * @param eventId   UUID del evento. No debe ser {@code null}.
   * @param accountId UUID de la cuenta propietaria. No debe ser {@code null}.
   * @return {@code true} si el evento existe y pertenece a la cuenta.
   */
  boolean existsByIdAndAccountId(UUID eventId, UUID accountId);

  /**
   * Lista todos los eventos asociados a una cuenta específica.
   *
   * <p>
   * Utilizado por el caso de uso de listado para recuperar el conjunto
   * completo de eventos de una cuenta, sobre el cual se aplican filtros
   * adicionales en memoria.
   *
   * @param accountId UUID de la cuenta propietaria. No debe ser {@code null}.
   * @return lista de eventos de la cuenta. Puede ser vacía si no hay
   *         registros.
   */
  List<Event> findAllByAccountId(UUID accountId);

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
   * Elimina un evento por su identificador único.
   *
   * <p>
   * Si no existe un evento con el ID dado, la operación no tiene efecto
   * (comportamiento silencioso). La validación de existencia previa se
   * realiza en la capa de aplicación.
   *
   * @param id UUID del evento a eliminar. No debe ser {@code null}.
   */
  void deleteById(UUID id);
}
