package org.adultofuncional.main.agenda.application.usecase;

import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
import org.adultofuncional.main.agenda.application.dto.EventCategoryDTO;
import org.adultofuncional.main.agenda.application.dto.EventRequest;
import org.adultofuncional.main.agenda.application.dto.EventResponse;
import org.adultofuncional.main.agenda.domain.repository.EventRepository;
import org.adultofuncional.main.agenda.infrastructure.persistence.entity.EventEntity;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.CategoryEntity;
import org.adultofuncional.main.shared.exception.BusinessException;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso: Crear un nuevo evento en la agenda.
 *
 * <p><strong>¿Qué es?</strong><br>
 * Es un servicio de aplicación que encapsula la lógica de negocio para registrar
 * un nuevo evento en la agenda del usuario.
 *
 * <p><strong>¿Para qué sirve?</strong><br>
 * Sirve para:
 * <ul>
 *   <li>Validar que la cuenta del usuario exista</li>
 *   <li>Validar que la categoría exista (si se proporciona)</li>
 *   <li>Validar que la hora de inicio sea anterior a la hora de fin</li>
 *   <li>Establecer valores por defecto (prioridad = "Media", estado = "Pendiente")</li>
 *   <li>Crear un nuevo evento con los datos proporcionados</li>
 *   <li>Persistir el evento en la base de datos</li>
 * </ul>
 *
 * <p><strong>¿Cómo funciona?</strong><br>
 * <ol>
 *   <li>Recibe el ID de la cuenta y el DTO con los datos del evento</li>
 *   <li>Verifica que la cuenta exista en el repositorio</li>
 *   <li>Si se proporciona categoryId, verifica que la categoría exista</li>
 *   <li>Valida que startHour sea anterior a endHour</li>
 *   <li>Crea una nueva entidad EventEntity con los datos</li>
 *   <li>Establece valores por defecto si no se proporcionaron</li>
 *   <li>Persiste la entidad en el repositorio</li>
 *   <li>Retorna un DTO con los datos del evento creado</li>
 * </ol>
 *
 * @author Miguel Angel Blandon Montes
 * @version 1.0
 * @since 0.0.1
 * @see org.adultofuncional.main.agenda.domain.repository.EventRepository
 * @see org.adultofuncional.main.finances.domain.repository.CategoryRepository
 * @see EventRequest
 * @see EventResponse
 */
@Service
@RequiredArgsConstructor
public class CreateEventUseCase {

    private final EventRepository eventRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;

    // TODO: Agregar validación de frecuencia (valores permitidos: 0, 1, 7, 30, 365)
    // TODO: Agregar validación de que el recordatorio sea anterior a la fecha del evento
    // TODO: Agregar logging de auditoría para creación de eventos
    // TODO: Agregar lógica para generar eventos recurrentes automáticamente

    /**
     * Ejecuta la creación de un nuevo evento en la agenda.
     *
     * @param accountId Identificador de la cuenta. No puede ser {@code null}.
     * @param request   Objeto DTO con los datos del evento.
     * @return Un {@link EventResponse} con los datos del evento creado.
     * @throws NotFoundException  Si no existe la cuenta o categoría.
     * @throws BusinessException  Si los datos del evento no son válidos.
     */
    @Transactional
    public EventResponse execute(UUID accountId, EventRequest request) {
        // 1. Verificar que la cuenta existe
        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Cuenta no encontrada con id: " + accountId));

        // 2. Validar que startHour sea anterior a endHour
        if (request.getStartHour().isAfter(request.getEndHour())) {
            throw new BusinessException("La hora de inicio debe ser anterior a la hora de finalización");
        }

        if (request.getStartHour().isEqual(request.getEndHour())) {
            throw new BusinessException("La hora de inicio no puede ser igual a la hora de finalización");
        }

        // 3. Buscar categoría si se proporcionó
        CategoryEntity category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Categoría no encontrada con id: " + request.getCategoryId()));
        }

        // 4. Crear la entidad
        EventEntity entity = new EventEntity();
        entity.setAccount(account);
        entity.setEventTitle(request.getTitle());
        entity.setEventDate(request.getEventDate());
        entity.setEventFrequency(request.getFrequency());
        entity.setEventReminder(request.getReminder());
        entity.setEventStartHour(request.getStartHour());
        entity.setEventEndHour(request.getEndHour());
        entity.setEventDescription(request.getDescription());
        entity.setCategory(category);

        // 5. Establecer valores por defecto
        String priority = request.getPriority();
        if (priority == null || priority.isBlank()) {
            entity.setEventPriority("Media");
        } else {
            validarPrioridad(priority);
            entity.setEventPriority(priority);
        }

        String status = request.getStatus();
        if (status == null || status.isBlank()) {
            entity.setEventStatus("Pendiente");
        } else {
            validarEstado(status);
            entity.setEventStatus(status);
        }

        // 6. Persistir
        EventEntity savedEntity = eventRepository.save(entity);

        // 7. Retornar respuesta
        return mapToResponse(savedEntity);
    }

    /**
     * Valida que la prioridad sea válida.
     */
    private void validarPrioridad(String priority) {
        if (!priority.equals("Baja") && !priority.equals("Media") && !priority.equals("Alta")) {
            throw new BusinessException("La prioridad debe ser 'Baja', 'Media' o 'Alta'");
        }
    }

    /**
     * Valida que el estado sea válido.
     */
    private void validarEstado(String status) {
        if (!status.equals("Pendiente") && !status.equals("Completado") &&
            !status.equals("Cancelado") && !status.equals("Pospuesto")) {
            throw new BusinessException("El estado debe ser 'Pendiente', 'Completado', 'Cancelado' o 'Pospuesto'");
        }
    }

    /**
     * Convierte una entidad {@link EventEntity} en un DTO {@link EventResponse}.
     */
    private EventResponse mapToResponse(EventEntity entity) {
        EventCategoryDTO categoryDTO = null;
        if (entity.getCategory() != null) {
            categoryDTO = EventCategoryDTO.builder()
                    .id(entity.getCategory().getCategoryId())
                    .name(entity.getCategory().getCategoryName())
                    .type(entity.getCategory().getCategoryType())
                    .createdAt(entity.getCategory().getCategoryCreatedAt())
                    .build();
        }

        return EventResponse.builder()
                .id(entity.getEventId())
                .title(entity.getEventTitle())
                .priority(entity.getEventPriority())
                .eventDate(entity.getEventDate())
                .frequency(entity.getEventFrequency())
                .reminder(entity.getEventReminder())
                .startHour(entity.getEventStartHour())
                .endHour(entity.getEventEndHour())
                .description(entity.getEventDescription())
                .status(entity.getEventStatus())
                .category(categoryDTO)
                .build();
    }
}