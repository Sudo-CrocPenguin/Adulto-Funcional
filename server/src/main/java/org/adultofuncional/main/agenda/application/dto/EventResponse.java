package org.adultofuncional.main.agenda.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de respuesta para un evento.
 *
 * <p><strong>¿Qué es?</strong><br>
 * Objeto que devuelve la información completa de un evento.
 *
 * <p><strong>¿Para qué sirve?</strong><br>
 * Expone los datos no sensibles del evento al cliente.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Getter
@Builder
public class EventResponse {

    private UUID id;
    private String title;
    private String priority;
    private LocalDate eventDate;
    private Integer frequency;
    private LocalDateTime reminder;
    private LocalDateTime startHour;
    private LocalDateTime endHour;
    private String description;
    private String status;
    private EventCategoryDTO category;
}
cat > src/main/java/org/adultofuncional/main/agenda/application/usecase/CreateEventUseCase.java << 'EOF'
package org.adultofuncional.main.agenda.application.usecase;

import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.agenda.application.dto.EventCategoryDTO;
import org.adultofuncional.main.agenda.application.dto.EventRequest;
import org.adultofuncional.main.agenda.application.dto.EventResponse;
import org.adultofuncional.main.agenda.domain.model.Event;
import org.adultofuncional.main.agenda.domain.repository.EventRepository;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.adultofuncional.main.shared.exception.BusinessException;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso: Crear un nuevo evento.
 *
 * <p><strong>¿Qué es?</strong><br>
 * Servicio que registra un evento en la agenda de un usuario.
 *
 * <p><strong>¿Para qué sirve?</strong><br>
 * Valida la existencia de la cuenta y la categoría (opcional), y que la hora
 * de inicio sea anterior a la hora de fin. Establece valores por defecto.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Service
@RequiredArgsConstructor
public class CreateEventUseCase {

    private final EventRepository eventRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;

    // TODO: Validar valores de frecuencia permitidos (0,1,7,30,365)
    // TODO: Validar que el recordatorio sea anterior a la fecha del evento

    @Transactional
    public EventResponse execute(UUID accountId, EventRequest request) {
        // 1. Verificar cuenta
        accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Cuenta no encontrada con id: " + accountId));

        // 2. Validar horario
        if (request.getStartHour().isAfter(request.getEndHour()) || request.getStartHour().isEqual(request.getEndHour())) {
            throw new BusinessException("La hora de inicio debe ser anterior a la hora de fin");
        }

        // 3. Buscar categoría (opcional)
        var category = request.getCategoryId() != null
                ? categoryRepository.findById(request.getCategoryId())
                        .orElseThrow(() -> new NotFoundException("Categoría no encontrada"))
                : null;

        // 4. Valores por defecto
        String priority = request.getPriority() != null ? request.getPriority() : "Media";
        String status = request.getStatus() != null ? request.getStatus() : "Pendiente";

        // 5. Crear modelo de dominio
        Event event = Event.create(
                accountId,
                request.getTitle(),
                priority,
                request.getEventDate(),
                request.getFrequency(),
                request.getReminder(),
                request.getStartHour(),
                request.getEndHour(),
                request.getDescription(),
                status,
                category != null ? category.getCategoryId() : null
        );

        Event saved = eventRepository.save(event);

        // 6. Mapear respuesta
        return mapToResponse(saved, category);
    }

    private EventResponse mapToResponse(Event event, org.adultofuncional.main.finances.infrastructure.persistence.entity.CategoryEntity category) {
        EventCategoryDTO catDto = null;
        if (category != null) {
            catDto = EventCategoryDTO.builder()
                    .id(category.getCategoryId())
                    .name(category.getCategoryName())
                    .type(category.getCategoryType())
                    .createdAt(category.getCategoryCreatedAt())
                    .build();
        }
        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .priority(event.getPriority())
                .eventDate(event.getEventDate())
                .frequency(event.getFrequency())
                .reminder(event.getReminder())
                .startHour(event.getStartHour())
                .endHour(event.getEndHour())
                .description(event.getDescription())
                .status(event.getStatus())
                .category(catDto)
                .build();
    }
}
