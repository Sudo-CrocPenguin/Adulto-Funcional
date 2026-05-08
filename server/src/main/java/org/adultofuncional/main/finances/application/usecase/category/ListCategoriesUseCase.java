package org.adultofuncional.main.finances.application.usecase.category;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.finances.application.dto.category.CategoryFilterRequest;
import org.adultofuncional.main.finances.application.dto.category.CategoryResponse;
import org.adultofuncional.main.finances.domain.model.Category;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso responsable de la consulta y listado de categorías financieras
 * registradas en el sistema, con soporte de filtrado opcional por tipo.
 *
 * <p>Esta clase implementa el caso de uso de listado de categorías dentro
 * de la capa de aplicación, orquestando la recuperación de todas las entidades
 * desde el repositorio, la aplicación de filtros en memoria y la transformación
 * de los resultados a la respuesta esperada por el invocador.</p>
 *
 * <p><b>¿Qué es?</b><br>
 * Un servicio de aplicación de responsabilidad única que encapsula
 * exclusivamente la lógica del caso de uso "listar categorías". Es gestionado
 * por el contenedor de Spring mediante {@code @Service}, e inyecta sus
 * dependencias a través del constructor generado por {@code @RequiredArgsConstructor}
 * de Lombok.</p>
 *
 * <p><b>¿Para qué sirve?</b><br>
 * Recupera todas las categorías disponibles en el sistema y, de forma opcional,
 * las filtra por tipo según los criterios definidos en el {@link CategoryFilterRequest}.
 * Retorna la lista resultante como una colección de {@link CategoryResponse},
 * lista para ser consumida por las capas superiores.</p>
 *
 * <p><b>¿Cómo funciona?</b><br>
 * El método {@code execute} es el único punto de entrada del caso de uso.
 * Primero recupera todas las categorías desde el repositorio mediante
 * {@code findAll()}. Luego, si el filtro proporcionado no es nulo y contiene
 * un tipo definido, aplica un filtrado en memoria sobre la lista recuperada
 * usando la API de Streams de Java, conservando únicamente las categorías
 * cuyo tipo coincida con el solicitado. Finalmente, transforma cada entidad
 * {@link Category} en un {@link CategoryResponse} mediante un mapeo con
 * Streams y retorna la lista resultante. La anotación
 * {@code @Transactional(readOnly = true)} optimiza la operación indicando
 * al motor de persistencia que no habrá escrituras durante su ejecución.</p>
 *
 * @author Miguel Angel Blandon Montes
 */
@Service
@RequiredArgsConstructor
public class ListCategoriesUseCase {

    /**
     * Repositorio de categorías utilizado para recuperar todas las entidades
     * disponibles en el sistema.
     *
     * <p>Abstracción de la capa de persistencia que desacopla el caso de uso
     * de la implementación concreta del almacenamiento. Es inyectado
     * automáticamente por Spring a través del constructor generado
     * por {@code @RequiredArgsConstructor}.</p>
     */
    private final CategoryRepository categoryRepository;

    /**
     * Ejecuta el caso de uso de listado de categorías financieras con
     * filtrado opcional por tipo.
     *
     * <p>Recupera la totalidad de las categorías registradas en el sistema
     * y aplica, si corresponde, un filtro en memoria por tipo de categoría
     * según los criterios del {@link CategoryFilterRequest} recibido.
     * Transforma cada entidad resultante en un {@link CategoryResponse}
     * y retorna la lista completa al invocador.</p>
     *
     * <p>El filtrado por tipo se aplica únicamente cuando el parámetro
     * {@code filter} no es {@code null} y su campo {@code type} contiene
     * un valor definido. Si el filtro es {@code null} o su tipo es
     * {@code null}, se retornan todas las categorías sin distinción.</p>
     *
     * <p>La operación se ejecuta dentro de una transacción de solo lectura
     * gracias a {@code @Transactional(readOnly = true)}, lo que permite al
     * motor de persistencia aplicar optimizaciones de rendimiento al garantizar
     * que no se realizarán operaciones de escritura durante su ejecución.</p>
     *
     * @param filter objeto {@link CategoryFilterRequest} que contiene los criterios
     *               de filtrado a aplicar sobre el listado. Puede ser {@code null},
     *               en cuyo caso se retornan todas las categorías disponibles.
     * @return lista de {@link CategoryResponse} con los datos de todas las categorías
     *         que cumplen los criterios del filtro, o todas las categorías si no
     *         se proporcionó ningún criterio. Retorna una lista vacía si no existen
     *         categorías que coincidan.
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> execute(CategoryFilterRequest filter) {
        List<Category> categories = categoryRepository.findAll();
        if (filter != null && filter.getType() != null) {
            categories = categories.stream()
                    .filter(c -> c.getType() == filter.getType())
                    .collect(Collectors.toList());
        }
        return categories.stream()
                .map(c -> CategoryResponse.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .type(c.getType())
                        .createdAt(c.getCreatedAt())
                        .deleted(false)
                        .build())
                .collect(Collectors.toList());
    }
}