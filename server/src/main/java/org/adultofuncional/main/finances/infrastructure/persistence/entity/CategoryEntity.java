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

/**
 * Entidad JPA que representa una categoría para clasificación interna del
 * sistema.
 * 
 * <p>
 * <strong>⚠️ Importante:</strong> Esta entidad es de <strong>uso
 * interno</strong>.
 * Las categorías son precargadas por el sistema y <strong>no son gestionables
 * por el usuario final</strong> (no hay endpoints CRUD para categorías).
 * El usuario solo puede seleccionar categorías preexistentes.
 * 
 * <p>
 * Esta entidad tiene un propósito <strong>dual</strong>:
 * <ul>
 * <li><b>Módulo financiero:</b> Categoriza movimientos y gastos fijos</li>
 * <li><b>Módulo agenda:</b> Categoriza eventos (Trabajo, Personal, Salud,
 * etc.)</li>
 * </ul>
 * 
 * <p>
 * La diferenciación se realiza mediante el campo {@code category_type}, que
 * permite filtrar categorías según el módulo que las utiliza.
 * 
 * <p>
 * <strong>Características principales:</strong>
 * <ul>
 * <li><b>Identificador único:</b> UUID v7 generado automáticamente</li>
 * <li><b>Soft delete:</b> Borrado lógico mediante
 * {@code category_deleted_at}</li>
 * <li><b>Filtro automático:</b> {@code @SQLRestriction} excluye categorías
 * eliminadas</li>
 * <li><b>Fecha de creación automática:</b> Establecida en
 * {@code @PrePersist}</li>
 * <li><b>Uso compartido:</b> Utilizada por movimientos, gastos fijos y
 * eventos</li>
 * </ul>
 * 
 * <p>
 * <strong>Restricciones de base de datos:</strong>
 * <ul>
 * <li>{@code category_id}: CHAR(36) PRIMARY KEY DEFAULT(UUID_V7())</li>
 * <li>{@code category_name}: VARCHAR(20) NOT NULL</li>
 * <li>{@code category_type}: VARCHAR(20) NOT NULL</li>
 * <li>{@code category_deleted_at}: TIMESTAMP NULL (soft delete)</li>
 * </ul>
 * 
 * <p>
 * <strong>Relaciones:</strong>
 * <ul>
 * <li>{@code @OneToMany} → {@link MovementEntity} (movimientos
 * financieros)</li>
 * <li>{@code @OneToMany} → {@link FixedExpensesEntity} (gastos fijos)</li>
 * <li>{@code @OneToMany} → {@link EventEntity} (eventos de agenda)</li>
 * </ul>
 * 
 * @author juan
 * @since 0.0.1
 * @see MovementEntity
 * @see FixedExpensesEntity
 * @see EventEntity
 */
@Entity
@Table(name = "categories")
@SQLRestriction("category_deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class CategoryEntity {

  /**
   * Identificador único de la categoría.
   * 
   * <p>
   * Se genera automáticamente como UUID v7 (ordenable temporalmente).
   * El estilo {@code UuidGenerator.Style.TIME} garantiza que los primeros
   * bits contengan un timestamp, lo que mejora la indexación en base de datos.
   * 
   * <p>
   * Formato: {@code CHAR(36)} en base de datos.
   */
  @Id
  @GeneratedValue
  @UuidGenerator(style = UuidGenerator.Style.TIME)
  @Column(name = "category_id", columnDefinition = "CHAR(36)")
  private UUID category_id;

  /**
   * Nombre descriptivo de la categoría.
   * 
   * <p>
   * Obligatorio. Máximo 20 caracteres.
   * 
   * <p>
   * <strong>Ejemplos por módulo:</strong>
   * <ul>
   * <li><b>Finanzas:</b> Alimentación, Transporte, Servicios, Salario,
   * Freelance</li>
   * <li><b>Agenda:</b> Trabajo, Personal, Salud, Estudio, Ocio, Familia</li>
   * </ul>
   */
  @Column(name = "category_name", length = 20, nullable = false)
  private String category_name;

  /**
   * Tipo de categoría que define el módulo de la aplicación donde se utiliza.
   * 
   * <p>
   * Obligatorio. Máximo 20 caracteres.
   * 
   * <p>
   * <strong>Valores:</strong>
   * <ul>
   * <li><b>"Finanzas":</b> Categorías para el módulo financiero (movimientos y
   * gastos fijos)</li>
   * <li><b>"Agenda":</b> Categorías para el módulo de agenda (eventos)</li>
   * </ul>
   * 
   * <p>
   * Este campo permite que una misma tabla almacene categorías para distintos
   * módulos, filtrando según el contexto de uso.
   * 
   * <p>
   * <strong>Ejemplos:</strong>
   * <ul>
   * <li>Finanzas: Alimentación, Transporte, Servicios, Salario</li>
   * <li>Agenda: Trabajo, Personal, Salud, Ocio</li>
   * </ul>
   */
  @Column(name = "category_type", length = 20, nullable = false)
  private String category_type;

  /**
   * Fecha y hora de creación de la categoría.
   * 
   * <p>
   * Se establece automáticamente en {@link #onCreate()} antes de
   * persistir la entidad por primera vez. El campo está marcado como
   * {@code updatable = false}, por lo que nunca se modificará después
   * de la inserción inicial.
   * 
   * <p>
   * Útil para auditoría y ordenamiento por antigüedad.
   */
  @Column(name = "category_created_at", updatable = false)
  private LocalDateTime category_created_at;

  /**
   * Fecha y hora de borrado lógico (soft delete).
   * 
   * <p>
   * <strong>⚠️ Uso administrativo interno:</strong> Este campo permite desactivar
   * categorías obsoletas sin eliminar datos históricos. <strong>No está expuesto
   * a usuarios finales.</strong>
   * 
   * <p>
   * Inicialmente {@code null}. Cuando se invoca {@link #softDelete()},
   * se establece con la fecha y hora actual.
   * 
   * <p>
   * Gracias a la anotación {@code @SQLRestriction} a nivel de clase,
   * todas las consultas JPA excluirán automáticamente las categorías
   * con este campo distinto de {@code null}.
   * 
   * <p>
   * <strong>Ventajas:</strong> Mantiene integridad referencial con movimientos
   * y eventos históricos que usen esta categoría.
   */
  @Column(name = "category_deleted_at")
  private LocalDateTime category_deleted_at;

  /**
   * Lista de movimientos financieros asociados a esta categoría.
   * 
   * <p>
   * Relación {@code @OneToMany} bidireccional con {@link MovementEntity}.
   * Una categoría puede estar asociada a múltiples movimientos.
   * 
   * <p>
   * Se inicializa como {@code ArrayList} vacía para evitar
   * {@link NullPointerException} al agregar elementos.
   */
  @OneToMany(mappedBy = "category")
  private List<MovementEntity> movements = new ArrayList<>();

  /**
   * Lista de gastos fijos asociados a esta categoría.
   * 
   * <p>
   * Relación {@code @OneToMany} bidireccional con {@link FixedExpensesEntity}.
   * Una categoría puede estar asociada a múltiples gastos fijos.
   * 
   * <p>
   * Se inicializa como {@code ArrayList} vacía para evitar
   * {@link NullPointerException} al agregar elementos.
   */
  @OneToMany(mappedBy = "category")
  private List<FixedExpensesEntity> fixed_expenses = new ArrayList<>();

  /**
   * Lista de eventos de agenda asociados a esta categoría.
   * 
   * <p>
   * Relación {@code @OneToMany} bidireccional con {@link EventEntity}.
   * Una categoría puede estar asociada a múltiples eventos.
   * 
   * <p>
   * Se inicializa como {@code ArrayList} vacía para evitar
   * {@link NullPointerException} al agregar elementos.
   */
  @OneToMany(mappedBy = "category")
  private List<EventEntity> events = new ArrayList<>();

  /**
   * Callback de ciclo de vida JPA ejecutado antes de persistir la entidad.
   * 
   * <p>
   * Establece automáticamente la fecha y hora de creación de la categoría
   * al momento de ser guardada por primera vez en la base de datos.
   * 
   * <p>
   * Este método es invocado automáticamente por el proveedor JPA
   * (Hibernate) durante la operación {@code persist()}.
   */
  @PrePersist
  protected void onCreate() {
    category_created_at = LocalDateTime.now();
  }

  /**
   * Marca la categoría como eliminada (soft delete).
   * 
   * <p>
   * Establece {@code category_deleted_at} con la fecha y hora actual.
   * A partir de este momento, la categoría será excluida de todas las
   * consultas JPA gracias a {@code @SQLRestriction}.
   * 
   * <p>
   * <strong>⚠️ Nota:</strong> Este método no persiste el cambio automáticamente.
   * Debe llamarse dentro de una transacción y luego guardar la entidad.
   * Solo debe ser usado por administradores del sistema.
   */
  public void softDelete() {
    this.category_deleted_at = LocalDateTime.now();
  }
}
