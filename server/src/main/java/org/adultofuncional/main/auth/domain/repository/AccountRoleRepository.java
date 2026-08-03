package org.adultofuncional.main.auth.domain.repository;

import java.util.Set;
import java.util.UUID;

import org.adultofuncional.main.auth.domain.model.AccountRole;

/** Puerto de persistencia para los permisos concedidos a una cuenta. */
public interface AccountRoleRepository {

  /** Obtiene todos los roles persistidos para la cuenta. */
  Set<AccountRole> findByAccountId(UUID accountId);

  /** Concede un rol de forma idempotente. */
  void grant(UUID accountId, AccountRole role);
}
