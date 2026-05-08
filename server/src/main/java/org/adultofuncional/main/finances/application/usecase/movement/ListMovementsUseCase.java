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
 * Caso de uso: Listar los movimientos financieros de una cuenta aplicando
 * filtros opcionales.
 *
 * <p>
 * Recupera todos los movimientos de la cuenta desde
 * {@link MovementRepository#findAllByAccountId} y aplica en memoria los filtros
 * proporcionados: tipo de movimiento, categoría, rango de fechas
 * ({@code startDate} / {@code endDate}) y término de búsqueda sobre la
 * descripción.
 *
 * <p>
 * <strong>Filtros soportados (todos opcionales):</strong>
 * <ul>
 * <li>{@code movementType} — filtra por tipo ({@code INCOME} o
 * {@code EXPENSE}).</li>
 * <li>{@code categoryId} — filtra por categoría asociada.</li>
 * <li>{@code startDate} / {@code endDate} — rango de fechas del
 * movimiento.</li>
 * <li>{@code searchTerm} — búsqueda insensible a mayúsculas sobre la
 * descripción.</li>
 * </ul>
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see MovementRepository
 * @see AccountRepository
 */
@Service
@RequiredArgsConstructor
public class ListMovementsUseCase {

  /** Puerto de dominio para la consulta de movimientos. */
  private final MovementRepository movementRepository;

  /** Puerto de dominio para la validación de la cuenta (módulo account). */
  private final AccountRepository accountRepository;

  /**
   * Ejecuta el listado filtrado de movimientos.
   *
   * @param accountId Identificador de la cuenta propietaria.
   * @param filter    Filtro opcional con tipo, categoría, rango de fechas y
   *                  término de búsqueda. Puede ser {@code null} para obtener
   *                  todos los movimientos de la cuenta.
   * @return Lista de {@link MovementResponse} con los movimientos que
   *         cumplen los criterios. La categoría se retorna como
   *         {@code null} en esta versión.
   * @throws NotFoundException si la cuenta no existe.
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
            .filter(m -> m.getDescription() != null &&
                m.getDescription().toLowerCase().contains(term))
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
