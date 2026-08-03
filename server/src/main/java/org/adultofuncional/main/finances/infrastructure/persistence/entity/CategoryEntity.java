package org.adultofuncional.main.finances.infrastructure.persistence.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.adultofuncional.main.agenda.infrastructure.persistence.entity.EventEntity;
import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad JPA que mapea la tabla {@code categories} de MariaDB.
 *
 * <p>
 * Categoría de uso interno del sistema (precargada, no gestionable por el
 * usuario) que sirve para clasificar movimientos, gastos fijos y eventos.
 * El campo {@code category_type} diferencia entre categorías de finanzas y de
 * agenda.
 *
 * <p>
 * Schema de la tabla {@code categories}:
 * 
 * <pre>
 * category_id   CHAR(36)    NOT NULL PRIMARY KEY
 * category_name VARCHAR(50) NOT NULL
 * category_type VARCHAR(20) NOT NULL
 * </pre>
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 * @see MovementEntity
 * @see FixedExpensesEntity
 * @see EventEntity
 */
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
public class CategoryEntity {

  /**
   * Identificador único de la categoría.
   *
   * <p>
   * Columna: {@code category_id CHAR(36) NOT NULL PRIMARY KEY}.
   */
  @Id
  @Column(name = "category_id", columnDefinition = "CHAR(36)")
  private UUID categoryId;

  /**
   * Nombre de la categoría.
   *
   * <p>
   * Columna: {@code category_name VARCHAR(50) NOT NULL}.
   * Ejemplos: "Alimentación", "Transporte", "Trabajo", "Salud".
   */
  @Column(name = "category_name", length = 50, nullable = false)
  private String categoryName;

  @Column(name = "normalized_name", length = 150, nullable = false)
  private String normalizedName;

  /**
   * Tipo de categoría que define el módulo donde se utiliza.
   *
   * <p>
   * Columna: {@code category_type VARCHAR(20) NOT NULL}.
   * Valores: {@code "Finanzas"} para movimientos y gastos fijos,
   * {@code "Agenda"} para eventos.
   */
  @Column(name = "category_type", length = 20, nullable = false)
  private String categoryType;

  @Column(name = "category_scope", length = 8, nullable = false)
  private String categoryScope;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_account_id")
  private AccountEntity ownerAccount;

  /** Discriminador calculado por MariaDB para la restricción única. */
  @Column(name = "ownership_discriminator", insertable = false, updatable = false)
  private String ownershipDiscriminator;

  /**
   * Movimientos financieros asociados a esta categoría.
   */
  @OneToMany(mappedBy = "category")
  private List<MovementEntity> movements = new ArrayList<>();

  /**
   * Gastos fijos asociados a esta categoría.
   */
  @OneToMany(mappedBy = "category")
  private List<FixedExpensesEntity> fixedExpenses = new ArrayList<>();

  /**
   * Eventos de agenda asociados a esta categoría.
   */
  @OneToMany(mappedBy = "category")
  private List<EventEntity> events = new ArrayList<>();
}
