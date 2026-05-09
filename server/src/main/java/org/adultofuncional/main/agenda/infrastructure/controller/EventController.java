package org.adultofuncional.main.agenda.infrastructure.controller;

import java.util.List;
import java.util.UUID;

import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.agenda.application.dto.EventRequest;
import org.adultofuncional.main.agenda.application.dto.EventResponse;
import org.adultofuncional.main.agenda.application.dto.EventUpdateRequest;
import org.adultofuncional.main.agenda.application.usecase.CreateEventUseCase;
import org.adultofuncional.main.agenda.application.usecase.DeleteEventUseCase;
import org.adultofuncional.main.agenda.application.usecase.GetEventUseCase;
import org.adultofuncional.main.agenda.application.usecase.ListEventsUseCase;
import org.adultofuncional.main.agenda.application.usecase.UpdateEventUseCase;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.adultofuncional.main.shared.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
 * Controlador REST del módulo de agenda personal.
 *
 * <p>
 * Expone endpoints para gestionar eventos bajo la ruta base
 * {@code /api/agenda/events}. Delega la lógica de negocio a los casos de uso
 * correspondientes y retorna las respuestas envueltas en
 * {@link ApiResponse}, manteniendo la consistencia con el resto de la API.
 *
 * <p>
 * El {@code accountId} se resuelve internamente a partir del correo
 * electrónico del usuario autenticado mediante
 * {@link #resolveAccountId(String)},
 * evitando que el cliente manipule identificadores de cuenta en la URL.
 *
 * <h2>Endpoints expuestos</h2>
 * 
 * <pre>
 * POST   /api/agenda/events             → crear evento
 * GET    /api/agenda/events/{eventId}   → obtener evento por ID
 * GET    /api/agenda/events             → listar eventos (filtros opcionales)
 * PATCH  /api/agenda/events/{eventId}   → actualizar parcialmente
 * DELETE /api/agenda/events/{eventId}   → eliminar evento
 * </pre>
 *
 * @author Lidys Jaraba
 * @since 0.0.1
 * @see CreateEventUseCase
 * @see GetEventUseCase
 * @see ListEventsUseCase
 * @see UpdateEventUseCase
 * @see DeleteEventUseCase
 * @see ApiResponse
 */
@RestController
@RequestMapping("/api/agenda/events")
@RequiredArgsConstructor
public class EventController {

  private final CreateEventUseCase createEventUseCase;
  private final GetEventUseCase getEventUseCase;
  private final ListEventsUseCase listEventsUseCase;
  private final UpdateEventUseCase updateEventUseCase;
  private final DeleteEventUseCase deleteEventUseCase;
  private final AccountRepository accountRepository;

  /**
   * Resuelve el identificador único de la cuenta a partir del correo
   * electrónico del usuario autenticado.
   *
   * <p>
   * Consulta el {@link AccountRepository} buscando la cuenta asociada
   * al correo proporcionado. Si no existe una cuenta con ese correo,
   * lanza {@link NotFoundException} interrumpiendo el flujo del endpoint.
   *
   * @param email correo electrónico del usuario autenticado, obtenido
   *              desde el contexto de seguridad mediante
   *              {@code @AuthenticationPrincipal}.
   * @return UUID de la cuenta asociada al correo electrónico.
   * @throws NotFoundException si no existe ninguna cuenta con el email dado.
   */
  private UUID resolveAccountId(String email) {
    return accountRepository.findByEmail(email)
        .orElseThrow(() -> new NotFoundException("Cuenta no encontrada para el email: " + email))
        .getId();
  }

  /**
   * Crea un nuevo evento en la agenda del usuario autenticado.
   *
   * @param request     DTO con los datos validados del evento a crear.
   * @param loggedEmail correo electrónico del usuario autenticado.
   * @return {@code 201 Created} con el {@link EventResponse} del evento
   *         creado envuelto en un {@link ApiResponse}.
   * @throws NotFoundException si la cuenta del usuario no existe.
   */
  @PostMapping
  public ResponseEntity<ApiResponse<EventResponse>> create(
      @Valid @RequestBody EventRequest request,
      @AuthenticationPrincipal String loggedEmail) {

    UUID accountId = resolveAccountId(loggedEmail);
    EventResponse response = createEventUseCase.execute(accountId, request);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.<EventResponse>builder()
            .status(HttpStatus.CREATED.value())
            .message("Evento creado exitosamente")
            .data(response)
            .build());
  }

  /**
   * Obtiene un evento específico de la agenda del usuario autenticado.
   *
   * @param eventId     UUID del evento a consultar.
   * @param loggedEmail correo electrónico del usuario autenticado.
   * @return {@code 200 OK} con el {@link EventResponse} del evento.
   * @throws NotFoundException si el evento no existe o no pertenece a la
   *                           cuenta.
   */
  @GetMapping("/{eventId}")
  public ResponseEntity<ApiResponse<EventResponse>> getById(
      @PathVariable UUID eventId,
      @AuthenticationPrincipal String loggedEmail) {

    UUID accountId = resolveAccountId(loggedEmail);
    EventResponse response = getEventUseCase.execute(accountId, eventId);

    return ResponseEntity.ok(ApiResponse.<EventResponse>builder()
        .status(HttpStatus.OK.value())
        .message("Evento obtenido exitosamente")
        .data(response)
        .build());
  }

  /**
   * Lista los eventos de la agenda del usuario autenticado con filtros
   * opcionales.
   *
   * @param status      estado a filtrar (ej. {@code "Pendiente"}); opcional.
   * @param priority    prioridad a filtrar (ej. {@code "Alta"}); opcional.
   * @param categoryId  categoría a filtrar; opcional.
   * @param loggedEmail correo electrónico del usuario autenticado.
   * @return {@code 200 OK} con la lista de {@link EventResponse} que
   *         cumplen los criterios. Puede ser una lista vacía.
   * @throws NotFoundException si la cuenta del usuario no existe.
   */
  @GetMapping
  public ResponseEntity<ApiResponse<List<EventResponse>>> list(
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String priority,
      @RequestParam(required = false) UUID categoryId,
      @AuthenticationPrincipal String loggedEmail) {

    UUID accountId = resolveAccountId(loggedEmail);
    List<EventResponse> response = listEventsUseCase.execute(accountId, status, priority, categoryId);

    return ResponseEntity.ok(ApiResponse.<List<EventResponse>>builder()
        .status(HttpStatus.OK.value())
        .message("Eventos listados exitosamente")
        .data(response)
        .build());
  }

  /**
   * Actualiza parcialmente un evento existente (semántica PATCH).
   *
   * <p>
   * Solo los campos presentes en el cuerpo de la petición son modificados;
   * los demás conservan su valor actual.
   *
   * @param eventId     UUID del evento a actualizar.
   * @param request     DTO con los campos a modificar; validado con Bean
   *                    Validation.
   * @param loggedEmail correo electrónico del usuario autenticado.
   * @return {@code 200 OK} con el {@link EventResponse} actualizado.
   * @throws NotFoundException si el evento no existe o no pertenece a la
   *                           cuenta.
   * @throws BusinessException si la hora de inicio es posterior a la de fin.
   */
  @PatchMapping("/{eventId}")
  public ResponseEntity<ApiResponse<EventResponse>> update(
      @PathVariable UUID eventId,
      @Valid @RequestBody EventUpdateRequest request,
      @AuthenticationPrincipal String loggedEmail) {

    UUID accountId = resolveAccountId(loggedEmail);
    EventResponse response = updateEventUseCase.execute(accountId, eventId, request);

    return ResponseEntity.ok(ApiResponse.<EventResponse>builder()
        .status(HttpStatus.OK.value())
        .message("Evento actualizado exitosamente")
        .data(response)
        .build());
  }

  /**
   * Elimina un evento de la agenda del usuario autenticado.
   *
   * @param eventId     UUID del evento a eliminar.
   * @param loggedEmail correo electrónico del usuario autenticado.
   * @return {@code 200 OK} con mensaje de confirmación.
   * @throws NotFoundException si el evento no existe o no pertenece a la
   *                           cuenta.
   */
  @DeleteMapping("/{eventId}")
  public ResponseEntity<ApiResponse<Void>> delete(
      @PathVariable UUID eventId,
      @AuthenticationPrincipal String loggedEmail) {

    UUID accountId = resolveAccountId(loggedEmail);
    deleteEventUseCase.execute(accountId, eventId);

    return ResponseEntity.ok(ApiResponse.<Void>builder()
        .status(HttpStatus.OK.value())
        .message("Evento eliminado exitosamente")
        .build());
  }
}
