package org.adultofuncional.main.agenda.application.usecase;

import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.agenda.domain.repository.EventRepository;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso: Eliminar un evento.
 *
 * <p><strong>¿Qué es?</strong><br>
 * Servicio que elimina permanentemente un evento de la agenda.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Service
@RequiredArgsConstructor
public class DeleteEventUseCase {

    private final EventRepository eventRepository;

    @Transactional
    public void execute(UUID accountId, UUID eventId) {
        if (!eventRepository.existsByIdAndAccountId(eventId, accountId)) {
            throw new NotFoundException("Evento no encontrado con id: " + eventId);
        }
        eventRepository.deleteById(eventId);
    }
}
