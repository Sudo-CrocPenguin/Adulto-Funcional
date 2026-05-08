package org.adultofuncional.main.finances.domain.model;

import java.util.UUID;

import org.adultofuncional.main.finances.domain.enums.CategoryType;

import com.fasterxml.uuid.Generators;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * Modelo de dominio que representa una categoría del sistema.
 *
 * <p>
 * Las categorías permiten clasificar movimientos financieros y gastos fijos
 * según su naturaleza (por ejemplo, "Alimentación", "Transporte", "Salario").
 * También pueden pertenecer al ámbito de agenda, definido por el enumerado
 * {@link CategoryType}.
 *
 * <p>
 * <strong>Responsabilidades:</strong>
 * <ul>
 * <li>Encapsular las invariantes de negocio del nombre y el tipo de
 * categoría.</li>
 * <li>Generar su propio identificador UUID v7 en el método de fábrica
 * {@link #create}, garantizando que el dominio es dueño de su identidad.</li>
 * <li>Proveer un método {@link #reconstitute} para reconstruir instancias
 * desde la capa de persistencia sin revalidar las invariantes ya
 * garantizadas por la base de datos.</li>
 * </ul>
 *
 * <p>
 * Las validaciones de formato (longitud exacta, caracteres especiales,
 * contenido HTML) se aplican en los DTOs de entrada de la capa de aplicación
 * y no se duplican aquí. La entidad solo garantiza que los valores no sean
 * nulos ni vacíos en el momento de su construcción o modificación.
 *
 * <p>
 * <strong>Datos sensibles:</strong> este modelo no contiene campos sensibles,
 * por lo que puede proyectarse completamente en los DTOs de respuesta sin
 * necesidad de filtrado adicional.
 *
 * @author Jeronimo Ospina Zapata
 * @since 0.0.1
 * @see CategoryType
 * @see org.adultofuncional.main.finances.application.dto.category.CategoryResponse
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Category {

  /**
   * Identificador único de la categoría (UUID v7).
   * Generado automáticamente en {@link #create}.
   */
  @EqualsAndHashCode.Include
  final UUID id;

  /**
   * Nombre descriptivo de la categoría.
   * No puede ser nulo ni vacío.
   */
  String name;

  /**
   * Tipo o ámbito de la categoría.
   * Valores definidos en {@link CategoryType}.
   */
  CategoryType type;

  /**
   * Constructor privado. Usar los métodos de fábrica {@link #create} o
   * {@link #reconstitute}.
   */
  private Category(UUID id, String name, CategoryType type) {
    validateId(id);
    validateName(name);
    validateType(type);

    this.id = id;
    this.name = name;
    this.type = type;
  }

  /**
   * Método de fábrica para crear una nueva categoría antes de persistirla.
   *
   * <p>
   * Genera un identificador UUID v7 mediante
   * {@link Generators#timeBasedEpochGenerator()}, garantizando que el dominio
   * sea responsable de asignar su propia identidad.
   *
   * @param name nombre de la categoría (no puede ser nulo ni vacío).
   * @param type tipo de categoría según {@link CategoryType} (no puede ser
   *             nulo).
   * @return instancia de {@code Category} lista para ser persistida.
   * @throws IllegalArgumentException si {@code name} es nulo o vacío, o si
   *                                  {@code type} es nulo.
   */
  public static Category create(String name, CategoryType type) {
    UUID id = Generators.timeBasedEpochGenerator().generate();
    return new Category(id, name, type);
  }

  /**
   * Método de fábrica para reconstituir una categoría existente desde la capa
   * de persistencia.
   *
   * <p>
   * A diferencia de {@link #create}, no genera un nuevo UUID ni aplica
   * validaciones adicionales sobre los valores ya existentes en la base de
   * datos, bajo el supuesto de que esos datos fueron validados al ser
   * persistidos por primera vez.
   *
   * @param id   identificador UUID tal como está almacenado.
   * @param name nombre de la categoría.
   * @param type tipo de categoría.
   * @return instancia de {@code Category} reconstituida.
   */
  public static Category reconstitute(UUID id, String name, CategoryType type) {
    return new Category(id, name, type);
  }

  /**
   * Actualiza el nombre de la categoría.
   *
   * @param name nuevo nombre (no puede ser nulo ni vacío).
   * @throws IllegalArgumentException si {@code name} es nulo o vacío.
   */
  public void updateName(String name) {
    validateName(name);
    this.name = name;
  }

  /**
   * Actualiza el tipo de la categoría.
   *
   * @param type nuevo tipo (no puede ser nulo).
   * @throws IllegalArgumentException si {@code type} es nulo.
   */
  public void updateType(CategoryType type) {
    validateType(type);
    this.type = type;
  }

  // ── Invariantes de negocio ────────────────────────────────────────────────

  private static void validateId(UUID id) {
    if (id == null) {
      throw new IllegalArgumentException("Id cannot be null");
    }
  }

  private static void validateName(String name) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Name cannot be null or empty");
    }
  }

  private static void validateType(CategoryType type) {
    if (type == null) {
      throw new IllegalArgumentException("Type cannot be null");
    }
  }
}
