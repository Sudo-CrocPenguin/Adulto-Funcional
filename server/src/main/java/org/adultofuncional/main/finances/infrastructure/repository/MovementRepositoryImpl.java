package org.adultofuncional.main.finances.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.adultofuncional.main.finances.domain.model.Movement;
import org.adultofuncional.main.finances.domain.repository.MovementRepository;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.MovementEntity;
import org.adultofuncional.main.finances.infrastructure.persistence.mapper.MovementMapper;
import org.adultofuncional.main.finances.infrastructure.persistence.repository.FinancesJpaRepository;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;


@Repository
@RequiredArgsConstructor
public class MovementRepositoryImpl implements MovementRepository {
    
    private final FinancesJpaRepository jpaRepository;
    private final MovementMapper mapper;

    @Override
    public Optional<Movement> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }


    @Override
    public List<Movement> findAllByAccountId(UUID accountId) {
        return jpaRepository.findByAccount_AccountId(accountId)
        .stream().map(mapper::toDomain).toList();
    }


    @Override
    public Movement save(Movement movement) {

        MovementEntity entity = mapper.toEntity(movement, movement.getId());
        MovementEntity saved = jpaRepository.save(entity);

        return mapper.toDomain(saved);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
