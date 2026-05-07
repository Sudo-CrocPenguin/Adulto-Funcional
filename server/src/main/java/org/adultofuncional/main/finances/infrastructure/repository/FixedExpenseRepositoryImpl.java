package org.adultofuncional.main.finances.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.adultofuncional.main.finances.domain.model.FixedExpense;
import org.adultofuncional.main.finances.domain.repository.FixedExpenseRepository;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.FixedExpensesEntity;
import org.adultofuncional.main.finances.infrastructure.persistence.mapper.FixedExpenseMapper;
import org.adultofuncional.main.finances.infrastructure.persistence.repository.SpringFixedExpenseJpaRepository;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class FixedExpenseRepositoryImpl implements FixedExpenseRepository {
  private final SpringFixedExpenseJpaRepository fixedExpenseJpaRepository;
  private final FixedExpenseMapper fixedExpenseMapper;

  @Override
  public Optional<FixedExpense> findById(UUID id) {
    return fixedExpenseJpaRepository.findById(id).map(fixedExpenseMapper::toDomain);
  }

  @Override
  public List<FixedExpense> findAllByAccountId(UUID accountId) {
    return fixedExpenseJpaRepository.findByAccount_AccountId(accountId)
        .stream().map(fixedExpenseMapper::toDomain).toList();
  }

  @Override
  public FixedExpense save(FixedExpense fixedExpense) {
    FixedExpensesEntity entity = fixedExpenseMapper.toEntity(fixedExpense);
    FixedExpensesEntity saved = fixedExpenseJpaRepository.save(entity);
    return fixedExpenseMapper.toDomain(saved);

  }

  @Override
  public void deleteById(UUID id) {
    fixedExpenseJpaRepository.deleteById(id);
  }

}
