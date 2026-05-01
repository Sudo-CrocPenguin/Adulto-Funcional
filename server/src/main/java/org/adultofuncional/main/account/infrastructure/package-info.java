/**
 * Capa de infraestructura del módulo de cuentas.
 *
 * <p>
 * Implementa los adaptadores que conectan el dominio con tecnologías
 * externas: Spring Data JPA, mapeo de entidades y exposición REST.
 * </p>
 *
 * <p>
 * Componentes:
 * <ul>
 * <li>{@code controller} — {@code AccountController} (REST API)</li>
 * <li>{@code persistence} — Entidad JPA {@code AccountEntity} y mapeador</li>
 * <li>{@code repository} — Implementación {@code AccountRepositoryImpl}</li>
 * </ul>
 * </p>
 *
 * @author Lydis Ester Jaraba, Juan Sebastian Rios, Jeronimo Ospina Zapata
 * @since 0.0.1
 */
package org.adultofuncional.main.account.infrastructure;
