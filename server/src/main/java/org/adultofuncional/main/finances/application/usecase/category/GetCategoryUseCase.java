package org.adultofuncional.main.finances.application.usecase.category;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.finances.application.dto.category.CategoryResponse;
import org.adultofuncional.main.finances.domain.model.Category;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso responsable de la consulta de una categoría financiera
 * específica por su identificador único en el sistema.
 *
 * <p>Esta clase implementa el caso de uso de obtención de una categoría
 * dentro de la capa de aplicación, orquestando la búsqueda de la entidad
 * en el repositorio y su transformación a la respuesta esperada por
 * el invocador.</p>
 *
 * <p><b>¿Qué es?</b><br>
 * Un servicio de aplicación de responsabilidad única que encapsula
 * exclusivamente la lógica del caso de uso "obtener categoría por ID".
 * Es gestionado por el contenedor de Spring mediante {@code @Service},
 * e inyecta sus dependencias a través del constructor generado por
 * {@code @RequiredArgsConstructor} de Lombok.</p>
 *
 * <p><b>¿Para qué sirve?</b><br>
 * Recibe el identificador único de una categoría, la busca en el sistema
 * y retorna su información completa como un {@link CategoryResponse}.
 * Si la categoría no existe, interrumpe el flujo lanzando una excepción
 * que informa al invocador del error de forma descriptiva.</p>
 *
 * <p><b>¿Cómo funciona?</b><br>
 * El método {@code execute} es el único punto de entrada del caso de uso.
 * Busca la categoría en el repositorio mediante su UUID usando
 * {@code orElseThrow}, lo que lanza automáticamente una {@link NotFoundException}
 * si no se encuentra el registro. Si la categoría existe, construye y retorna
 * un {@link CategoryResponse} con los datos de la entidad recuperada.
 * La anotación {@code @Transactional(readOnly = true)} optimiza la operación
 * indicando al motor de persistencia que no habrá escrituras, lo que permite
 * aplicar optimizaciones de rendimiento en la sesión de base de datos.</p>
 *
 * @author Miguel Angel Blandon Montes
 */
@Service
@RequiredArgsConstructor
public class GetCategoryUseCase {

    /**
     * Repositorio de categorías utilizado para buscar la entidad
     * por su identificador único.
     *
     * <p>Abstracción de la capa de persistencia que desacopla el caso de uso
     * de la implementación concreta del almacenamiento. Es inyectado
     * automáticamente por Spring a través del constructor generado
     * por {@code @RequiredArgsConstructor}.</p>
     */
    private final CategoryRepository categoryRepository;

    /**
     * Ejecuta el caso de uso de consulta de una categoría financiera por su UUID.
     *
     * <p>Busca la categoría en el repositorio mediante su identificador único.
     * Si no se encuentra, lanza una {@link NotFoundException} de forma inmediata.
     * Si existe, construye y retorna un {@link CategoryResponse} con los datos
     * completos de la categoría recuperada desde la capa de persistencia.</p>
     *
     * <p>La operación se ejecuta dentro de una transacción de solo lectura
     * gracias a {@code @Transactional(readOnly = true)}, lo que permite al
     * motor de persistencia aplicar optimizaciones de rendimiento al garantizar
     * que no se realizarán operaciones de escritura durante su ejecución.</p>
     *
     * @param categoryId identificador UUID de la categoría que se desea consultar.
     * @return {@link CategoryResponse} con los datos completos de la categoría
     *         encontrada, incluyendo su identificador UUID, nombre, tipo,
     *         fecha de creación y estado de eliminación lógica en {@code false}.
     * @throws NotFoundException si no existe ninguna categoría registrada
     *                           con el identificador proporcionado.
     */
    @Transactional(readOnly = true)
    public CategoryResponse execute(UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Categoría no encontrada con id: " + categoryId));
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .type(category.getType())
                .createdAt(category.getCreatedAt())
                .deleted(false)
                .build();
    }
}