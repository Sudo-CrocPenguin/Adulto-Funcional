package org.adultofuncional.main.agenda.infrastructure.repository;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.adultofuncional.main.agenda.domain.model.Event;
import org.adultofuncional.main.agenda.domain.repository.EventRepository;
import org.adultofuncional.main.agenda.infrastructure.persistence.entity.EventEntity;
import org.adultofuncional.main.agenda.infrastructure.persistence.mapper.EventMapper;
import org.adultofuncional.main.agenda.infrastructure.persistence.repository.SpringEventJpaRepository;
import org.adultofuncional.main.shared.pagination.PageQuery;
import org.adultofuncional.main.shared.pagination.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador concreto del puerto {@link EventRepository}.
 *
 * <p>
 * Implementa las operaciones de persistencia de eventos delegando en
 * {@link SpringEventJpaRepository} (Spring Data JPA) y utilizando el
 * {@link EventMapper} para convertir entre las entidades JPA
 * ({@link EventEntity}) y el modelo de dominio ({@link Event}).
 *
 * <p>
 * <strong>Métodos implementados:</strong>
 * <ul>
 * <li>{@link #findByIdAndAccountId(UUID, UUID)} — busca un evento por ID y
 * cuenta,
 * garantizando propiedad.</li>
 * <li>Consulta páginas filtradas y ordenadas dentro de una cuenta.</li>
 * <li>{@link #save(Event)} — persiste un evento nuevo o actualizado,
 * devolviendo el
 * modelo de dominio resultante.</li>
 * <li>{@link #deleteByIdAndAccountId(UUID, UUID)} — elimina un evento por ID y
 * cuenta.</li>
 * </ul>
 *
 * @author Jeronimo Ospina Zapata
 * @since 0.0.1
 * @see EventRepository
 * @see SpringEventJpaRepository
 * @see EventMapper
 */
@Repository
@RequiredArgsConstructor
public class EventRepositoryImpl implements EventRepository {

  private static final Map<String, String> SORT_FIELDS = Map.of(
      "eventDate", "eventDate",
      "startHour", "eventStartHour",
      "priority", "eventPriority",
      "status", "eventStatus",
      "title", "eventTitle",
      "id", "eventId");

  private final SpringEventJpaRepository jpaRepository;
  private final EventMapper mapper;

  /**
   * Busca un evento por su ID y el ID de la cuenta propietaria.
   *
   * <p>
   * Utiliza {@link SpringEventJpaRepository#findByEventIdAndAccount_AccountId}
   * para garantizar que el evento pertenece a la cuenta indicada.
   *
   * @param eventId   UUID del evento. No debe ser {@code null}.
   * @param accountId UUID de la cuenta propietaria. No debe ser {@code null}.
   * @return {@link Optional} con el evento si existe y pertenece a la cuenta;
   *         {@code Optional.empty()} en caso contrario.
   */
  @Override
  public Optional<Event> findByIdAndAccountId(UUID eventId, UUID accountId) {
    return jpaRepository.findByEventIdAndAccount_AccountId(eventId, accountId)
        .map(mapper::toDomain);
  }

  /**
   * Consulta una página acotada de eventos.
   *
   * <p>
   * Traduce el campo lógico a la propiedad JPA, añade el UUID como desempate
   * y ejecuta filtros y límites directamente en MariaDB.
   *
   * @param accountId UUID de la cuenta propietaria. No debe ser {@code null}.
   * @return lista de eventos de la cuenta (vacía si no hay registros).
   */
  @Override
  public PageResult<Event> findPageByAccountId(
      UUID accountId,
      String status,
      String priority,
      UUID categoryId,
      LocalDate startDate,
      LocalDate endDate,
      PageQuery pageQuery) {
    String entitySortField = SORT_FIELDS.get(pageQuery.sortBy());
    Sort.Direction direction = pageQuery.ascending() ? Sort.Direction.ASC : Sort.Direction.DESC;
    Sort sort = Sort.by(direction, entitySortField);
    if (!entitySortField.equals("eventId")) {
      sort = sort.and(Sort.by(direction, "eventId"));
    }
    PageRequest pageable = PageRequest.of(pageQuery.number(), pageQuery.size(), sort);
    Page<EventEntity> page = jpaRepository.findPageByAccountId(
        accountId,
        status,
        priority,
        categoryId,
        startDate,
        endDate,
        pageable);
    return new PageResult<>(
        page.getContent().stream().map(mapper::toDomain).toList(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.hasNext(),
        page.hasPrevious());
  }

  /**
   * Persiste un evento nuevo o actualiza uno existente.
   *
   * <p>
   * Convierte el modelo de dominio a entidad JPA con
   * {@link EventMapper#toEntity(Event)}, la guarda mediante Spring Data JPA
   * y vuelve a convertir el resultado a dominio para retornar la versión
   * persistida (incluyendo el ID si fue generado).
   *
   * @param event el evento a guardar. No debe ser {@code null}.
   * @return el evento persistido como modelo de dominio.
   */
  @Override
  public Event save(Event event) {
    EventEntity entity = mapper.toEntity(event);
    EventEntity savedEntity = jpaRepository.save(entity);
    return mapper.toDomain(savedEntity);
  }

  /**
   * Elimina un evento por identificador y cuenta en una sola sentencia.
   */
  @Override
  public boolean deleteByIdAndAccountId(UUID eventId, UUID accountId) {
    return jpaRepository.deleteByEventIdAndAccountId(eventId, accountId) > 0;
  }
}
