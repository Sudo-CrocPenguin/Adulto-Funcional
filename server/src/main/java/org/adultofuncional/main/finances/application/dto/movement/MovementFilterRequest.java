package org.adultofuncional.main.finances.application.dto.movement;

import org.adultofuncional.main.finances.domain.enums.MovementType;
import org.adultofuncional.main.shared.security.NoHtml;

import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO para filtrar la lista de movimientos.
 * <p>
 * <strong>¿Qué es?</strong><br>
 * Objeto que encapsula los criterios de búsqueda y paginación para
 * consultar movimientos financieros.
 * <p>
 * <strong>¿Para qué sirve?</strong><br>
 * Permite al cliente filtrar por rango de fechas, tipo, categoría,
 * texto, y además especificar ordenamiento y paginación.
 * <p>
 * <strong>¿Cómo funciona?</strong><br>
 * El controlador recibe parámetros de consulta (query params) y los
 * mapea a este DTO, que se pasa al caso de uso {@code ListMovementsUseCase}.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Getter
@Builder
public class MovementFilterRequest {

    private LocalDate startDate;
    private LocalDate endDate;
    private MovementType movementType;
    private UUID categoryId;

    @Size(max = 100, message = "El término de búsqueda es demasiado largo")
    @NoHtml
    private String searchTerm;

    private String sortBy;        // movimientoDate, amount, registerDate
    private String sortDirection; // ASC, DESC
    private Integer page;
    private Integer size;

    // TODO: Agregar soporte para filtros múltiples de categorías/etiquetas
}
