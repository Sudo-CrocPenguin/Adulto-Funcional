package org.adultofuncional.main.auth.infrastructure.persistence.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Clave compuesta de un rol concedido a una cuenta. */
@Embeddable
@Getter
@NoArgsConstructor
public class AccountRoleId implements Serializable {

  @Column(name = "account_role_fk_account_id", columnDefinition = "CHAR(36)")
  private UUID accountId;

  @Column(name = "account_role_name", length = 30)
  private String roleName;

  public AccountRoleId(UUID accountId, String roleName) {
    this.accountId = accountId;
    this.roleName = roleName;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof AccountRoleId that)) {
      return false;
    }
    return Objects.equals(accountId, that.accountId) && Objects.equals(roleName, that.roleName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(accountId, roleName);
  }
}
