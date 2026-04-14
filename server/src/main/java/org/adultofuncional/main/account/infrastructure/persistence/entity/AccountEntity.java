package org.adultofuncional.main.account.infrastructure.persistence.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.adultofuncional.main.agenda.infrastructure.persistence.entity.EventEntity;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.FixedExpensesEntity;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.MovementEntity;
import org.adultofuncional.main.security.infrastructure.persistence.entity.PasswordEntity;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
public class AccountEntity {

  @Id
  @GeneratedValue
  @UuidGenerator(style = UuidGenerator.Style.TIME)
  @Column(name = "account_id", columnDefinition = "CHAR(36)")
  private UUID account_id;

  @Column(name = "account_names", length = 50, nullable = false)
  private String account_names;

  @Column(name = "account_lastnames", length = 50, nullable = false)
  private String account_lastnames;

  @Column(name = "account_email", length = 255, nullable = false, unique = true)
  private String account_email;

  @Column(name = "account_phone", length = 20, nullable = false)
  private String account_phone;

  @Column(name = "account_password", length = 60, nullable = false)
  private String account_password;

  @Column(name = "account_master_key", length = 24)
  private String account_master_key;

  @Column(name = "account_created_at", updatable = false)
  private LocalDateTime account_created_at;

  @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<MovementEntity> movements = new ArrayList<>();

  @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<FixedExpensesEntity> fixed_expenses = new ArrayList<>();

  @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<EventEntity> events = new ArrayList<>();

  @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<PasswordEntity> passwords = new ArrayList<>();

  @PrePersist
  protected void onCreate() {
    account_created_at = LocalDateTime.now();
  }
}
