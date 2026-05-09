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

@RestController
@RequestMapping("/api/v1/accounts/{accountId}/events")
@RequiredArgsConstructor
public class EventController {
    
    private final CreateEventUseCase createEventUseCase;
    private final GetEventUseCase getEventUseCase;
    private final ListEventsUseCase listEventsUseCase;
    private final UpdateEventUseCase updateEventUseCase;
    private final DeleteEventUseCase deleteEventUseCase;


    @PostMapping
    public ResponseEntity<EventResponse> create(@PathVariable UUID accountId, @Valid
        @RequestBody EventRequest request) {

            EventResponse response = createEventUseCase.execute(accountId, request);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponse> getById(@PathVariable UUID accountId,
        @PathVariable UUID eventId) {

        EventResponse response = getEventUseCase.execute(accountId, eventId);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> list(
        @PathVariable UUID accountId, @RequestParam(required = false) String status,
        @RequestParam(required = false) String priority,
        @RequestParam(required = false) UUID categoryId) {

            List<EventResponse> response = listEventsUseCase.execute(accountId, status, priority, categoryId);

            return ResponseEntity.ok(response);
    }


    @PatchMapping("/{eventId}")
    public ResponseEntity<EventResponse> update(@PathVariable UUID accountId, @PathVariable UUID eventId,
        @Valid @RequestBody EventUpdateRequest request) {

            EventResponse response = updateEventUseCase.execute(accountId, eventId, request);

            return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> delete(@PathVariable UUID accountId, @PathVariable UUID eventId) {

        deleteEventUseCase.execute(accountId, eventId);

        return ResponseEntity.noContent().build();
        
    }
    
}
