package org.adultofuncional.main.finances.application.usecase.fixedexpense;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.finances.domain.repository.FixedExpenseRepository;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso responsable de la eliminación de un gasto fijo existente
 * en el sistema financiero.
 *
 * <p>Esta clase implementa el caso de uso de eliminación de gastos fijos
 * dentro de la capa de aplicación, orquestando la validación de existencia
 * de la entidad y su posterior eliminación a través del repositorio.</p>
 *
 * <p><b>¿Qué es?</b><br>
 * Un servicio de aplicación de responsabilidad única que encapsula
 * exclusivamente la lógica del caso de uso "eliminar gasto fijo". Es gestionado
 * por el contenedor de Spring mediante {@code @Service}, e inyecta sus
 * dependencias a través del constructor generado por {@code @RequiredArgsConstructor}
 * de Lombok.</p>
 *
 * <p><b>¿Para qué sirve?</b><br>
 * Recibe el identificador de la cuenta propietaria y el identificador único
 * del gasto fijo a eliminar, verifica que el gasto fijo exista en el sistema
 * antes de proceder, y lo elimina a través del repositorio. Si el gasto fijo
 * no existe, lanza una excepción que interrumpe el flujo e informa al
 * invocador del error de forma descriptiva.</p>
 *
 * <p><b>¿Cómo funciona?</b><br>
 * El método {@code execute} es el único punto de entrada del caso de uso.
 * Primero consulta el repositorio para verificar la existencia del gasto fijo
 * mediante su UUID; si no lo encuentra, lanza una {@link NotFoundException}
 * con un mensaje descriptivo que incluye el identificador buscado. Si el gasto
 * fijo existe, invoca {@link FixedExpenseRepository#deleteById(UUID)} para
 * eliminarlo del sistema. La anotación {@code @Transactional} garantiza que
 * toda la operación (verificación y eliminación) se ejecute dentro de una
 * misma transacción de base de datos, asegurando la integridad del sistema
 * ante cualquier fallo.</p>
 *
 * @author Miguel Angel Blandon Montes
 */
@Service
@RequiredArgsConstructor
public class DeleteFixedExpenseUseCase {

    /**
     * Repositorio de gastos fijos utilizado para verificar la existencia
     * y eliminar la entidad solicitada.
     *
     * <p>Abstracción de la capa de persistencia que desacopla el caso de uso
     * de la implementación concreta del almacenamiento. Es inyectado
     * automáticamente por Spring a través del constructor generado
     * por {@code @RequiredArgsConstructor}.</p>
     */
    private final FixedExpenseRepository fixedExpenseRepository;

    /**
     * Ejecuta el caso de uso de eliminación de un gasto fijo del sistema.
     *
     * <p>Verifica primero que el gasto fijo identificado por {@code expenseId}
     * exista en el sistema y, de ser así, procede a eliminarlo a través del
     * repositorio. Si el gasto fijo no es encontrado, interrumpe el flujo
     * lanzando una {@link NotFoundException} antes de realizar cualquier
     * operación de escritura.</p>
     *
     * <p>La operación se ejecuta dentro de una transacción de base de datos
     * gracias a {@code @Transactional}, garantizando que cualquier fallo
     * durante la eliminación revierta los cambios y mantenga la integridad
     * del sistema.</p>
     *
     * @param accountId identificador UUID de la cuenta propietaria del gasto fijo.
     *                  Se recibe como parámetro de contexto para mantener la
     *                  trazabilidad de la operación, aunque la eliminación se
     *                  realiza directamente por {@code expenseId}.
     * @param expenseId identificador UUID del gasto fijo que se desea eliminar.
     * @throws NotFoundException si no existe ningún gasto fijo registrado
     *                           con el identificador {@code expenseId} proporcionado.
     */
    @Transactional
    public void execute(UUID accountId, UUID expenseId) {
        if (!fixedExpenseRepository.findById(expenseId).isPresent()) {
            throw new NotFoundException("Gasto fijo no encontrado con id: " + expenseId);
        }
        fixedExpenseRepository.deleteById(expenseId);
    }
}