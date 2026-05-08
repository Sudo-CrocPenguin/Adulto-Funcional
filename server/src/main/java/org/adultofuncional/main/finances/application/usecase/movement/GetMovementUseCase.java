package org.adultofuncional.main.finances.application.usecase.movement;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.finances.application.dto.movement.MovementResponse;
import org.adultofuncional.main.finances.domain.model.Movement;
import org.adultofuncional.main.finances.domain.repository.MovementRepository;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso responsable de la consulta detallada de un movimiento financiero
 * específico asociado a una cuenta de usuario.
 *
 * <p>Esta clase implementa la lógica de recuperación de datos dentro de la capa
 * de aplicación, asegurando que el recurso solicitado pertenezca efectivamente
 * al contexto de la cuenta proporcionada antes de exponer su información.</p>
 *
 * <p><b>¿Qué es?</b><br>
 * Un servicio de aplicación de lectura única que encapsula la lógica para
 * "obtener el detalle de un movimiento". Es gestionado por Spring (@code @Service)
 * e inyecta sus dependencias mediante el constructor generado por Lombok.</p>
 *
 * <p><b>¿Para qué sirve?</b><br>
 * Permite recuperar toda la información relevante de una transacción individual
 * (ya sea ingreso o egreso). Actúa como un filtro de seguridad para evitar
 * el acceso a movimientos de otras cuentas mediante la validación del
 * {@code accountId}.</p>
 *
 * <p><b>¿Cómo funciona?</b><br>
 * El método {@code execute} aplica el siguiente flujo de control:</p>
 * <ol>
 *   <li>Busca la entidad {@link Movement} en el repositorio mediante su ID único.</li>
 *   <li>Si el movimiento no existe, lanza una {@link NotFoundException}.</li>
 *   <li>Verifica si el {@code accountId} del movimiento coincide con el de la
 *       solicitud. Si no hay coincidencia, lanza {@link NotFoundException}
 *       para proteger la privacidad de los datos.</li>
 *   <li>Mapea los atributos de la entidad a un objeto de transferencia
 *       {@link MovementResponse} utilizando el patrón Builder.</li>
 * </ol>
 *
 * <p>La anotación {@code @Transactional(readOnly = true)} garantiza una
 * operación de consulta optimizada a nivel de base de datos.</p>
 *
 * @author Miguel Angel Blandon Montes
 */
@Service
@RequiredArgsConstructor
public class GetMovementUseCase {

    /**
     * Repositorio de movimientos utilizado para la búsqueda y recuperación
     * de la información persistida en el sistema.
     */
    private final MovementRepository movementRepository;

    // TODO: Evaluar la inclusión de los detalles de la categoría asociada en la respuesta
    /**
     * Ejecuta la consulta del detalle de un movimiento financiero.
     *
     * <p>Valida la propiedad del recurso antes de retornarlo. En el mapeo a
     * {@link MovementResponse}, la categoría se establece actualmente como
     * {@code null} siguiendo la arquitectura actual de respuestas simples.</p>
     *
     * @param accountId identificador UUID de la cuenta que solicita el detalle.
     * @param movementId identificador UUID del movimiento que se desea consultar.
     * @return {@link MovementResponse} con los datos detallados de la transacción.
     * @throws NotFoundException si el movimiento no existe o si se intenta
     *                           acceder a un recurso que no pertenece a la cuenta.
     */
    @Transactional(readOnly = true)
    public MovementResponse execute(UUID accountId, UUID movementId) {
        Movement movement = movementRepository.findById(movementId)
            .orElseThrow(() -> new NotFoundException("Movimiento no encontrado con id: " + movementId));
        
        if (!movement.getAccountId().equals(accountId)) {
            throw new NotFoundException("Movimiento no pertenece a la cuenta");
        }
        
        return MovementResponse.builder()
            .id(movement.getId())
            .movementType(movement.getType())
            .amount(movement.getAmount())
            .registerDate(movement.getCreatedAt())
            .description(movement.getDescription())
            .movementDate(movement.getDate())
            .category(null)
            .build();
    }
}