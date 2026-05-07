package org.adultofuncional.main.finances.application.dto.movement;

import org.adultofuncional.main.finances.application.dto.category.CategoryResponse;
import org.adultofuncional.main.finances.domain.enums.MovementType;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de respuesta para un movimiento financiero.
 * <p>
 * <strong>¿Qué es?</strong><br>
 * Objeto que envía el servidor al cliente después de operaciones
 * de consulta o creación/actualización de movimientos.
 * <p>
 * <strong>¿Para qué sirve?</strong><br>
 * Entrega la información del movimiento de forma segura, sin exponer
 * datos sensibles de la cuenta propietaria.
 * <p>
 * <strong>¿Cómo funciona?</strong><br>
 * Los casos de uso recuperan una entidad {@code MovementEntity}
 * y la mapean a este DTO antes de enviarla al cliente.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Getter
@Builder
public class MovementResponse {

    private UUID id;
    private MovementType movementType;
    private BigDecimal amount;
    private LocalDateTime registerDate;
    private String description;
    private LocalDate movementDate;
    private CategoryResponse category; // opcional
}
