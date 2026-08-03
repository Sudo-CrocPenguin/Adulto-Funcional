package org.adultofuncional.main.finances.application.usecase.movement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.adultofuncional.main.finances.application.dto.movement.MovementResponse;
import org.adultofuncional.main.finances.application.dto.movement.UpdateMovementRequest;
import org.adultofuncional.main.finances.domain.enums.MovementType;
import org.adultofuncional.main.finances.domain.enums.CategoryType;
import org.adultofuncional.main.finances.domain.model.Category;
import org.adultofuncional.main.finances.domain.model.Movement;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.adultofuncional.main.finances.domain.repository.MovementRepository;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.junit.jupiter.api.Test;

class MovementOwnershipUseCaseTest {

  @Test
  void doesNotReturnMovementOwnedByAnotherAccount() {
    MovementRepository repository = mock(MovementRepository.class);
    CategoryRepository categoryRepository = mock(CategoryRepository.class);
    GetMovementUseCase useCase = new GetMovementUseCase(repository, categoryRepository);
    UUID accountId = UUID.randomUUID();
    UUID movementId = UUID.randomUUID();

    when(repository.findByIdAndAccountId(movementId, accountId))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.execute(accountId, movementId))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  void doesNotUpdateMovementOwnedByAnotherAccount() {
    MovementRepository repository = mock(MovementRepository.class);
    CategoryRepository categoryRepository = mock(CategoryRepository.class);
    UpdateMovementUseCase useCase = new UpdateMovementUseCase(repository, categoryRepository);
    UUID accountId = UUID.randomUUID();
    UUID movementId = UUID.randomUUID();
    UpdateMovementRequest request = UpdateMovementRequest.builder()
        .description("Cambio ajeno")
        .build();

    when(repository.findByIdAndAccountId(movementId, accountId))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.execute(accountId, movementId, request))
        .isInstanceOf(NotFoundException.class);

    verify(repository, never()).save(any(Movement.class));
  }

  @Test
  void doesNotDeleteMovementOwnedByAnotherAccount() {
    MovementRepository repository = mock(MovementRepository.class);
    DeleteMovementUseCase useCase = new DeleteMovementUseCase(repository);
    UUID accountId = UUID.randomUUID();
    UUID movementId = UUID.randomUUID();

    when(repository.deleteByIdAndAccountId(movementId, accountId)).thenReturn(false);

    assertThatThrownBy(() -> useCase.execute(accountId, movementId))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  void returnsMovementToOwningAccount() {
    MovementRepository repository = mock(MovementRepository.class);
    CategoryRepository categoryRepository = mock(CategoryRepository.class);
    GetMovementUseCase useCase = new GetMovementUseCase(repository, categoryRepository);
    UUID accountId = UUID.randomUUID();
    UUID movementId = UUID.randomUUID();
    Movement movement = movement(movementId, accountId);

    when(repository.findByIdAndAccountId(movementId, accountId))
        .thenReturn(Optional.of(movement));
    when(categoryRepository.findAccessibleByIdAndType(
        accountId,
        movement.getCategoryId(),
        CategoryType.FINANCES))
        .thenReturn(Optional.of(category(movement.getCategoryId())));

    MovementResponse response = useCase.execute(accountId, movementId);

    assertThat(response.getId()).isEqualTo(movementId);
  }

  @Test
  void updatesMovementForOwningAccount() {
    MovementRepository repository = mock(MovementRepository.class);
    CategoryRepository categoryRepository = mock(CategoryRepository.class);
    UpdateMovementUseCase useCase = new UpdateMovementUseCase(repository, categoryRepository);
    UUID accountId = UUID.randomUUID();
    UUID movementId = UUID.randomUUID();
    Movement movement = movement(movementId, accountId);
    UpdateMovementRequest request = UpdateMovementRequest.builder()
        .description("Compra corregida")
        .build();

    when(repository.findByIdAndAccountId(movementId, accountId))
        .thenReturn(Optional.of(movement));
    when(categoryRepository.findAccessibleByIdAndType(
        accountId,
        movement.getCategoryId(),
        CategoryType.FINANCES))
        .thenReturn(Optional.of(category(movement.getCategoryId())));
    when(repository.save(movement)).thenReturn(movement);

    MovementResponse response = useCase.execute(accountId, movementId, request);

    assertThat(response.getDescription()).isEqualTo("Compra corregida");
    verify(repository).save(movement);
  }

  @Test
  void deletesMovementForOwningAccount() {
    MovementRepository repository = mock(MovementRepository.class);
    DeleteMovementUseCase useCase = new DeleteMovementUseCase(repository);
    UUID accountId = UUID.randomUUID();
    UUID movementId = UUID.randomUUID();
    when(repository.deleteByIdAndAccountId(movementId, accountId)).thenReturn(true);

    assertThatCode(() -> useCase.execute(accountId, movementId)).doesNotThrowAnyException();

    verify(repository).deleteByIdAndAccountId(movementId, accountId);
  }

  private Movement movement(UUID movementId, UUID accountId) {
    return Movement.reconstitute(
        movementId,
        MovementType.EXPENSE,
        new BigDecimal("25000.00"),
        UUID.randomUUID(),
        accountId,
        "Compra",
        LocalDate.now(),
        Instant.now().minusSeconds(60));
  }

  private Category category(UUID categoryId) {
    return Category.reconstitute(categoryId, "Compras", CategoryType.FINANCES);
  }
}
