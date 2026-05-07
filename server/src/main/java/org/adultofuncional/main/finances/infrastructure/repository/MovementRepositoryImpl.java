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
 * Implementación del repositorio de movimientos en la capa de infraestructura.
 *
 * <p>
 * Conecta el puerto del dominio con Spring Data JPA y MariaDB,
 * convirtiendo entre {@link Movement} (modelo de dominio) y
 * {@link MovementEntity} (entidad JPA) mediante {@link MovementMapper}.
 *
 * @author Lidys Jaraba
 * @since 0.0.1
 * @see MovementRepository
 * @see MovementMapper
 */

@Repository
@RequiredArgsConstructor
public class MovementRepositoryImpl implements MovementRepository {
    
    private final SpringMovementJpaRepository jpaRepository;
    private final MovementMapper mapper;

    /**
   * Busca un movimiento por su identificador UUID.
   *
   * @param id identificador del movimiento; no puede ser {@code null}
   * @return {@link Optional} con el movimiento si existe, vacío si no
   */

    @Override
    public Optional<Movement> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    /**
   * Lista todos los movimientos asociados a una cuenta.
   *
   * <p>
   * Delega en {@link FinancesJpaRepository#findByAccount_AccountId(UUID)},
   * que Spring Data traduce a una consulta por FK {@code movement_fk_account_id}.
   *
   * @param accountId UUID de la cuenta propietaria; no puede ser {@code null}
   * @return lista de movimientos; vacía si no hay ninguno
   */

    @Override
    public List<Movement> findAllByAccountId(UUID accountId) {
        return jpaRepository.findByAccount_AccountId(accountId)
        .stream().map(mapper::toDomain).toList();
    }

     /**
   * Guarda o actualiza un movimiento.
   *
   * <p>
   * Flujo: {@code Movement} → {@code mapper.toEntity()}
   * → {@code jpaRepository.save()} → {@code mapper.toDomain()}
   *
   * @param movement modelo de dominio a persistir; no puede ser {@code null}
   * @return modelo de dominio con los campos actualizados tras la persistencia
   */

    @Override
    
    public Movement save(Movement movement) {

        MovementEntity entity = mapper.toEntity(movement);
        MovementEntity saved = jpaRepository.save(entity);

        return mapper.toDomain(saved);
    }

    /**
   * Elimina físicamente un movimiento por su identificador.
   *
   * @param id identificador del movimiento a eliminar; no puede ser {@code null}
   */
  
    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
