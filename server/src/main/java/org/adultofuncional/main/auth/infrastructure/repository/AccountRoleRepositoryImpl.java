package org.adultofuncional.main.auth.infrastructure.repository;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.adultofuncional.main.auth.domain.model.AccountRole;
import org.adultofuncional.main.auth.domain.repository.AccountRoleRepository;
import org.adultofuncional.main.auth.infrastructure.persistence.entity.AccountRoleEntity;
import org.adultofuncional.main.auth.infrastructure.persistence.entity.AccountRoleId;
import org.adultofuncional.main.auth.infrastructure.persistence.repository.AccountRoleJpaRepository;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

/** Adaptador JPA del puerto de roles de cuenta. */
@Repository
@RequiredArgsConstructor
public class AccountRoleRepositoryImpl implements AccountRoleRepository {

  private final AccountRoleJpaRepository jpaRepository;

  @Override
  public Set<AccountRole> findByAccountId(UUID accountId) {
    return jpaRepository.findAllByIdAccountId(accountId).stream()
        .map(entity -> AccountRole.valueOf(entity.getId().getRoleName()))
        .collect(Collectors.toUnmodifiableSet());
  }

  @Override
  public void grant(UUID accountId, AccountRole role) {
    AccountRoleId id = new AccountRoleId(accountId, role.name());
    if (!jpaRepository.existsById(id)) {
      jpaRepository.save(new AccountRoleEntity(id));
    }
  }
}
