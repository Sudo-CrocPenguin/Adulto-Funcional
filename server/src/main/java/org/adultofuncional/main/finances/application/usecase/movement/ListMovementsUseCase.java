package org.adultofuncional.main.finances.application.usecase.movement;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.finances.application.dto.movement.MovementFilterRequest;
import org.adultofuncional.main.finances.application.dto.movement.MovementResponse;
import org.adultofuncional.main.finances.domain.model.Movement;
import org.adultofuncional.main.finances.domain.repository.MovementRepository;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Caso de uso responsable de la consulta y listado filtrado de movimientos
 * financieros (ingresos y egresos) asociados a una cuenta de usuario.
 *
 * <p>Esta clase implementa la lógica de recuperación múltiple dentro de la capa
 * de aplicación, permitiendo aplicar diversos criterios de búsqueda sobre el
 * historial de transacciones de forma flexible.</p>
 *
 * <p><b>¿Qué es?</b><br>
 * Un servicio de aplicación de responsabilidad única diseñado para la lectura y
 * filtrado del historial de movimientos. Utiliza el contenedor de Spring
 * mediante {@code @Service} y gestiona sus dependencias a través de
 * {@code @RequiredArgsConstructor}.</p>
 *
 * <p><b>¿Para qué sirve?</b><br>
 * Proporciona una interfaz para obtener todos los registros financieros de una
 * cuenta específica, permitiendo al usuario acotar los resultados mediante
 * filtros de tipo de movimiento, categoría, rangos de fechas y términos de
 * búsqueda en la descripción.</p>
 *
 * <p><b>¿Cómo funciona?</b><br>
 * El método {@code execute} orquestra el flujo de recuperación de la siguiente manera:</p>
 * <ol>
 *   <li>Valida la existencia de la cuenta mediante el {@link AccountRepository};
 *       si no existe, lanza {@link NotFoundException}.</li>
 *   <li>Recupera la totalidad de los movimientos vinculados a la cuenta desde el
 *       {@link MovementRepository}.</li>
 *   <li>Si se proporciona un objeto {@link MovementFilterRequest}, aplica de forma
 *       encadenada y en memoria los filtros activos:
 *       <ul>
 *         <li>Por tipo de movimiento (Ingreso/Egreso).</li>
 *         <li>Por identificador de categoría.</li>
 *         <li>Por rango cronológico (fecha de inicio y fecha de fin).</li>
 *         <li>Por término de búsqueda (insensible a mayúsculas en la descripción).</li>
 *       </ul>
 *   </li>
 *   <li>Transforma la lista resultante de entidades {@link Movement} a una
 *       colección de {@link MovementResponse} mediante Streams.</li>
 * </ol>
 *
 * <p>La anotación {@code @Transactional(readOnly = true)} optimiza la consulta
 * garantizando que no se realicen bloqueos de escritura en la base de datos.</p>
 *
 * @author Miguel Angel Blandon Montes
 */
@Service
@RequiredArgsConstructor
public class ListMovementsUseCase {

    /**
     * Repositorio de movimientos utilizado para recuperar el historial completo
     * de transacciones asociadas a la cuenta.
     */
    private final MovementRepository movementRepository;

    /**
     * Repositorio de cuentas utilizado para validar que la cuenta consultada
     * sea válida antes de proceder con el listado.
     */
    private final AccountRepository accountRepository;

    // TODO: Evaluar la delegación del filtrado a nivel de base de datos (Query Methods o Criteria) para mejorar el rendimiento con grandes volúmenes de datos
    /**
     * Ejecuta el listado de movimientos aplicando los criterios de filtrado.
     *
     * <p>El filtrado por descripción se realiza mediante una comparación de
     * subcadena ({@code contains}) tras normalizar a minúsculas, asegurando
     * que la búsqueda sea amigable para el usuario.</p>
     *
     * @param accountId identificador UUID de la cuenta cuyos movimientos se listarán.
     * @param filter objeto {@link MovementFilterRequest} con los parámetros de búsqueda opcionales.
     * @return lista de {@link MovementResponse} con los movimientos que cumplen los criterios.
     * @throws NotFoundException si la cuenta proporcionada no existe en el sistema.
     */
    @Transactional(readOnly = true)
    public List<MovementResponse> execute(UUID accountId, MovementFilterRequest filter) {
        accountRepository.findById(accountId)
            .orElseThrow(() -> new NotFoundException("Cuenta no encontrada con id: " + accountId));

        List<Movement> movements = movementRepository.findAllByAccountId(accountId);
        if (filter != null) {
            if (filter.getMovementType() != null) {
                movements = movements.stream()
                    .filter(m -> m.getType() == filter.getMovementType())
                    .collect(Collectors.toList());
            }
            if (filter.getCategoryId() != null) {
                movements = movements.stream()
                    .filter(m -> m.getCategoryId() != null && m.getCategoryId().equals(filter.getCategoryId()))
                    .collect(Collectors.toList());
            }
            if (filter.getStartDate() != null) {
                movements = movements.stream()
                    .filter(m -> !m.getDate().isBefore(filter.getStartDate()))
                    .collect(Collectors.toList());
            }
            if (filter.getEndDate() != null) {
                movements = movements.stream()
                    .filter(m -> !m.getDate().isAfter(filter.getEndDate()))
                    .collect(Collectors.toList());
            }
            if (StringUtils.hasText(filter.getSearchTerm())) {
                String term = filter.getSearchTerm().toLowerCase();
                movements = movements.stream()
                    .filter(m -> m.getDescription() != null && m.getDescription().toLowerCase().contains(term))
                    .collect(Collectors.toList());
            }
        }
        return movements.stream()
            .map(m -> MovementResponse.builder()
                .id(m.getId())
                .movementType(m.getType())
                .amount(m.getAmount())
                .registerDate(m.getCreatedAt())
                .description(m.getDescription())
                .movementDate(m.getDate())
                .category(null)
                .build())
            .collect(Collectors.toList());
    }
}