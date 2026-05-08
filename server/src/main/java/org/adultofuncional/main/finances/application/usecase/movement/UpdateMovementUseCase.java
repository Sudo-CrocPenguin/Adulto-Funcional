package org.adultofuncional.main.finances.application.usecase.movement;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.finances.application.dto.movement.MovementResponse;
import org.adultofuncional.main.finances.application.dto.movement.UpdateMovementRequest;
import org.adultofuncional.main.finances.domain.model.Movement;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.adultofuncional.main.finances.domain.repository.MovementRepository;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Caso de uso responsable de la actualización parcial de la información
 * de un movimiento financiero existente.
 *
 * <p>Esta clase implementa la lógica necesaria para modificar los atributos de
 * un movimiento, asegurando que el recurso pertenezca a la cuenta del usuario
 * y validando la integridad de las categorías asociadas antes de persistir
 * los cambios.</p>
 *
 * <p><b>¿Qué es?</b><br>
 * Un servicio de aplicación de responsabilidad única encargado de gestionar la
 * edición de registros financieros. Es gestionado por Spring mediante
 * {@code @Service} e inyecta sus dependencias de forma automática.</p>
 *
 * <p><b>¿Para qué sirve?</b><br>
 * Permite al usuario corregir o actualizar datos de un movimiento previo, como
 * el monto, la descripción, la fecha o la categoría. Actúa como un filtro de
 * integridad de negocio y seguridad de acceso a datos.</p>
 *
 * <p><b>¿Cómo funciona?</b><br>
 * El método {@code execute} orquestra la actualización siguiendo estos pasos:</p>
 * <ol>
 *   <li>Localiza el movimiento por su ID único; lanza {@link NotFoundException}
 *        si no existe.</li>
 *   <li>Valida que el {@code accountId} del movimiento coincida con el de la
 *       solicitud para garantizar la propiedad del recurso.</li>
 *   <li>Evalúa cada campo del {@link UpdateMovementRequest} de forma independiente:
 *       <ul>
 *         <li>Si el campo está presente, invoca el método {@code update} del
 *             modelo de dominio para sincronizar el estado.</li>
 *         <li>Si se actualiza la categoría, verifica previamente su existencia
 *             en el {@link CategoryRepository}.</li>
 *       </ul>
 *   </li>
 *   <li>Persiste la entidad actualizada y retorna un {@link MovementResponse}
 *       con los datos finales del registro.</li>
 * </ol>
 *
 * @author Miguel Angel Blandon Montes
 */
@Service
@RequiredArgsConstructor
public class UpdateMovementUseCase {

    /**
     * Repositorio de movimientos utilizado para la recuperación y actualización
     * de la entidad en el sistema de persistencia.
     */
    private final MovementRepository movementRepository;

    /**
     * Repositorio de categorías utilizado para validar la existencia de la
     * nueva categoría asignada al movimiento.
     */
    private final CategoryRepository categoryRepository;

    // TODO: Optimizar las llamadas al método update del dominio para evitar múltiples reasignaciones en una sola ejecución
    // TODO: Notificar al servicio de saldos si el monto o el tipo de movimiento han cambiado
    /**
     * Ejecuta el proceso de actualización del movimiento financiero.
     *
     * <p>La operación es transaccional, garantizando que todas las validaciones
     * y la persistencia final se completen satisfactoriamente o se realice un
     * rollback en caso de error.</p>
     *
     * @param accountId identificador UUID de la cuenta propietaria del movimiento.
     * @param movementId identificador UUID del movimiento que se desea actualizar.
     * @param request objeto {@link UpdateMovementRequest} con los datos a modificar.
     * @return {@link MovementResponse} con el estado actualizado de la transacción.
     * @throws NotFoundException si el movimiento o la nueva categoría no existen,
     *                           o si el movimiento no pertenece a la cuenta.
     */
    @Transactional
    public MovementResponse execute(UUID accountId, UUID movementId, UpdateMovementRequest request) {
        Movement movement = movementRepository.findById(movementId)
            .orElseThrow(() -> new NotFoundException("Movimiento no encontrado con id: " + movementId));
        
        if (!movement.getAccountId().equals(accountId)) {
            throw new NotFoundException("Movimiento no pertenece a la cuenta");
        }

        if (request.getMovementType() != null) {
            movement.update(
                request.getMovementType(),
                movement.getAmount(),
                movement.getCategoryId(),
                movement.getDescription(),
                movement.getDate()
            );
        }
        if (request.getAmount() != null) {
            movement.update(
                movement.getType(),
                request.getAmount(),
                movement.getCategoryId(),
                movement.getDescription(),
                movement.getDate()
            );
        }
        if (StringUtils.hasText(request.getDescription())) {
            movement.update(
                movement.getType(),
                movement.getAmount(),
                movement.getCategoryId(),
                request.getDescription(),
                movement.getDate()
            );
        }
        if (request.getMovementDate() != null) {
            movement.update(
                movement.getType(),
                movement.getAmount(),
                movement.getCategoryId(),
                movement.getDescription(),
                request.getMovementDate()
            );
        }
        if (request.getCategoryId() != null) {
            categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Categoría no encontrada con id: " + request.getCategoryId()));
            movement.update(
                movement.getType(),
                movement.getAmount(),
                request.getCategoryId(),
                movement.getDescription(),
                movement.getDate()
            );
        }

        Movement saved = movementRepository.save(movement);
        return MovementResponse.builder()
            .id(saved.getId())
            .movementType(saved.getType())
            .amount(saved.getAmount())
            .registerDate(saved.getCreatedAt())
            .description(saved.getDescription())
            .movementDate(saved.getDate())
            .category(null)
            .build();
    }
}