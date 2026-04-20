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

/**
 * Entidad JPA que representa un gasto fijo recurrente del usuario.
 * 
 * <p>
 * Esta entidad modela pagos periódicos como suscripciones, servicios,
 * alquileres o cualquier gasto que se repita con una frecuencia determinada.
 * 
 * <p>
 * <strong>Características principales:</strong>
 * <ul>
 * <li><b>Identificador único:</b> UUID v7 generado automáticamente</li>
 * <li><b>Frecuencia configurable:</b> Mensual, Anual, Semanal, etc.</li>
 * <li><b>Estado:</b> Activo o Inactivo para controlar su aplicación</li>
 * <li><b>Fecha de cierre:</b> Permite programar la finalización del gasto</li>
 * <li><b>Precisión monetaria:</b> {@link BigDecimal} con escala 2 para valores
 * exactos</li>
 * </ul>
 * 
 * <p>
 * <strong>Frecuencias típicas:</strong>
 * <ul>
 * <li>"Mensual" - Pago cada mes</li>
 * <li>"Anual" - Pago cada año</li>
 * <li>"Semanal" - Pago cada semana</li>
 * <li>"Quincenal" - Pago cada 15 días</li>
 * <li>"Trimestral" - Pago cada 3 meses</li>
 * </ul>
 * 
 * <p>
 * <strong>Estados:</strong>
 * <ul>
 * <li>"Activo" - El gasto se aplica actualmente</li>
 * <li>"Inactivo" - El gasto está pausado o cancelado</li>
 * </ul>
 * 
 * <p>
 * <strong>Restricciones de base de datos:</strong>
 * <ul>
 * <li>{@code fixed_expense_id}: CHAR(36) PRIMARY KEY DEFAULT(UUID_V7())</li>
 * <li>{@code fixed_expense_name}: VARCHAR(20) NOT NULL</li>
 * <li>{@code fixed_expense_frequency}: VARCHAR(15) NOT NULL</li>
 * <li>{@code fixed_expense_amount}: DECIMAL(10,2) NOT NULL</li>
 * <li>{@code fixed_expense_status}: VARCHAR(15) NOT NULL</li>
 * <li>{@code fixed_expense_closing_date}: DATE NOT NULL</li>
 * </ul>
 * 
 * <p>
 * <strong>Relaciones:</strong>
 * <ul>
 * <li>{@code @ManyToOne} → {@link AccountEntity} (obligatorio)</li>
 * <li>{@code @ManyToOne} → {@link CategoryEntity} (opcional)</li>
 * </ul>
 * 
 * <p>
 * Esta entidad permite al sistema generar automáticamente movimientos
 * financieros recurrentes basados en la frecuencia y el monto configurados.
 * 
 * @author juan
 * @since 0.0.1
 * @see AccountEntity
 * @see CategoryEntity
 * @see MovementEntity
 */
@Entity
@Table(name = "fixed_expenses")
@Getter
@Setter
@NoArgsConstructor
public class FixedExpensesEntity {

  /**
   * Identificador único del gasto fijo.
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
  @Column(name = "fixed_expense_id", columnDefinition = "CHAR(36)")
  private UUID fixed_expense_id;

  /**
   * Nombre descriptivo del gasto fijo.
   * 
   * <p>
   * Obligatorio. Máximo 20 caracteres.
   * 
   * <p>
   * <strong>Ejemplos:</strong>
   * <ul>
   * <li>"Netflix" - Suscripción de streaming</li>
   * <li>"Alquiler" - Pago de vivienda</li>
   * <li>"Gimnasio" - Membresía mensual</li>
   * <li>"Internet" - Servicio de conexión</li>
   * <li>"Seguro" - Póliza anual</li>
   * </ul>
   */
  @Column(name = "fixed_expense_name", length = 20, nullable = false)
  private String fixed_expense_name;

  /**
   * Frecuencia con la que se repite el gasto.
   * 
   * <p>
   * Obligatorio. Máximo 15 caracteres.
   * 
   * <p>
   * <strong>Valores típicos:</strong>
   * <ul>
   * <li>"Mensual" - Una vez al mes</li>
   * <li>"Anual" - Una vez al año</li>
   * <li>"Semanal" - Una vez por semana</li>
   * <li>"Quincenal" - Cada 15 días</li>
   * <li>"Trimestral" - Cada 3 meses</li>
   * </ul>
   * 
   * <p>
   * Este valor es utilizado por la lógica de negocio para determinar
   * cuándo generar automáticamente los movimientos correspondientes.
   */
  @Column(name = "fixed_expense_frequency", length = 15, nullable = false)
  private String fixed_expense_frequency;

  /**
   * Monto del gasto fijo.
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
   * Formato en BD: {@code DECIMAL(10,2)}.
   */
  @Column(name = "fixed_expense_amount", precision = 10, scale = 2, nullable = false)
  private BigDecimal fixed_expense_amount;

  /**
   * Estado actual del gasto fijo.
   * 
   * <p>
   * Obligatorio. Máximo 15 caracteres.
   * 
   * <p>
   * <strong>Valores:</strong>
   * <ul>
   * <li><b>"Activo":</b> El gasto se aplica y genera movimientos</li>
   * <li><b>"Inactivo":</b> El gasto está pausado (no genera movimientos)</li>
   * </ul>
   * 
   * <p>
   * Útil para pausar temporalmente un gasto sin eliminarlo permanentemente.
   */
  @Column(name = "fixed_expense_status", length = 15, nullable = false)
  private String fixed_expense_status;

  /**
   * Fecha de cierre o finalización del gasto fijo.
   * 
   * <p>
   * Obligatorio. Indica el día hasta el cual el gasto está vigente.
   * 
   * <p>
   * Después de esta fecha, el sistema dejará de generar movimientos
   * automáticos para este gasto, aunque el estado siga siendo "Activo".
   * 
   * <p>
   * <strong>Ejemplo:</strong> Una suscripción anual con fecha de cierre
   * 31/12/2026 dejará de generar movimientos a partir de enero 2027.
   */
  @Column(name = "fixed_expense_closing_date", nullable = false)
  private LocalDate fixed_expense_closing_date;

  /**
   * Categoría asociada al gasto fijo.
   * 
   * <p>
   * Relación {@code @ManyToOne} opcional con {@link CategoryEntity}.
   * Carga perezosa ({@code LAZY}) para optimizar rendimiento.
   * 
   * <p>
   * Permite clasificar el gasto para reportes y análisis financieros.
   * 
   * <p>
   * En base de datos: {@code fixed_expense_fk_category_id CHAR(36)}.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "fixed_expense_fk_category_id")
  private CategoryEntity category;

  /**
   * Cuenta de usuario propietaria del gasto fijo.
   * 
   * <p>
   * Relación {@code @ManyToOne} obligatoria con {@link AccountEntity}.
   * Carga perezosa ({@code LAZY}) para optimizar rendimiento.
   * 
   * <p>
   * Cada gasto fijo pertenece a una única cuenta. La eliminación de una cuenta
   * eliminará en cascada todos sus gastos fijos ({@code CascadeType.ALL} +
   * {@code orphanRemoval = true} definido en {@link AccountEntity}).
   * 
   * <p>
   * En base de datos: {@code fixed_expense_fk_account_id CHAR(36) NOT NULL}.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "fixed_expense_fk_account_id", nullable = false)
  private AccountEntity account;
}
