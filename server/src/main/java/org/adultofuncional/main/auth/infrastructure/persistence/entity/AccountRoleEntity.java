package org.adultofuncional.main.auth.infrastructure.persistence.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Entidad JPA de un rol persistente concedido a una cuenta. */
@Entity
@Table(name = "account_roles")
@Getter
@NoArgsConstructor
public class AccountRoleEntity {

  @EmbeddedId
  private AccountRoleId id;

  public AccountRoleEntity(AccountRoleId id) {
    this.id = id;
  }
}
