package org.adultofuncional.main.security.infrastructure.persistence.entity;

import java.time.LocalDate;
import java.util.UUID;

import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "passwords")
@Getter
@Setter
@NoArgsConstructor
public class PasswordEntity {

  @Id
  @GeneratedValue
  @UuidGenerator(style = UuidGenerator.Style.TIME)
  @Column(name = "password_id", columnDefinition = "CHAR(36)")
  private UUID password_id;

  @Column(name = "password_application_name", length = 35, nullable = false)
  private String password_application_name;

  @Column(name = "password_application", length = 20, nullable = false)
  private String password_application;

  @Column(name = "password_last_change_date")
  private LocalDate password_last_change_date;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "passwords_fk_account_id", nullable = false)
  private AccountEntity account;

}
