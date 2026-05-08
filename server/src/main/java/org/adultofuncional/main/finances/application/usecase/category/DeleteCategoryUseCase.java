package org.adultofuncional.main.finances.application.usecase.category;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso responsable de la eliminación lógica de una categoría financiera
 * existente en el sistema.
 *
 * <p>Esta clase implementa el caso de uso de eliminación de categorías dentro
 * de la capa de aplicación, orquestando la validación de existencia de la
 * entidad y su posterior eliminación a través del repositorio.</p>
 *
 * <p><b>¿Qué es?</b><br>
 * Un servicio de aplicación de responsabilidad única que encapsula
 * exclusivamente la lógica del caso de uso "eliminar categoría". Es gestionado
 * por el contenedor de Spring mediante {@code @Service}, e inyecta sus
 * dependencias a través del constructor generado por {@code @RequiredArgsConstructor}
 * de Lombok.</p>
 *
 * <p><b>¿Para qué sirve?</b><br>
 * Recibe el identificador único de una categoría, verifica que exista en el
 * sistema antes de proceder, y la elimina a través del repositorio. Si la
 * categoría no existe, lanza una excepción que interrumpe el flujo e informa
 * al invocador del error.</p>
 *
 * <p><b>¿Cómo funciona?</b><br>
 * El método {@code execute} es el único punto de entrada del caso de uso.
 * Primero consulta el repositorio para verificar la existencia de la categoría
 * mediante su UUID; si no la encuentra, lanza una {@link NotFoundException}
 * con un mensaje descriptivo. Si la categoría existe, invoca
 * {@link CategoryRepository#deleteById(UUID)} para eliminarla del sistema.
 * La anotación {@code @Transactional} garantiza que toda la operación
 * (verificación y eliminación) se ejecute dentro de una misma transacción
 * de base de datos, asegurando la integridad del sistema ante cualquier fallo.</p>
 *
 * @author Miguel Angel Blandon Montes
 */
@Service
@RequiredArgsConstructor
public class DeleteCategoryUseCase {

    /**
     * Repositorio de categorías utilizado para verificar la existencia
     * y eliminar la entidad solicitada.
     *
     * <p>Abstracción de la capa de persistencia que desacopla el caso de uso
     * de la implementación concreta del almacenamiento. Es inyectado
     * automáticamente por Spring a través del constructor generado
     * por {@code @RequiredArgsConstructor}.</p>
     */
    private final CategoryRepository categoryRepository;

    /**
     * Ejecuta el caso de uso de eliminación de una categoría financiera.
     *
     * <p>Orquesta el flujo completo de eliminación: verifica primero que la
     * categoría exista en el sistema mediante su identificador único y, de
     * ser así, procede a eliminarla a través del repositorio. Si la categoría
     * no es encontrada, interrumpe el flujo lanzando una excepción antes
     * de realizar cualquier operación de escritura.</p>
     *
     * <p>La operación se ejecuta dentro de una transacción de base de datos
     * gracias a {@code @Transactional}, garantizando que cualquier fallo
     * durante la eliminación revierta los cambios y mantenga
     * la integridad del sistema.</p>
     *
     * @param categoryId identificador UUID de la categoría que se desea eliminar.
     * @throws NotFoundException si no existe ninguna categoría registrada
     *                           con el identificador proporcionado.
     */
    @Transactional
    public void execute(UUID categoryId) {
        if (!categoryRepository.findById(categoryId).isPresent()) {
            throw new NotFoundException("Categoría no encontrada con id: " + categoryId);
        }
        categoryRepository.deleteById(categoryId);
    }
}