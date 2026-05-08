package org.adultofuncional.main.finances.application.dto.fixedexpense;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.adultofuncional.main.finances.application.dto.category.CategoryResponse;
import org.adultofuncional.main.finances.domain.enums.Frequency;
import org.adultofuncional.main.finances.domain.enums.Status;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO (Data Transfer Object) que representa la respuesta del sistema
 * al consultar un gasto fijo registrado en el sistema financiero.
 *
 * <p>
 * Esta clase encapsula la información completa de un gasto fijo que
 * el sistema retorna hacia las capas superiores (controladores, clientes
 * de la API) luego de ejecutar operaciones de consulta o creación.
 * </p>
 *
 * <p>
 * <b>¿Qué es?</b><br>
 * Un objeto de transferencia de datos inmutable que proyecta los datos
 * relevantes de un gasto fijo del dominio hacia el exterior del sistema,
 * incluyendo la información de su categoría asociada como un objeto
 * anidado de tipo {@link CategoryResponse}.
 * </p>
 *
 * <p>
 * <b>¿Para qué sirve?</b><br>
 * Transporta la información completa de un gasto fijo (identificador,
 * nombre, frecuencia, monto, estado, fecha de cierre y categoría asociada)
 * desde la capa de aplicación hasta quien consuma el servicio, sin exponer
 * directamente la entidad de dominio ni sus dependencias internas.
 * </p>
 *
 * <p>
 * <b>¿Cómo funciona?</b><br>
 * Se construye mediante el patrón Builder generado por Lombok
 * ({@code @Builder}),
 * permitiendo instanciarlo de forma clara y flexible desde los mappers o
 * casos de uso de la capa de aplicación. Los getters son generados
 * automáticamente por {@code @Getter} de Lombok. La categoría asociada
 * se incluye como un {@link CategoryResponse} completo, evitando que
 * el consumidor deba realizar consultas adicionales para obtener
 * sus datos.
 * </p>
 *
 * <p>
 * <b>Ejemplo de uso:</b>
 * </p>
 * 
 * <pre>{@code
 * FixedExpenseResponse respuesta = FixedExpenseResponse.builder()
 *     .id(UUID.randomUUID())
 *     .name("Netflix")
 *     .frequency(Frequency.MONTHLY)
 *     .amount(new BigDecimal("15.99"))
 *     .status(Status.ACTIVE)
 *     .closingDate(LocalDate.of(2025, 12, 31))
 *     .category(categoryResponse)
 *     .build();
 * }</pre>
 *
 * @author Miguel Angel Blandon Montes
 */
@Getter
@Builder
public class FixedExpenseResponse {

  /**
   * Identificador único del gasto fijo.
   *
   * <p>
   * Corresponde al UUID generado por el sistema al momento de registrar
   * el gasto fijo. Permite identificarlo de forma unívoca en todas
   * las operaciones del sistema.
   */
  private UUID id;

  /**
   * Nombre descriptivo del gasto fijo.
   *
   * <p>
   * Representa la etiqueta legible por el usuario que identifica
   * el gasto recurrente dentro del sistema financiero
   * (por ejemplo: "Netflix", "Arriendo", "Gimnasio").
   */
  private String name;

  /**
   * Frecuencia de cobro o recurrencia del gasto fijo.
   *
   * <p>
   * Corresponde a un valor del enumerado {@link Frequency} que indica
   * cada cuánto tiempo se genera el gasto dentro del sistema
   * (por ejemplo: diario, semanal, mensual, anual, entre otros).
   */
  private Frequency frequency;

  /**
   * Monto monetario del gasto fijo.
   *
   * <p>
   * Representa el valor económico del gasto recurrente. Se utiliza
   * {@link BigDecimal} para garantizar precisión en los cálculos
   * monetarios y evitar errores de redondeo propios de tipos flotantes.
   */
  private BigDecimal amount;

  /**
   * Estado operativo actual del gasto fijo.
   *
   * <p>
   * Corresponde a un valor del enumerado {@link Status} que indica
   * la situación actual del gasto recurrente dentro del sistema
   * (por ejemplo: activo, pausado, cancelado, entre otros).
   */
  private Status status;

  /**
   * Fecha de cierre o vencimiento del gasto fijo.
   *
   * <p>
   * Indica hasta cuándo está vigente el gasto recurrente en el sistema.
   * Se representa como {@link LocalDate} sin información de hora
   * ni zona horaria, dado que la vigencia opera a nivel de día calendario.
   */
  private LocalDate nextDueDate;

  /**
   * Categoría financiera asociada al gasto fijo.
   *
   * <p>
   * Contiene la información completa de la categoría a la que pertenece
   * el gasto fijo, representada como un objeto {@link CategoryResponse} anidado.
   * Si el gasto fijo no tiene categoría asociada, este campo será {@code null}.
   */
  private CategoryResponse category;
}
