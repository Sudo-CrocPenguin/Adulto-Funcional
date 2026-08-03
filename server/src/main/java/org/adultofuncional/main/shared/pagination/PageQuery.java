package org.adultofuncional.main.shared.pagination;

/**
 * Consulta de paginación independiente del framework de persistencia.
 *
 * <p><strong>Qué es:</strong> un valor inmutable con página, tamaño, campo
 * lógico de orden y dirección.</p>
 *
 * <p><strong>Para qué sirve:</strong> permite que los puertos de dominio
 * expresen límites y orden sin depender de {@code Pageable} de Spring.</p>
 *
 * <p><strong>Cómo funciona:</strong> se construye exclusivamente mediante
 * {@link PaginationPolicy}, que valida el contrato público antes de que el
 * adaptador traduzca el campo lógico a una propiedad JPA.</p>
 */
public record PageQuery(int number, int size, String sortBy, boolean ascending) {
}
