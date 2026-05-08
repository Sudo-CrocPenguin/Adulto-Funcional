package org.adultofuncional.main.finances.application.usecase.category;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.finances.application.dto.category.CategoryResponse;
import org.adultofuncional.main.finances.application.dto.category.UpdateCategoryRequest;
import org.adultofuncional.main.finances.domain.model.Category;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Caso de uso responsable de la actualización parcial de una categoría
 * financiera existente en el sistema.
 *
 * <p>Esta clase implementa el caso de uso de actualización de categorías
 * dentro de la capa de aplicación, orquestando la búsqueda de la entidad,
 * la aplicación selectiva de los cambios solicitados y su posterior
 * persistencia.</p>
 *
 * <p><b>¿Qué es?</b><br>
 * Un servicio de aplicación de responsabilidad única que encapsula
 * exclusivamente la lógica del caso de uso "actualizar categoría". Es gestionado
 * por el contenedor de Spring mediante {@code @Service}, e inyecta sus
 * dependencias a través del constructor generado por {@code @RequiredArgsConstructor}
 * de Lombok.</p>
 *
 * <p><b>¿Para qué sirve?</b><br>
 * Recibe el identificador de una categoría existente y un conjunto de campos
 * opcionales a modificar. Aplica únicamente los cambios que fueron
 * proporcionados, respetando el principio de actualización parcial (PATCH),
 * y retorna la representación actualizada de la categoría como un
 * {@link CategoryResponse}.</p>
 *
 * <p><b>¿Cómo funciona?</b><br>
 * El método {@code execute} es el único punto de entrada del caso de uso.
 * Primero busca la categoría en el repositorio por su UUID, lanzando una
 * {@link NotFoundException} si no existe. Luego aplica los cambios de forma
 * selectiva: actualiza el nombre solo si el valor proporcionado tiene texto
 * real (verificado con {@link StringUtils#hasText}), y actualiza el tipo
 * solo si no es nulo. Cada cambio se delega al modelo de dominio
 * {@link Category} a través de sus métodos de actualización, respetando
 * el encapsulamiento de las reglas de negocio. Finalmente persiste la entidad
 * modificada y construye el {@link CategoryResponse} con los datos actualizados.
 * La anotación {@code @Transactional} garantiza la integridad de toda
 * la operación dentro de una única transacción de base de datos.</p>
 *
 * @author Miguel Angel Blandon Montes
 */
@Service
@RequiredArgsConstructor
public class UpdateCategoryUseCase {

    /**
     * Repositorio de categorías utilizado para buscar y persistir
     * la entidad modificada.
     *
     * <p>Abstracción de la capa de persistencia que desacopla el caso de uso
     * de la implementación concreta del almacenamiento. Es inyectado
     * automáticamente por Spring a través del constructor generado
     * por {@code @RequiredArgsConstructor}.</p>
     */
    private final CategoryRepository categoryRepository;

    /**
     * Ejecuta el caso de uso de actualización parcial de una categoría financiera.
     *
     * <p>Busca la categoría por su identificador único y aplica selectivamente
     * los cambios contenidos en el {@link UpdateCategoryRequest}: actualiza el
     * nombre si se proporcionó un valor con texto real, y actualiza el tipo si
     * se proporcionó un valor no nulo. Los campos no proporcionados conservan
     * su valor actual sin modificación. Una vez aplicados los cambios, persiste
     * la entidad y retorna su representación actualizada.</p>
     *
     * <p>La verificación del nombre se realiza con {@link StringUtils#hasText},
     * que garantiza que el valor no sea nulo, vacío ni compuesto únicamente
     * por espacios en blanco, evitando sobrescribir el nombre con valores
     * sin contenido significativo.</p>
     *
     * <p>La operación se ejecuta dentro de una transacción de base de datos
     * gracias a {@code @Transactional}, garantizando que cualquier fallo
     * durante la actualización revierta los cambios y mantenga
     * la integridad del sistema.</p>
     *
     * @param categoryId identificador UUID de la categoría que se desea actualizar.
     * @param request    objeto {@link UpdateCategoryRequest} que contiene los campos
     *                   a modificar. Todos sus campos son opcionales; los que sean
     *                   nulos o vacíos serán ignorados durante la actualización.
     * @return {@link CategoryResponse} con los datos completos y actualizados de la
     *         categoría, incluyendo su identificador UUID, nombre, tipo, fecha de
     *         creación y estado de eliminación lógica en {@code false}.
     * @throws NotFoundException si no existe ninguna categoría registrada
     *                           con el identificador proporcionado.
     */
    @Transactional
    public CategoryResponse execute(UUID categoryId, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Categoría no encontrada con id: " + categoryId));
        if (StringUtils.hasText(request.getName())) {
            category.updateName(request.getName());
        }
        if (request.getType() != null) {
            category.updateType(request.getType());
        }
        Category saved = categoryRepository.save(category);
        return CategoryResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .type(saved.getType())
                .createdAt(saved.getCreatedAt())
                .deleted(false)
                .build();
    }
}