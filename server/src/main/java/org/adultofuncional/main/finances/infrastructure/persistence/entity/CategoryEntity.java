package org.adultofuncional.main.finances.infrastructure.persistence.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.adultofuncional.main.agenda.infrastructure.persistence.entity.EventEntity;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UuidGenerator;

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
@Table(name = "categories")
@SQLRestriction("category_deleted_at IS NULL") // Soft delete automático
@Getter
@Setter
@NoArgsConstructor
public class CategoryEntity {

  @Id
  @GeneratedValue
  @UuidGenerator(style = UuidGenerator.Style.TIME)
  @Column(name = "category_id", columnDefinition = "CHAR(36)")
  private UUID category_id;

  @Column(name = "category_name", length = 20, nullable = false)
  private String category_name;

  @Column(name = "category_type", length = 20, nullable = false)
  private String category_type;

  @Column(name = "category_created_at", updatable = false)
  private LocalDateTime category_created_at;

  @Column(name = "category_deleted_at")
  private LocalDateTime category_deleted_at;

  @OneToMany(mappedBy = "category")
  private List<MovementEntity> movements = new ArrayList<>();

  @OneToMany(mappedBy = "category")
  private List<FixedExpensesEntity> fixed_expenses = new ArrayList<>();

  @OneToMany(mappedBy = "category")
  private List<EventEntity> events = new ArrayList<>();

  @PrePersist
  protected void onCreate() {
    category_created_at = LocalDateTime.now();
  }

  public void softDelete() {
    this.category_deleted_at = LocalDateTime.now();
  }
}
