package org.adultofuncional.main.finances.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "movements")
@Getter
@Setter
@NoArgsConstructor
public class MovementEntity {

  @Id
  @GeneratedValue
  @UuidGenerator(style = UuidGenerator.Style.TIME)
  @Column(name = "movement_id", columnDefinition = "CHAR(36)")
  private UUID movement_id;

  @Column(name = "movement_type", length = 7, nullable = false)
  private String movement_type;

  @Column(name = "movement_amount", precision = 10, scale = 2, nullable = false)
  private BigDecimal movement_amount;

  @Column(name = "movement_register_date", nullable = false, updatable = false)
  private LocalDateTime movement_register_date;

  @Column(name = "movement_description", columnDefinition = "TEXT")
  private String movement_description;

  @Column(name = "movement_date", nullable = false)
  private LocalDate movement_date;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "movement_fk_account_id", nullable = false)
  private AccountEntity account;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "movement_fk_category_id")
  private CategoryEntity category;

  @PrePersist
  protected void onCreate() {
    movement_register_date = LocalDateTime.now();
  }
}
