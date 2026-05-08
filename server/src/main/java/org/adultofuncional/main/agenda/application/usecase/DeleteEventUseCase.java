package org.adultofuncional.main.agenda.application.usecase;

import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.agenda.domain.repository.EventRepository;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso: Eliminar un evento de la agenda.
 *
 * <p>
 * Verifica que el evento exista y pertenezca a la cuenta indicada, y lo
 * elimina permanentemente del sistema. La verificación de propiedad se realiza
 * mediante {@link EventRepository#existsByIdAndAccountId(UUID, UUID)} antes de
 * proceder a la eliminación.
 *
 * <p>
 * La operación se ejecuta dentro de una transacción ({@code @Transactional}),
 * garantizando la integridad en caso de fallo durante la eliminación.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see EventRepository
 * @see NotFoundException
 */
@Service
@RequiredArgsConstructor
public class DeleteEventUseCase {

  private final EventRepository eventRepository;

  /**
   * Ejecuta la eliminación de un evento por su identificador.
   *
   * @param accountId Identificador de la cuenta propietaria (usado para
   *                  verificar propiedad). No puede ser {@code null}.
   * @param eventId   Identificador del evento a eliminar. No puede ser
   *                  {@code null}.
   * @throws NotFoundException si no existe un evento con el ID dado que
   *                           pertenezca a la cuenta indicada.
   */
  @Transactional
  public void execute(UUID accountId, UUID eventId) {
    if (!eventRepository.existsByIdAndAccountId(eventId, accountId)) {
      throw new NotFoundException("Evento no encontrado con id: " + eventId);
    }
    eventRepository.deleteById(eventId);
  }
}
