/**
 * Capa de infraestructura del módulo de seguridad (gestor de contraseñas).
 *
 * <p>
 * Implementa los adaptadores que conectan el sistema con tecnologías externas
 * (Spring MVC, Spring Data JPA, MariaDB). Contiene el controlador REST, las
 * entidades JPA, los repositorios Spring Data, los mapeadores y la
 * implementación concreta de los puertos de repositorio definidos en el
 * dominio.
 *
 * <h2>Subpaquetes</h2>
 * <ul>
 * <li>{@code controller} — Endpoints REST para la gestión de contraseñas.</li>
 * <li>{@code persistence} — Entidades JPA, repositorios Spring Data y
 * mapeadores.</li>
 * <li>{@code repository} — Adaptador concreto del puerto
 * {@code PasswordRepository}.</li>
 * </ul>
 *
 * @author Juan Sebastian Rios, Daniel Salazar, Jeronimo Ospina Zapata
 * @since 0.0.1
 * @see org.adultofuncional.main.security.domain
 * @see org.adultofuncional.main.security.application
 */
package org.adultofuncional.main.security.infrastructure;
