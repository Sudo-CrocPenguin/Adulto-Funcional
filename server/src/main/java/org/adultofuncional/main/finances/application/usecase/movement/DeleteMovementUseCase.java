package org.adultofuncional.main.finances.application.usecase.movement;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.finances.domain.model.Movement;
import org.adultofuncional.main.finances.domain.repository.MovementRepository;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso responsable de la eliminación de un movimiento financiero
 * específico asociado a una cuenta de usuario.
 *
 * <p>Esta clase implementa la lógica de borrado asegurando que exista una
 * relación de propiedad válida entre el movimiento y la cuenta, evitando
 * que se eliminen recursos de forma cruzada entre diferentes usuarios.</p>
 *
 * <p><b>¿Qué es?</b><br>
 * Un servicio de aplicación de responsabilidad única que gestiona el proceso
 * de eliminación de registros de movimientos (ingresos/egresos). Es un
 * componente inyectado en el contenedor de Spring mediante {@code @Service}.</p>
 *
 * <p><b>¿Para qué sirve?</b><br>
 * Permite al usuario deshacer o corregir el registro de una transacción
 * eliminándola permanentemente del historial de la cuenta. Actúa como un
 * guardián de integridad al verificar que el {@code accountId} coincida
 * con el dueño del recurso antes de proceder.</p>
 *
 * <p><b>¿Cómo funciona?</b><br>
 * El método {@code execute} realiza las siguientes validaciones y acciones:</p>
 * <ol>
 *   <li>Busca el movimiento en el {@link MovementRepository} por su ID;
 *       lanza {@link NotFoundException} si no existe.</li>
 *   <li>Verifica que el {@code accountId} del movimiento recuperado sea
 *       idéntico al {@code accountId} recibido en la petición.</li>
 *   <li>Si la validación de propiedad falla, lanza un {@link NotFoundException}
 *       para no revelar la existencia de IDs de otros usuarios.</li>
 *   <li>Si todo es correcto, ejecuta la eliminación física del registro
 *       en la base de datos.</li>
 * </ol>
 *
 * @author Miguel Angel Blandon Montes
 */
@Service
@RequiredArgsConstructor
public class DeleteMovementUseCase {

    /**
     * Repositorio utilizado para la localización y eliminación de las
     * entidades de tipo movimiento en el sistema de persistencia.
     */
    private final MovementRepository movementRepository;

    // TODO: Evaluar si se debe realizar una eliminación lógica (soft delete) en lugar de física
    // TODO: Notificar al servicio de cuenta para recalcular el saldo tras la eliminación
    /**
     * Ejecuta la lógica de eliminación del movimiento identificado.
     *
     * <p>La operación está marcada como {@code @Transactional} para asegurar
     * que la verificación y la eliminación se realicen dentro del mismo
     * contexto de persistencia, garantizando la consistencia de la operación.</p>
     *
     * @param accountId identificador UUID de la cuenta que solicita la eliminación.
     * @param movementId identificador UUID del movimiento que se desea eliminar.
     * @throws NotFoundException si el movimiento no existe o no pertenece a la cuenta.
     */
    @Transactional
    public void execute(UUID accountId, UUID movementId) {
        Movement movement = movementRepository.findById(movementId)
            .orElseThrow(() -> new NotFoundException("Movimiento no encontrado con id: " + movementId));
        
        if (!movement.getAccountId().equals(accountId)) {
            throw new NotFoundException("Movimiento no pertenece a la cuenta");
        }
        
        movementRepository.deleteById(movementId);
    }
}