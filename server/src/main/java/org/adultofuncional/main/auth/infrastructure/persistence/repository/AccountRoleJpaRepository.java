package org.adultofuncional.main.auth.infrastructure.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.adultofuncional.main.auth.infrastructure.persistence.entity.AccountRoleEntity;
import org.adultofuncional.main.auth.infrastructure.persistence.entity.AccountRoleId;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio Spring Data para roles de cuenta. */
public interface AccountRoleJpaRepository extends JpaRepository<AccountRoleEntity, AccountRoleId> {

  List<AccountRoleEntity> findAllByIdAccountId(UUID accountId);
}
