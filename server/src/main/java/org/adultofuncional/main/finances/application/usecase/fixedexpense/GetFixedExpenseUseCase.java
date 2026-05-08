package org.adultofuncional.main.finances.application.usecase.fixedexpense;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.finances.application.dto.fixedexpense.FixedExpenseResponse;
import org.adultofuncional.main.finances.domain.model.FixedExpense;
import org.adultofuncional.main.finances.domain.repository.FixedExpenseRepository;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso responsable de la consulta de un gasto fijo específico
 * por su identificador único en el sistema financiero.
 *
 * <p>Esta clase implementa el caso de uso de obtención de un gasto fijo
 * dentro de la capa de aplicación, orquestando la búsqueda de la entidad
 * en el repositorio y su transformación a la respuesta esperada por
 * el invocador.</p>
 *
 * <p><b>¿Qué es?</b><br>
 * Un servicio de aplicación de responsabilidad única que encapsula
 * exclusivamente la lógica del caso de uso "obtener gasto fijo por ID".
 * Es gestionado por el contenedor de Spring mediante {@code @Service},
 * e inyecta sus dependencias a través del constructor generado por
 * {@code @RequiredArgsConstructor} de Lombok.</p>
 *
 * <p><b>¿Para qué sirve?</b><br>
 * Recibe el identificador de la cuenta propietaria y el identificador único
 * del gasto fijo, lo busca en el sistema y retorna su información completa
 * como un {@link FixedExpenseResponse}. Si el gasto fijo no existe,
 * interrumpe el flujo lanzando una excepción que informa al invocador
 * del error de forma descriptiva.</p>
 *
 * <p><b>¿Cómo funciona?</b><br>
 * El método {@code execute} es el único punto de entrada del caso de uso.
 * Busca el gasto fijo en el repositorio mediante su UUID usando
 * {@code orElseThrow}, lo que lanza automáticamente una {@link NotFoundException}
 * si no se encuentra el registro. Si el gasto fijo existe, construye y retorna
 * un {@link FixedExpenseResponse} con los datos de la entidad recuperada.
 * La anotación {@code @Transactional(readOnly = true)} optimiza la operación
 * indicando al motor de persistencia que no habrá escrituras, permitiendo
 * aplicar optimizaciones de rendimiento en la sesión de base de datos.</p>
 *
 * @author Miguel Angel Blandon Montes
 */
@Service
@RequiredArgsConstructor
public class GetFixedExpenseUseCase {

    /**
     * Repositorio de gastos fijos utilizado para buscar la entidad
     * por su identificador único.
     *
     * <p>Abstracción de la capa de persistencia que desacopla el caso de uso
     * de la implementación concreta del almacenamiento. Es inyectado
     * automáticamente por Spring a través del constructor generado
     * por {@code @RequiredArgsConstructor}.</p>
     */
    private final FixedExpenseRepository fixedExpenseRepository;

    /**
     * Ejecuta el caso de uso de consulta de un gasto fijo por su UUID.
     *
     * <p>Busca el gasto fijo en el repositorio mediante su identificador único.
     * Si no se encuentra, lanza una {@link NotFoundException} de forma inmediata.
     * Si existe, construye y retorna un {@link FixedExpenseResponse} con los
     * datos completos del gasto fijo recuperado desde la capa de persistencia.</p>
     *
     * <p>La fecha de cierre del gasto fijo se obtiene del campo
     * {@code nextDueDate} de la entidad de dominio {@link FixedExpense},
     * que representa la próxima fecha de vencimiento del gasto recurrente.
     * La categoría se retorna como {@code null} en esta respuesta.</p>
     *
     * <p>La operación se ejecuta dentro de una transacción de solo lectura
     * gracias a {@code @Transactional(readOnly = true)}, lo que permite al
     * motor de persistencia aplicar optimizaciones de rendimiento al garantizar
     * que no se realizarán operaciones de escritura durante su ejecución.</p>
     *
     * @param accountId identificador UUID de la cuenta propietaria del gasto fijo.
     *                  Se recibe como parámetro de contexto para mantener la
     *                  trazabilidad de la operación, aunque la consulta se
     *                  realiza directamente por {@code expenseId}.
     * @param expenseId identificador UUID del gasto fijo que se desea consultar.
     * @return {@link FixedExpenseResponse} con los datos completos del gasto
     *         fijo encontrado, incluyendo su identificador UUID, nombre,
     *         frecuencia, monto, estado y fecha de cierre. La categoría se
     *         retorna como {@code null} en esta respuesta.
     * @throws NotFoundException si no existe ningún gasto fijo registrado
     *                           con el identificador {@code expenseId} proporcionado.
     */
    @Transactional(readOnly = true)
    public FixedExpenseResponse execute(UUID accountId, UUID expenseId) {
        FixedExpense expense = fixedExpenseRepository.findById(expenseId)
                .orElseThrow(() -> new NotFoundException("Gasto fijo no encontrado con id: " + expenseId));
        return FixedExpenseResponse.builder()
                .id(expense.getId())
                .name(expense.getName())
                .frequency(expense.getFrequency())
                .amount(expense.getAmount())
                .status(expense.getStatus())
                .closingDate(expense.getNextDueDate())
                .category(null)
                .build();
    }
}