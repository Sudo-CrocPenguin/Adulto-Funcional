package org.adultofuncional.main.finances.application.usecase.movement;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.finances.application.dto.movement.CreateMovementRequest;
import org.adultofuncional.main.finances.application.dto.movement.MovementResponse;
import org.adultofuncional.main.finances.domain.model.Movement;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.adultofuncional.main.finances.domain.repository.MovementRepository;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso responsable de la creación y registro de nuevos movimientos
 * financieros (ingresos o egresos) asociados a una cuenta de usuario.
 *
 * <p>Esta clase orquestra el flujo de registro de transacciones, validando la
 * integridad de las relaciones con la cuenta y la categoría antes de persistir
 * el movimiento en el sistema.</p>
 *
 * <p><b>¿Qué es?</b><br>
 * Un servicio de aplicación de propósito único encargado de la lógica de negocio
 * para el registro de movimientos. Es un componente gestionado por el contenedor
 * de Spring mediante {@code @Service}.</p>
 *
 * <p><b>¿Para qué sirve?</b><br>
 * Permite registrar formalmente un flujo de dinero en una cuenta específica.
 * Garantiza que no se creen movimientos para cuentas inexistentes y opcionalmente
 * vincula el movimiento a una categoría de gastos o ingresos previamente validada.</p>
 *
 * <p><b>¿Cómo funciona?</b><br>
 * El método {@code execute} sigue una secuencia lógica de validación y creación:</p>
 * <ol>
 *   <li>Verifica la existencia de la cuenta mediante {@code accountRepository},
 *       lanzando {@link NotFoundException} si falla.</li>
 *   <li>Si la solicitud incluye una categoría, valida su existencia en el
 *       {@link CategoryRepository}.</li>
 *   <li>Instancia un nuevo objeto de dominio {@link Movement} utilizando el
 *       método de fábrica {@code create}, centralizando las reglas de inicialización.</li>
 *   <li>Persiste el movimiento y retorna un objeto {@link MovementResponse} con
 *       los datos generados (incluyendo el ID y la fecha de registro).</li>
 * </ol>
 *
 * @author Miguel Angel Blandon Montes
 */
@Service
@RequiredArgsConstructor
public class CreateMovementUseCase {

    /**
     * Repositorio para la gestión de persistencia de los movimientos financieros.
     */
    private final MovementRepository movementRepository;

    /**
     * Repositorio para la validación de existencia de la cuenta del usuario.
     */
    private final AccountRepository accountRepository;

    /**
     * Repositorio para la validación de categorías asociadas al movimiento.
     */
    private final CategoryRepository categoryRepository;

    // TODO: Implementar lógica para actualizar el saldo de la cuenta tras registrar el movimiento
    /**
     * Ejecuta el proceso de creación de un movimiento financiero.
     *
     * <p>La operación se ejecuta bajo una transacción ({@code @Transactional}),
     * lo que asegura que si la validación de categoría o la persistencia fallan,
     * no se realicen cambios parciales en el sistema.</p>
     *
     * @param accountId identificador UUID de la cuenta que registra el movimiento.
     * @param request objeto {@link CreateMovementRequest} con los datos de la transacción.
     * @return {@link MovementResponse} con la información del movimiento creado.
     * @throws NotFoundException si la cuenta o la categoría proporcionada no existen.
     */
    @Transactional
    public MovementResponse execute(UUID accountId, CreateMovementRequest request) {
        accountRepository.findById(accountId)
            .orElseThrow(() -> new NotFoundException("Cuenta no encontrada con id: " + accountId));

        UUID finalCategoryId = null;
        if (request.getCategoryId() != null) {
            finalCategoryId = request.getCategoryId();
            final UUID categoryIdToCheck = finalCategoryId;
            categoryRepository.findById(categoryIdToCheck)
                .orElseThrow(() -> new NotFoundException("Categoría no encontrada con id: " + categoryIdToCheck));
        }

        Movement movement = Movement.create(
            request.getMovementType(),
            request.getAmount(),
            finalCategoryId,
            accountId,
            request.getDescription(),
            request.getMovementDate()
        );

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