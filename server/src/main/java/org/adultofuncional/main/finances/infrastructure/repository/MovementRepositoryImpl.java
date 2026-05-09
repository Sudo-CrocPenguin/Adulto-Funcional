package org.adultofuncional.main.finances.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.adultofuncional.main.finances.domain.model.Movement;
import org.adultofuncional.main.finances.domain.repository.MovementRepository;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.MovementEntity;
import org.adultofuncional.main.finances.infrastructure.persistence.mapper.MovementMapper;
import org.adultofuncional.main.finances.infrastructure.persistence.repository.SpringMovementJpaRepository;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador concreto del puerto {@link MovementRepository}.
 *
 * <p>
 * Implementa las operaciones de persistencia de movimientos delegando en
 * {@link SpringMovementJpaRepository} (Spring Data JPA) y utilizando el
 * {@link MovementMapper} para convertir entre las entidades JPA
 * ({@link MovementEntity}) y el modelo de dominio ({@link Movement}).
 *
 * <p>
 * <strong>Métodos implementados:</strong>
 * <ul>
 * <li>{@link #findById(UUID)} — busca un movimiento por ID y lo convierte
 * a dominio.</li>
 * <li>{@link #findAllByAccountId(UUID)} — lista todos los movimientos
 * asociados a una cuenta.</li>
 * <li>{@link #save(Movement)} — persiste un movimiento nuevo o actualizado,
 * devolviendo el modelo de dominio resultante.</li>
 * <li>{@link #deleteById(UUID)} — elimina un movimiento por su ID.</li>
 * </ul>
 *
 * @author Lidys Jaraba
 * @since 0.0.1
 * @see MovementRepository
 * @see SpringMovementJpaRepository
 * @see MovementMapper
 */
@Repository
@RequiredArgsConstructor
public class MovementRepositoryImpl implements MovementRepository {

  private final SpringMovementJpaRepository jpaRepository;
  private final MovementMapper mapper;

  /**
   * Busca un movimiento por su identificador único.
   *
   * <p>
   * Consulta el repositorio Spring Data JPA y convierte la entidad resultante
   * al modelo de dominio mediante
   * {@link MovementMapper#toDomain(MovementEntity)}.
   *
   * @param id UUID del movimiento. No debe ser {@code null}.
   * @return {@link Optional} con el movimiento si existe;
   *         {@code Optional.empty()} en caso contrario.
   */
  @Override
  public Optional<Movement> findById(UUID id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  /**
   * Lista todos los movimientos asociados a una cuenta específica.
   *
   * <p>
   * Utiliza el método {@code findByAccount_AccountId} de Spring Data JPA
   * para recuperar las entidades y luego las convierte una a una al modelo
   * de dominio {@link Movement} mediante el mapper.
   *
   * @param accountId UUID de la cuenta propietaria. No debe ser {@code null}.
   * @return lista de movimientos de la cuenta (vacía si no hay registros).
   */
  @Override
  public List<Movement> findAllByAccountId(UUID accountId) {
    return jpaRepository.findByAccount_AccountId(accountId)
        .stream().map(mapper::toDomain).toList();
  }

  /**
   * Persiste un movimiento nuevo o actualiza uno existente.
   *
   * <p>
   * Convierte el modelo de dominio a entidad JPA con
   * {@link MovementMapper#toEntity(Movement)}, la guarda mediante Spring Data JPA
   * y vuelve a convertir el resultado a dominio para retornar la versión
   * persistida (incluyendo el ID si fue generado).
   *
   * @param movement el movimiento a guardar. No debe ser {@code null}.
   * @return el movimiento persistido como modelo de dominio.
   */
  @Override
  public Movement save(Movement movement) {
    MovementEntity entity = mapper.toEntity(movement);
    MovementEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  /**
   * Elimina un movimiento por su identificador único.
   *
   * <p>
   * Si no existe ningún movimiento con el ID dado, la operación no tiene efecto
   * (comportamiento silencioso de Spring Data JPA). La validación de existencia
   * previa se realiza en la capa de aplicación.
   *
   * @param id UUID del movimiento a eliminar. No debe ser {@code null}.
   */
  @Override
  public void deleteById(UUID id) {
    jpaRepository.deleteById(id);
  }
}
