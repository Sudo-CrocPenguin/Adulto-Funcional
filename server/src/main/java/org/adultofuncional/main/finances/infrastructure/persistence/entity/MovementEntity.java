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

/**
 * Entidad JPA que representa un movimiento financiero individual.
 * 
 * <p>
 * Esta es la entidad fundamental del módulo de finanzas. Cada movimiento
 * representa una transacción que afecta el balance de una cuenta, ya sea
 * una entrada de dinero (Ingreso) o una salida (Egreso).
 * 
 * <p>
 * <strong>Características principales:</strong>
 * <ul>
 * <li><b>Identificador único:</b> UUID v7 generado automáticamente</li>
 * <li><b>Tipo de movimiento:</b> "Ingreso" o "Egreso"</li>
 * <li><b>Fecha de registro automática:</b> Establecida en
 * {@code @PrePersist}</li>
 * <li><b>Fecha de movimiento independiente:</b> Permite registrar transacciones
 * en fechas pasadas o futuras</li>
 * <li><b>Precisión monetaria:</b> {@link BigDecimal} con escala 2 para valores
 * exactos</li>
 * <li><b>Categorización opcional:</b> Asociable a una categoría para
 * clasificación</li>
 * </ul>
 * 
 * <p>
 * <strong>Tipos de movimiento:</strong>
 * <ul>
 * <li><b>"Ingreso":</b> Entrada de dinero que aumenta el balance</li>
 * <li><b>"Egreso":</b> Salida de dinero que disminuye el balance</li>
 * </ul>
 * 
 * <p>
 * <strong>Diferencia entre fechas:</strong>
 * <ul>
 * <li>{@code movement_register_date}: Cuándo se registró en el sistema
 * (automático)</li>
 * <li>{@code movement_date}: Cuándo ocurrió realmente la transacción
 * (manual)</li>
 * </ul>
 * Esta separación permite registrar movimientos retroactivos o programar
 * futuros.
 * 
 * <p>
 * <strong>Restricciones de base de datos:</strong>
 * <ul>
 * <li>{@code movement_id}: CHAR(36) PRIMARY KEY DEFAULT(UUID_V7())</li>
 * <li>{@code movement_type}: VARCHAR(7) NOT NULL</li>
 * <li>{@code movement_amount}: DECIMAL(10,2) NOT NULL</li>
 * <li>{@code movement_date}: DATE NOT NULL</li>
 * <li>{@code movement_description}: TEXT NULL</li>
 * </ul>
 * 
 * <p>
 * <strong>Relaciones:</strong>
 * <ul>
 * <li>{@code @ManyToOne} → {@link AccountEntity} (obligatorio)</li>
 * <li>{@code @ManyToOne} → {@link CategoryEntity} (opcional)</li>
 * </ul>
 * 
 * @author juan
 * @since 0.0.1
 * @see AccountEntity
 * @see CategoryEntity
 */
@Entity
@Table(name = "movements")
@Getter
@Setter
@NoArgsConstructor
public class MovementEntity {

  /**
   * Identificador único del movimiento.
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
  @Column(name = "movement_id", columnDefinition = "CHAR(36)")
  private UUID movement_id;

  /**
   * Tipo de movimiento financiero.
   * 
   * <p>
   * Obligatorio. Máximo 7 caracteres.
   * 
   * <p>
   * <strong>Valores:</strong>
   * <ul>
   * <li><b>"Ingreso":</b> Entrada de dinero (salario, venta, reembolso)</li>
   * <li><b>"Egreso":</b> Salida de dinero (compra, pago, retiro)</li>
   * </ul>
   * 
   * <p>
   * <strong>Validación en aplicación:</strong>
   * Al asignar una categoría, se debe verificar que el {@code category_type}
   * sea compatible con este tipo de movimiento.
   */
  @Column(name = "movement_type", length = 7, nullable = false)
  private String movement_type;

  /**
   * Monto del movimiento.
   * 
   * <p>
   * Obligatorio. Precisión de 10 dígitos totales con 2 decimales.
   * 
   * <p>
   * <strong>⚠️ Importante:</strong> Se usa {@link BigDecimal} en lugar de
   * {@code float} o {@code double} para garantizar precisión exacta en
   * operaciones financieras y evitar errores de redondeo.
   * 
   * <p>
   * <strong>Convención:</strong>
   * <ul>
   * <li>Ingresos: valores positivos</li>
   * <li>Egresos: valores positivos (el tipo ya indica la dirección)</li>
   * </ul>
   * 
   * <p>
   * Formato en BD: {@code DECIMAL(10,2)}.
   */
  @Column(name = "movement_amount", precision = 10, scale = 2, nullable = false)
  private BigDecimal movement_amount;

  /**
   * Fecha y hora en que se registró el movimiento en el sistema.
   * 
   * <p>
   * Se establece automáticamente en {@link #onCreate()} antes de
   * persistir la entidad por primera vez. El campo está marcado como
   * {@code updatable = false}, por lo que nunca se modificará después
   * de la inserción inicial.
   * 
   * <p>
   * Útil para auditoría y para diferenciar cuándo se registró vs cuándo ocurrió.
   */
  @Column(name = "movement_register_date", nullable = false, updatable = false)
  private LocalDateTime movement_register_date;

  /**
   * Descripción opcional del movimiento.
   * 
   * <p>
   * Campo de texto libre sin límite de longitud ({@code TEXT} en base de datos).
   * 
   * <p>
   * <strong>Ejemplos:</strong>
   * <ul>
   * <li>"Compra semanal en supermercado"</li>
   * <li>"Pago de salario mensual"</li>
   * <li>"Cena con amigos"</li>
   * <li>"Factura de electricidad"</li>
   * </ul>
   */
  @Column(name = "movement_description", columnDefinition = "TEXT")
  private String movement_description;

  /**
   * Fecha en que ocurrió o ocurrirá el movimiento.
   * 
   * <p>
   * Obligatorio. Representa el día calendario de la transacción.
   * 
   * <p>
   * A diferencia de {@code movement_register_date}, esta fecha es
   * proporcionada por el usuario y puede ser:
   * <ul>
   * <li><b>Pasada:</b> Para registrar movimientos retroactivos</li>
   * <li><b>Presente:</b> Para movimientos del día</li>
   * <li><b>Futura:</b> Para programar movimientos (proyecciones)</li>
   * </ul>
   */
  @Column(name = "movement_date", nullable = false)
  private LocalDate movement_date;

  /**
   * Cuenta de usuario propietaria del movimiento.
   * 
   * <p>
   * Relación {@code @ManyToOne} obligatoria con {@link AccountEntity}.
   * Carga perezosa ({@code LAZY}) para optimizar rendimiento.
   * 
   * <p>
   * Cada movimiento pertenece a una única cuenta. La eliminación de una cuenta
   * eliminará en cascada todos sus movimientos ({@code CascadeType.ALL} +
   * {@code orphanRemoval = true} definido en {@link AccountEntity}).
   * 
   * <p>
   * En base de datos: {@code movement_fk_account_id CHAR(36) NOT NULL}.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "movement_fk_account_id", nullable = false)
  private AccountEntity account;

  /**
   * Categoría asociada al movimiento.
   * 
   * <p>
   * Relación {@code @ManyToOne} opcional con {@link CategoryEntity}.
   * Carga perezosa ({@code LAZY}) para optimizar rendimiento.
   * 
   * <p>
   * Permite clasificar el movimiento para reportes y análisis financieros.
   * 
   * <p>
   * <strong>Validación recomendada:</strong>
   * La categoría asignada debe tener un {@code category_type} de "Finanzas".
   * 
   * <p>
   * En base de datos: {@code movement_fk_category_id CHAR(36)}.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "movement_fk_category_id")
  private CategoryEntity category;

  /**
   * Callback de ciclo de vida JPA ejecutado antes de persistir la entidad.
   * 
   * <p>
   * Establece automáticamente la fecha y hora de registro del movimiento
   * al momento de ser guardado por primera vez en la base de datos.
   * 
   * <p>
   * Este método es invocado automáticamente por el proveedor JPA
   * (Hibernate) durante la operación {@code persist()}.
   */
  @PrePersist
  protected void onCreate() {
    movement_register_date = LocalDateTime.now();
  }
}
