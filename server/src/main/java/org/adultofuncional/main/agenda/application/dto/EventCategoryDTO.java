package org.adultofuncional.main.agenda.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO (Data Transfer Object) que representa una categoría de eventos en la agenda.
 *
 * <p><strong>¿Qué es?</strong><br>
 * Es un objeto que encapsula la información de una categoría predefinida en el sistema,
 * específicamente para clasificar eventos de agenda.
 *
 * <p><strong>¿Para qué sirve?</strong><br>
 * Sirve para devolver al cliente las categorías disponibles (de tipo "Agenda")
 * para que el usuario pueda seleccionar una al crear o editar un evento.
 *
 * <p><strong>¿Cómo funciona?</strong><br>
 * Un caso de uso recupera las entidades {@code CategoryEntity} del repositorio
 * (excluyendo eliminadas lógicamente, y filtrando por tipo "Agenda"),
 * las mapea a este DTO y las retorna al cliente.
 *
 * @author Miguel Angel Blandon Montes
 * @version 1.0
 * @since 0.0.1
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventCategoryDTO {

    /**
     * Identificador único de la categoría (UUID v7).
     */
    private UUID id;

    /**
     * Nombre de la categoría.
     * Ejemplos: "Trabajo", "Personal", "Salud", "Estudio", "Familia".
     */
    private String name;

    /**
     * Tipo de categoría (siempre "Agenda" para eventos).
     */
    private String type;

    /**
     * Fecha y hora de creación de la categoría.
     */
    private LocalDateTime createdAt;

    // TODO: Agregar campo para color/icono de la categoría (opcional)
}