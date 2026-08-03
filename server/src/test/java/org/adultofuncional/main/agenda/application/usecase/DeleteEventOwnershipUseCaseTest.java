package org.adultofuncional.main.agenda.application.usecase;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.adultofuncional.main.agenda.domain.repository.EventRepository;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.junit.jupiter.api.Test;

class DeleteEventOwnershipUseCaseTest {

  @Test
  void doesNotDeleteEventOwnedByAnotherAccount() {
    EventRepository repository = mock(EventRepository.class);
    DeleteEventUseCase useCase = new DeleteEventUseCase(repository);
    UUID accountId = UUID.randomUUID();
    UUID eventId = UUID.randomUUID();

    when(repository.deleteByIdAndAccountId(eventId, accountId)).thenReturn(false);

    assertThatThrownBy(() -> useCase.execute(accountId, eventId))
        .isInstanceOf(NotFoundException.class);

    verify(repository).deleteByIdAndAccountId(eventId, accountId);
  }

  @Test
  void deletesEventForOwningAccount() {
    EventRepository repository = mock(EventRepository.class);
    DeleteEventUseCase useCase = new DeleteEventUseCase(repository);
    UUID accountId = UUID.randomUUID();
    UUID eventId = UUID.randomUUID();

    when(repository.deleteByIdAndAccountId(eventId, accountId)).thenReturn(true);

    assertThatCode(() -> useCase.execute(accountId, eventId)).doesNotThrowAnyException();

    verify(repository).deleteByIdAndAccountId(eventId, accountId);
  }
}
