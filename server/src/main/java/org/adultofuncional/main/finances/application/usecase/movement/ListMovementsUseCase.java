package org.adultofuncional.main.finances.application.usecase.movement;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.finances.application.dto.movement.MovementFilterRequest;
import org.adultofuncional.main.finances.application.dto.movement.MovementResponse;
import org.adultofuncional.main.finances.application.dto.category.CategoryResponse;
import org.adultofuncional.main.finances.domain.model.Category;
import org.adultofuncional.main.finances.domain.model.Movement;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.adultofuncional.main.finances.domain.repository.MovementRepository;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.adultofuncional.main.shared.exception.BusinessException;
import org.adultofuncional.main.shared.pagination.PageQuery;
import org.adultofuncional.main.shared.pagination.PageResult;
import org.adultofuncional.main.shared.pagination.PaginationPolicy;
import org.adultofuncional.main.shared.response.ApiErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Caso de uso: Listar los movimientos financieros de una cuenta aplicando
 * filtros opcionales.
 *
 * <p>
 * Delega al puerto una consulta que combina ownership, filtros, orden y
 * límites en SQL. Solo la página solicitada se transforma a DTO y sus
 * categorías se resuelven en lote.
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

  private static final Set<String> ALLOWED_SORTS =
      Set.of("movementDate", "amount", "movementType", "registerDate", "id");

  /** Puerto de dominio para la consulta de movimientos. */
  private final MovementRepository movementRepository;

  /** Puerto de dominio para la validación de la cuenta (módulo account). */
  private final AccountRepository accountRepository;
  private final CategoryRepository categoryRepository;

  /**
   * Ejecuta el listado filtrado de movimientos.
   *
   * @param accountId Identificador de la cuenta propietaria.
   * @param filter    Filtro opcional con tipo, categoría, rango de fechas y
   *                  término de búsqueda. Puede ser {@code null} para obtener
   *                  todos los movimientos de la cuenta.
   * @return página de {@link MovementResponse} con contenido y metadatos.
   * @throws NotFoundException si la cuenta no existe.
   */
  @Transactional(readOnly = true)
  public PageResult<MovementResponse> execute(UUID accountId, MovementFilterRequest filter) {
    accountRepository.findById(accountId)
        .orElseThrow(() -> new NotFoundException("Cuenta no encontrada con id: " + accountId));

    if (filter != null && filter.getStartDate() != null && filter.getEndDate() != null
        && filter.getStartDate().isAfter(filter.getEndDate())) {
      throw new BusinessException(
          "La fecha inicial no puede ser posterior a la fecha final",
          400,
          ApiErrorCode.PARAMETER_INVALID);
    }

    PageQuery pageQuery = PaginationPolicy.resolve(
        filter == null ? null : filter.getPage(),
        filter == null ? null : filter.getSize(),
        filter == null ? null : filter.getSortBy(),
        filter == null ? null : filter.getSortDirection(),
        "movementDate",
        false,
        ALLOWED_SORTS);
    PageResult<Movement> movements = movementRepository.findPageByAccountId(
        accountId,
        filter == null ? null : filter.getStartDate(),
        filter == null ? null : filter.getEndDate(),
        filter == null ? null : filter.getMovementType(),
        filter == null ? null : filter.getCategoryId(),
        filter != null && StringUtils.hasText(filter.getSearchTerm())
            ? filter.getSearchTerm().trim()
            : null,
        pageQuery);
    Set<UUID> categoryIds = movements.content().stream()
        .map(Movement::getCategoryId)
        .collect(Collectors.toSet());
    Map<UUID, Category> categories = categoryRepository
        .findAllAccessibleById(accountId, categoryIds).stream()
        .collect(Collectors.toMap(Category::getId, Function.identity()));

    return movements.map(m -> MovementResponse.builder()
            .id(m.getId())
            .movementType(m.getType())
            .amount(m.getAmount())
            .registerDate(m.getCreatedAt())
            .description(m.getDescription())
            .movementDate(m.getDate())
            .category(toCategoryResponse(categories.get(m.getCategoryId())))
            .build());
  }

  private CategoryResponse toCategoryResponse(Category category) {
    if (category == null) {
      return null;
    }
    return CategoryResponse.builder()
        .id(category.getId())
        .name(category.getName())
        .type(category.getType())
        .scope(category.getScope())
        .build();
  }
}
