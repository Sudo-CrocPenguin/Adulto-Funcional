package org.adultofuncional.main.agenda.infrastructure.controller;

import java.util.List;
import java.util.UUID;

import org.adultofuncional.main.agenda.application.dto.EventRequest;
import org.adultofuncional.main.agenda.application.dto.EventResponse;
import org.adultofuncional.main.agenda.application.dto.EventUpdateRequest;
import org.adultofuncional.main.agenda.application.usecase.CreateEventUseCase;
import org.adultofuncional.main.agenda.application.usecase.DeleteEventUseCase;
import org.adultofuncional.main.agenda.application.usecase.GetEventUseCase;
import org.adultofuncional.main.agenda.application.usecase.ListEventsUseCase;
import org.adultofuncional.main.agenda.application.usecase.UpdateEventUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST que expone los endpoints del módulo de agenda.
 *
 * <p>
 * Traduce las peticiones HTTP en llamadas a los casos de uso
 * correspondientes y devuelve los DTOs de respuesta con el código de estado
 * adecuado.
 *
 * <h2>Endpoints expuestos</h2>
 * <pre>
 * POST    /api/v1/accounts/{accountId}/events             → crear evento
 * GET     /api/v1/accounts/{accountId}/events/{eventId}   → obtener evento
 * GET     /api/v1/accounts/{accountId}/events             → listar eventos
 * PATCH   /api/v1/accounts/{accountId}/events/{eventId}   → actualizar parcialmente
 * DELETE  /api/v1/accounts/{accountId}/events/{eventId}   → eliminar evento
 * </pre>
 *
 * <p>
 * El segmento {@code {accountId}} en la URL garantiza que todas las
 * operaciones quedan acotadas a la cuenta propietaria, facilitando la
 * autorización a nivel de recurso.
 *
 * @author Lidys Jaraba
 * @since 0.0.1
 * @see CreateEventUseCase
 * @see GetEventUseCase
 * @see ListEventsUseCase
 * @see UpdateEventUseCase
 * @see DeleteEventUseCase
 */

@RestController
@RequestMapping("/api/v1/accounts/{accountId}/events")
@RequiredArgsConstructor
public class EventController {
    
    private final CreateEventUseCase createEventUseCase;
    private final GetEventUseCase getEventUseCase;
    private final ListEventsUseCase listEventsUseCase;
    private final UpdateEventUseCase updateEventUseCase;
    private final DeleteEventUseCase deleteEventUseCase;

    /**
     * Crea un nuevo evento en la agenda de la cuenta indicada.
     *
     * @param accountId identificador de la cuenta propietaria.
     * @param request   datos del evento a crear; validado con Bean Validation.
     * @return {@code 201 Created} con el {@link EventResponse} del evento
     *         persistido.
     */

    @PostMapping
    public ResponseEntity<EventResponse> create(@PathVariable UUID accountId, @Valid
        @RequestBody EventRequest request) {

            EventResponse response = createEventUseCase.execute(accountId, request);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtiene un evento específico por su identificador.
     *
     * @param accountId identificador de la cuenta propietaria.
     * @param eventId   identificador del evento.
     * @return {@code 200 OK} con el {@link EventResponse} del evento.
     * @throws org.adultofuncional.main.shared.exception.NotFoundException si el
     *         evento no existe o no pertenece a la cuenta.
     */

    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponse> getById(@PathVariable UUID accountId,
        @PathVariable UUID eventId) {

        EventResponse response = getEventUseCase.execute(accountId, eventId);

        return ResponseEntity.ok(response);
    }

    /**
     * Lista los eventos de la cuenta con filtros opcionales.
     *
     * @param accountId  identificador de la cuenta propietaria.
     * @param status     filtra por estado (ej. {@code "Pendiente"}); opcional.
     * @param priority   filtra por prioridad (ej. {@code "Alta"}); opcional.
     * @param categoryId filtra por categoría asociada; opcional.
     * @return {@code 200 OK} con la lista de {@link EventResponse} que cumplen
     *         los criterios. Puede ser una lista vacía.
     */

    @GetMapping
    public ResponseEntity<List<EventResponse>> list(
        @PathVariable UUID accountId, @RequestParam(required = false) String status,
        @RequestParam(required = false) String priority,
        @RequestParam(required = false) UUID categoryId) {

            List<EventResponse> response = listEventsUseCase.execute(accountId, status, priority, categoryId);

            return ResponseEntity.ok(response);
    }

    /**
     * Actualiza parcialmente un evento existente (semántica PATCH).
     *
     * <p>
     * Solo los campos presentes en el cuerpo de la petición son modificados;
     * los demás conservan su valor actual.
     *
     * @param accountId identificador de la cuenta propietaria.
     * @param eventId   identificador del evento a actualizar.
     * @param request   campos a modificar; validado con Bean Validation.
     * @return {@code 200 OK} con el {@link EventResponse} actualizado.
     * @throws org.adultofuncional.main.shared.exception.NotFoundException si el
     *         evento no existe o no pertenece a la cuenta.
     * @throws org.adultofuncional.main.shared.exception.BusinessException si la
     *         hora de inicio es posterior a la de fin.
     */

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventResponse> update(@PathVariable UUID accountId, @PathVariable UUID eventId,
        @Valid @RequestBody EventUpdateRequest request) {

            EventResponse response = updateEventUseCase.execute(accountId, eventId, request);

            return ResponseEntity.ok(response);
    }

    /**
     * Elimina un evento de la agenda.
     *
     * @param accountId identificador de la cuenta propietaria.
     * @param eventId   identificador del evento a eliminar.
     * @return {@code 204 No Content} si la eliminación fue exitosa.
     * @throws org.adultofuncional.main.shared.exception.NotFoundException si el
     *         evento no existe o no pertenece a la cuenta.
     */

    
    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> delete(@PathVariable UUID accountId, @PathVariable UUID eventId) {

        deleteEventUseCase.execute(accountId, eventId);

        return ResponseEntity.noContent().build();
        
    }
    
}
