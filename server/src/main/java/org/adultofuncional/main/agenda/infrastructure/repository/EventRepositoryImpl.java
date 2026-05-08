package org.adultofuncional.main.agenda.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.adultofuncional.main.agenda.domain.model.Event;
import org.adultofuncional.main.agenda.domain.repository.EventRepository;
import org.adultofuncional.main.agenda.infrastructure.persistence.entity.EventEntity;
import org.adultofuncional.main.agenda.infrastructure.persistence.mapper.EventMapper;
import org.adultofuncional.main.agenda.infrastructure.persistence.repository.SpringEventJpaRepository;
import org.springframework.stereotype.Repository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Implementación del puerto {@link EventRepository}.
 *
 * <p>
 * Adaptador encargado de conectar el dominio con la capa
 * de persistencia usando Spring Data JPA.
 *
 * @author Jeronimo Ospina Zapata
 * @since 0.0.1
 */
@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventRepositoryImpl implements EventRepository {

  final SpringEventJpaRepository jpaRepository;
  final EventMapper mapper;

  /**
   * Persiste un evento en la base de datos.
   *
   * @param event modelo de dominio
   * @return evento persistido
   */
  @Override
  public Event save(Event event) {
    EventEntity entity = mapper.toEntity(event);
    EventEntity savedEntity = jpaRepository.save(entity);

    return mapper.toDomain(savedEntity);
  }

  /**
   * Busca un evento por su identificador.
   *
   * @param id identificador
   * @return evento encontrado (si existe)
   */
  @Override
  public Optional<Event> findById(UUID id) {

    return jpaRepository.findById(id)
        .map(mapper::toDomain);
  }

  /**
   * Retorna todos los eventos registrados.
   *
   * @return lista de eventos
   */
  @Override
  public List<Event> findAll() {

    return jpaRepository.findAll()
        .stream()
        .map(mapper::toDomain)
        .toList();
  }

  /**
   * Verifica si un evento existe por su identificador.
   *
   * @param id identificador
   * @return {@code true} si existe
   */
  @Override
  public boolean existsById(UUID id) {

    return jpaRepository.existsById(id);
  }
}
