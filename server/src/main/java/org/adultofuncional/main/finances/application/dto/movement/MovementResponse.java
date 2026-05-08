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
 * DTO (Data Transfer Object) que representa la respuesta del sistema
 * al consultar un movimiento financiero registrado.
 *
 * <p>Esta clase encapsula la información completa de un movimiento financiero
 * que el sistema retorna hacia las capas superiores (controladores, clientes
 * de la API) luego de ejecutar operaciones de consulta o creación.</p>
 *
 * <p><b>¿Qué es?</b><br>
 * Un objeto de transferencia de datos inmutable que proyecta los datos
 * relevantes de un movimiento financiero del dominio hacia el exterior
 * del sistema, incluyendo la información de su categoría asociada como
 * un objeto anidado de tipo {@link CategoryResponse}.</p>
 *
 * <p><b>¿Para qué sirve?</b><br>
 * Transporta la información completa de un movimiento financiero
 * (identificador, tipo, monto, fecha de registro, descripción, fecha
 * del movimiento y categoría asociada) desde la capa de aplicación
 * hasta quien consuma el servicio, sin exponer directamente la entidad
 * de dominio ni sus dependencias internas.</p>
 *
 * <p><b>¿Cómo funciona?</b><br>
 * Se construye mediante el patrón Builder generado por Lombok ({@code @Builder}),
 * permitiendo instanciarlo de forma clara y flexible desde los mappers o
 * casos de uso de la capa de aplicación. Los getters son generados
 * automáticamente por {@code @Getter} de Lombok. La categoría asociada
 * se incluye como un {@link CategoryResponse} completo, evitando que
 * el consumidor deba realizar consultas adicionales para obtener
 * sus datos. Se distinguen dos fechas: {@code registerDate} como marca
 * temporal exacta del registro en el sistema, y {@code movementDate}
 * como el día calendario en que ocurrió el movimiento real.</p>
 *
 * <p><b>Ejemplo de uso:</b></p>
 * <pre>{@code
 * MovementResponse respuesta = MovementResponse.builder()
 *         .id(UUID.randomUUID())
 *         .movementType(MovementType.EXPENSE)
 *         .amount(new BigDecimal("50.00"))
 *         .registerDate(LocalDateTime.now())
 *         .description("Compra supermercado")
 *         .movementDate(LocalDate.now())
 *         .category(categoryResponse)
 *         .build();
 * }</pre>
 *
 * @author Miguel Angel Blandon Montes
 */
@Getter
@Builder
public class MovementResponse {

    /**
     * Identificador único del movimiento financiero.
     *
     * <p>Corresponde al UUID generado por el sistema al momento de registrar
     * el movimiento. Permite identificarlo de forma unívoca en todas
     * las operaciones del sistema.</p>
     */
    private UUID id;

    /**
     * Tipo de movimiento financiero registrado.
     *
     * <p>Corresponde a un valor del enumerado {@link MovementType} que
     * clasifica el movimiento dentro del sistema financiero, indicando
     * si corresponde a un ingreso o a un egreso del usuario.</p>
     */
    private MovementType movementType;

    /**
     * Monto monetario del movimiento financiero.
     *
     * <p>Representa el valor económico del movimiento registrado.
     * Se utiliza {@link BigDecimal} para garantizar precisión en los
     * cálculos monetarios y evitar errores de redondeo propios
     * de tipos flotantes.</p>
     */
    private BigDecimal amount;

    /**
     * Fecha y hora exacta en que el movimiento fue registrado en el sistema.
     *
     * <p>Marca temporal generada automáticamente por el sistema al momento
     * de persistir el movimiento. Se representa como {@link LocalDateTime},
     * incluyendo hora, minuto y segundo del registro, sin información
     * de zona horaria. Se diferencia de {@code movementDate} en que esta
     * refleja cuándo el sistema procesó el registro, no cuándo ocurrió
     * el movimiento real.</p>
     */
    private LocalDateTime registerDate;

    /**
     * Descripción textual del movimiento financiero.
     *
     * <p>Contiene las notas o detalles que el usuario proporcionó al registrar
     * el movimiento (por ejemplo: "Compra supermercado", "Pago de servicios").
     * Si el usuario no proporcionó descripción al crear el movimiento,
     * este campo será {@code null}.</p>
     */
    private String description;

    /**
     * Fecha en la que ocurrió el movimiento financiero.
     *
     * <p>Indica el día calendario en que se realizó el ingreso o egreso,
     * tal como fue informado por el usuario al registrarlo. Se representa
     * como {@link LocalDate} sin información de hora ni zona horaria,
     * dado que el registro opera a nivel de día calendario. Se diferencia
     * de {@code registerDate} en que esta refleja cuándo ocurrió el
     * movimiento real, independientemente de cuándo fue ingresado
     * al sistema.</p>
     */
    private LocalDate movementDate;

    /**
     * Categoría financiera asociada al movimiento.
     *
     * <p>Contiene la información completa de la categoría a la que pertenece
     * el movimiento, representada como un objeto {@link CategoryResponse} anidado.
     * Si el movimiento no tiene categoría asociada, este campo será {@code null}.</p>
     */
    private CategoryResponse category;
}