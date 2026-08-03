package org.adultofuncional.main.finances.application.usecase.movement;

import java.time.Clock;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.finances.application.dto.movement.CreateMovementRequest;
import org.adultofuncional.main.finances.application.dto.movement.MovementResponse;
import org.adultofuncional.main.finances.application.dto.category.CategoryResponse;
import org.adultofuncional.main.finances.domain.enums.CategoryType;
import org.adultofuncional.main.finances.domain.model.Category;
import org.adultofuncional.main.finances.domain.model.Movement;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.adultofuncional.main.finances.domain.repository.MovementRepository;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: Registrar un nuevo movimiento financiero (ingreso o egreso)
 * en una cuenta.
 *
 * <p>
 * Reglas de negocio:
 * <ul>
 * <li>La cuenta debe existir.</li>
 * <li>La categoría debe ser {@code SYSTEM} o pertenecer a la cuenta y tener
 * tipo {@code FINANCES}.</li>
 * </ul>
 *
 * <p>
 * La creación del modelo {@link Movement} se delega al método de fábrica
 * del dominio. La respuesta incluye la categoría accesible que fue validada.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see Movement
 * @see MovementRepository
 * @see AccountRepository
 * @see CategoryRepository
 */
@Service
@RequiredArgsConstructor
public class CreateMovementUseCase {

  /** Puerto de dominio para la persistencia de movimientos. */
  private final MovementRepository movementRepository;

  /** Puerto de dominio para la validación de la cuenta (módulo account). */
  private final AccountRepository accountRepository;

  /** Puerto de dominio para la validación de la categoría. */
  private final CategoryRepository categoryRepository;

  /** Reloj UTC usado para la fecha técnica de registro. */
  private final Clock clock;

  /**
   * Ejecuta la creación de un nuevo movimiento.
   *
   * @param accountId Identificador de la cuenta en la que se registra el
   *                  movimiento.
   * @param request   DTO con los datos validados del movimiento (tipo,
   *                  monto, fecha, descripción y categoría obligatoria).
   * @return {@link MovementResponse} con el movimiento y su categoría.
   * @throws NotFoundException si la cuenta o la categoría no existen.
   */
  @Transactional
  public MovementResponse execute(UUID accountId, CreateMovementRequest request) {
    accountRepository.findById(accountId)
        .orElseThrow(() -> new NotFoundException("Cuenta no encontrada con id: " + accountId));

    UUID finalCategoryId = request.getCategoryId();
    Category category = categoryRepository.findAccessibleByIdAndType(
            accountId,
            finalCategoryId,
            CategoryType.FINANCES)
        .orElseThrow(() -> new NotFoundException("Categoría no encontrada con id: " + finalCategoryId));

    Movement movement = Movement.create(
        request.getMovementType(),
        request.getAmount(),
        finalCategoryId,
        accountId,
        request.getDescription(),
        request.getMovementDate(),
        clock);

    Movement saved = movementRepository.save(movement);
    return MovementResponse.builder()
        .id(saved.getId())
        .movementType(saved.getType())
        .amount(saved.getAmount())
        .registerDate(saved.getCreatedAt())
        .description(saved.getDescription())
        .movementDate(saved.getDate())
        .category(toCategoryResponse(category))
        .build();
  }

  private CategoryResponse toCategoryResponse(Category category) {
    return CategoryResponse.builder()
        .id(category.getId())
        .name(category.getName())
        .type(category.getType())
        .scope(category.getScope())
        .build();
  }
}
