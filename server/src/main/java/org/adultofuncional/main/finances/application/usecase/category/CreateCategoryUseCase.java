package org.adultofuncional.main.finances.application.usecase.category;

import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.finances.application.dto.category.CategoryResponse;
import org.adultofuncional.main.finances.application.dto.category.CreateCategoryRequest;
import org.adultofuncional.main.finances.domain.model.Category;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso responsable de la creación de una nueva categoría financiera
 * en el sistema.
 *
 * <p>Esta clase implementa el caso de uso de creación de categorías dentro
 * de la capa de aplicación, orquestando la lógica necesaria para construir
 * la entidad de dominio, persistirla y retornar la respuesta al invocador.</p>
 *
 * <p><b>¿Qué es?</b><br>
 * Un servicio de aplicación de responsabilidad única que encapsula
 * exclusivamente la lógica del caso de uso "crear categoría". Es gestionado
 * por el contenedor de Spring mediante {@code @Service}, e inyecta sus
 * dependencias a través del constructor generado por {@code @RequiredArgsConstructor}
 * de Lombok.</p>
 *
 * <p><b>¿Para qué sirve?</b><br>
 * Recibe una solicitud de creación de categoría validada, delega la construcción
 * de la entidad al modelo de dominio {@link Category}, persiste la entidad
 * a través del repositorio y retorna un {@link CategoryResponse} con los
 * datos de la categoría recién creada.</p>
 *
 * <p><b>¿Cómo funciona?</b><br>
 * El método {@code execute} es el único punto de entrada del caso de uso.
 * Recibe un {@link CreateCategoryRequest} con los datos validados, invoca
 * el método de fábrica {@code Category.create()} del dominio para construir
 * la entidad respetando las reglas de negocio, persiste la entidad mediante
 * {@link CategoryRepository#save(Category)}, y construye el {@link CategoryResponse}
 * con los datos retornados por la capa de persistencia. La anotación
 * {@code @Transactional} garantiza que la operación de escritura se ejecute
 * dentro de una transacción de base de datos, asegurando la integridad
 * del dato persistido.</p>
 *
 * @author Miguel Angel Blandon Montes
 */
@Service
@RequiredArgsConstructor
public class CreateCategoryUseCase {

    /**
     * Repositorio de categorías utilizado para persistir la nueva entidad.
     *
     * <p>Abstracción de la capa de persistencia que desacopla el caso de uso
     * de la implementación concreta del almacenamiento. Es inyectado
     * automáticamente por Spring a través del constructor generado
     * por {@code @RequiredArgsConstructor}.</p>
     */
    private final CategoryRepository categoryRepository;

    /**
     * Ejecuta el caso de uso de creación de una nueva categoría financiera.
     *
     * <p>Orquesta el flujo completo de creación: construye la entidad de dominio
     * mediante el método de fábrica {@link Category#create(String, org.adultofuncional.main.finances.domain.enums.CategoryType)},
     * la persiste a través del repositorio y retorna la representación
     * de respuesta con los datos definitivos asignados por el sistema,
     * incluyendo el identificador único y la fecha de creación.</p>
     *
     * <p>La operación se ejecuta dentro de una transacción de base de datos
     * gracias a {@code @Transactional}, garantizando que cualquier fallo
     * durante la persistencia revierta los cambios y mantenga
     * la integridad del sistema.</p>
     *
     * @param request objeto {@link CreateCategoryRequest} que contiene el nombre
     *                y el tipo de la categoría a crear, previamente validados
     *                por el framework.
     * @return {@link CategoryResponse} con los datos completos de la categoría
     *         recién creada, incluyendo su identificador UUID, nombre, tipo,
     *         fecha de creación y estado de eliminación lógica en {@code false}.
     */
    @Transactional
    public CategoryResponse execute(CreateCategoryRequest request) {
        Category category = Category.create(request.getName(), request.getType());
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