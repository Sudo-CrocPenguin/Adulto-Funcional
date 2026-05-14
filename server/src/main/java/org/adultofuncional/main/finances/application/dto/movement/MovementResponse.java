package org.adultofuncional.main.finances.application.dto.movement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.adultofuncional.main.finances.application.dto.category.CategoryResponse;
import org.adultofuncional.main.finances.domain.enums.MovementType;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO de respuesta que expone los datos de un movimiento financiero.
 *
 * <p>
 * Incluye la categoría asociada como un objeto anidado
 * {@link CategoryResponse},
 * evitando que el cliente deba realizar consultas adicionales para obtener
 * sus datos. Distingue entre la fecha en que ocurrió el movimiento
 * ({@link #movementDate}) y la fecha de registro en el sistema
 * ({@link #registerDate}).
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see org.adultofuncional.main.finances.application.usecase.movement.GetMovementUseCase
 * @see org.adultofuncional.main.finances.application.usecase.movement.CreateMovementUseCase
 * @see CategoryResponse
 */
@Getter
@Builder
public class MovementResponse {

  /**
   * Identificador único del movimiento financiero.
   *
   * <p>
   * Corresponde al UUID generado por el sistema al momento de registrar
   * el movimiento. Permite identificarlo de forma unívoca en todas
   * las operaciones del sistema.
   * </p>
   */
  private UUID id;

  /**
   * Tipo de movimiento financiero registrado.
   *
   * <p>
   * Corresponde a un valor del enumerado {@link MovementType} que
   * clasifica el movimiento dentro del sistema financiero, indicando
   * si corresponde a un ingreso o a un egreso del usuario.
   * </p>
   */
  private MovementType movementType;

  /**
   * Monto monetario del movimiento financiero.
   *
   * <p>
   * Representa el valor económico del movimiento registrado.
   * Se utiliza {@link BigDecimal} para garantizar precisión en los
   * cálculos monetarios y evitar errores de redondeo propios
   * de tipos flotantes.
   * </p>
   */
  private BigDecimal amount;

  /**
   * Fecha y hora exacta en que el movimiento fue registrado en el sistema.
   *
   * <p>
   * Marca temporal generada automáticamente por el sistema al momento
   * de persistir el movimiento. Se representa como {@link LocalDateTime},
   * incluyendo hora, minuto y segundo del registro, sin información
   * de zona horaria. Se diferencia de {@code movementDate} en que esta
   * refleja cuándo el sistema procesó el registro, no cuándo ocurrió
   * el movimiento real.
   * </p>
   */
  private LocalDateTime registerDate;

  /**
   * Descripción textual del movimiento financiero.
   *
   * <p>
   * Contiene las notas o detalles que el usuario proporcionó al registrar
   * el movimiento (por ejemplo: "Compra supermercado", "Pago de servicios").
   * Si el usuario no proporcionó descripción al crear el movimiento,
   * este campo será {@code null}.
   * </p>
   */
  private String description;

  /**
   * Fecha en la que ocurrió el movimiento financiero.
   *
   * <p>
   * Indica el día calendario en que se realizó el ingreso o egreso,
   * tal como fue informado por el usuario al registrarlo. Se representa
   * como {@link LocalDate} sin información de hora ni zona horaria,
   * dado que el registro opera a nivel de día calendario. Se diferencia
   * de {@code registerDate} en que esta refleja cuándo ocurrió el
   * movimiento real, independientemente de cuándo fue ingresado
   * al sistema.
   * </p>
   */
  private LocalDate movementDate;

  /**
   * Categoría financiera asociada al movimiento.
   *
   * <p>
   * Contiene la información completa de la categoría a la que pertenece
   * el movimiento, representada como un objeto {@link CategoryResponse} anidado.
   * Si el movimiento no tiene categoría asociada, este campo será {@code null}.
   * </p>
   */
  private CategoryResponse category;
}
