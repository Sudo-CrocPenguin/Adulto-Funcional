/**
 * Paquete raíz de la aplicación Adulto Funcional Server.
 *
 * <p>
 * Backend construido con Spring Boot 3.5.13 y Java 21 que implementa
 * una arquitectura limpia (Clean Architecture) para la gestión financiera
 * personal, agenda y almacenamiento seguro de contraseñas.
 * </p>
 *
 * <p>
 * Tecnologías principales:
 * <ul>
 * <li>Spring Boot 3.5.13, Spring Data JPA, Spring Security</li>
 * <li>MariaDB 10.8 con Flyway para migraciones</li>
 * <li>Autenticación JWT + Argon2 para contraseñas</li>
 * <li>UUID v7 (ordenable temporalmente) para identificadores</li>
 * <li>Lombok para reducción de código repetitivo</li>
 * <li>Spring Boot Actuator para health checks</li>
 * </ul>
 * </p>
 *
 * <p>
 * Base de datos: MariaDB con las siguientes entidades principales:
 * {@code accounts}, {@code categories}, {@code movements},
 * {@code fixed_expenses}, {@code events}, {@code passwords}.
 * </p>
 *
 * <p>
 * Organización de paquetes:
 * <ul>
 * <li>{@code account} — Gestión de cuentas de usuario</li>
 * <li>{@code auth} — Autenticación y registro (JWT)</li>
 * <li>{@code config} — Configuraciones de Spring
 * (beans, jackson, security)</li>
 * <li>{@code finances} — Módulo financiero (movimientos, gastos fijos,
 * categorías)</li>
 * <li>{@code agenda} — Gestión de eventos y recordatorios</li>
 * <li>{@code security} — Gestor de contraseñas con Master Key</li>
 * <li>{@code shared} — Componentes transversales (excepciones, respuestas
 * API)</li>
 * </ul>
 * </p>
 *
 * @author Jeronimo Ospina Zapata, Lydis Ester Jaraba, Juan Sebastian Rios,
 *         Miguel Angel Blandon Montes
 * @since 0.0.1
 */
package org.adultofuncional.main;
