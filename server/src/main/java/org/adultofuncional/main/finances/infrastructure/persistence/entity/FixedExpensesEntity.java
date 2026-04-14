package org.adultofuncional.main.finances.infrastructure.persistence.entity;

import java.math.BigDecimal;
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
@Table(name = "fixed_expenses")
@Getter
@Setter
@NoArgsConstructor
public class FixedExpensesEntity {

  @Id
  @GeneratedValue
  @UuidGenerator(style = UuidGenerator.Style.TIME)
  @Column(name = "fixed_expense_id", columnDefinition = "CHAR(36)")
  private UUID fixed_expense_id;

  @Column(name = "fixed_expense_name", length = 20, nullable = false)
  private String fixed_expense_name;

  @Column(name = "fixed_expense_frequency", length = 15, nullable = false)
  private String fixed_expense_frequency; // Mensual, Anual, etc.

  @Column(name = "fixed_expense_amount", precision = 10, scale = 2, nullable = false)
  private BigDecimal fixed_expense_amount;

  @Column(name = "fixed_expense_status", length = 15, nullable = false)
  private String fixed_expense_status; // Activo, Inactivo

  @Column(name = "fixed_expense_closing_date", nullable = false)
  private LocalDate fixed_expense_closing_date;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "fixed_expense_fk_category_id")
  private CategoryEntity category;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "fixed_expense_fk_account_id", nullable = false)
  private AccountEntity account;
}
