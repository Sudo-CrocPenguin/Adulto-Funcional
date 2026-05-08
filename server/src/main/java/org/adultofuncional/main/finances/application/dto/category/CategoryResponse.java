package org.adultofuncional.main.finances.application.dto.category;

import java.util.UUID;

import org.adultofuncional.main.finances.domain.enums.CategoryType;
import org.adultofuncional.main.shared.security.OwnedResource;

import lombok.Builder;
import lombok.Getter;

/**
 * DTO (Data Transfer Object) que representa la respuesta del sistema
 * al consultar una categoría financiera.
 *
 * <p>
 * Esta clase encapsula la información de una categoría que el sistema
 * retorna hacia las capas superiores (controladores, clientes de la API)
 * luego de ejecutar operaciones de consulta o creación.
 * </p>
 *
 * <p>
 * <b>¿Qué es?</b><br>
 * Un objeto de transferencia de datos inmutable que proyecta los datos
 * relevantes de una categoría del dominio hacia el exterior del sistema,
 * implementando la interfaz {@link OwnedResource} del módulo de seguridad
 * compartido.
 * </p>
 *
 * <p>
 * <b>¿Para qué sirve?</b><br>
 * Transporta la información de una categoría (identificador, nombre, tipo,
 * fecha de creación y estado de eliminación lógica) desde la capa de
 * aplicación hasta quien consuma el servicio, sin exponer directamente
 * la entidad de dominio.
 * </p>
 *
 * <p>
 * <b>¿Cómo funciona?</b><br>
 * Se construye mediante el patrón Builder generado por Lombok
 * ({@code @Builder}),
 * permitiendo instanciarla de forma clara y flexible. Los getters son generados
 * automáticamente por {@code @Getter} de Lombok. Al implementar
 * {@link OwnedResource},
 * se integra con el mecanismo de seguridad del sistema; en este DTO el método
 * {@code getEmail()} retorna {@code null} dado que la categoría no está
 * asociada
 * directamente a un propietario identificable por correo en la capa de
 * respuesta.
 * </p>
 *
 * <p>
 * <b>Ejemplo de uso:</b>
 * </p>
 * 
 * <pre>{@code
 * CategoryResponse respuesta = CategoryResponse.builder()
 *     .id(UUID.randomUUID())
 *     .name("Alimentación")
 *     .type(CategoryType.EXPENSE)
 *     .createdAt(LocalDateTime.now())
 *     .deleted(false)
 *     .build();
 * }</pre>
 *
 * @author Miguel Angel Blandon Montes
 */
@Getter
@Builder
public class CategoryResponse implements OwnedResource {

  /**
   * Identificador único de la categoría.
   *
   * <p>
   * Corresponde al UUID generado por el sistema al momento de crear
   * la categoría. Permite identificarla de forma unívoca en todas
   * las operaciones del sistema.
   * </p>
   */
  private UUID id;

  /**
   * Nombre descriptivo de la categoría.
   *
   * <p>
   * Representa la etiqueta legible por el usuario que identifica
   * la categoría dentro del sistema financiero (por ejemplo:
   * "Alimentación", "Transporte", "Salario").
   * </p>
   */
  private String name;

  /**
   * Tipo de la categoría según la clasificación del dominio financiero.
   *
   * <p>
   * Corresponde a un valor del enumerado {@link CategoryType}, que
   * determina la naturaleza de la categoría dentro del sistema
   * (por ejemplo: ingreso, gasto, ahorro, entre otros).
   * </p>
   */
  private CategoryType type;

  /**
   * Retorna el correo electrónico del propietario del recurso.
   *
   * <p>
   * Implementación del contrato definido por la interfaz {@link OwnedResource}
   * del módulo de seguridad compartido. En este DTO de respuesta de categoría,
   * retorna {@code null} dado que las categorías no están asociadas
   * directamente a un propietario identificable por correo electrónico
   * en la capa de presentación.
   * </p>
   *
   * @return {@code null} en todos los casos para este DTO.
   */
  @Override
  public String getEmail() {
    return null;
  }
}
