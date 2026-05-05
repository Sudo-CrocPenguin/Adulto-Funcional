/**
 * Paquete raíz de la aplicación Adulto Funcional Server.
 *
 * <p>
 * Backend construido con <strong>Spring Boot 3.5.13</strong> y <strong>Java
 * 21</strong>
 * que implementa una Arquitectura Limpia (Clean Architecture) para la gestión
 * financiera personal, agenda de eventos y almacenamiento seguro de
 * contraseñas.
 *
 * <h3>Características principales</h3>
 * <ul>
 * <li>Gestión financiera: movimientos (ingresos/egresos), gastos fijos y
 * categorías.</li>
 * <li>Agenda personal: eventos con prioridad, recurrencia y recordatorios.</li>
 * <li>Gestor de contraseñas: cifrado AES‑256 protegido por Master Key.</li>
 * <li>Autenticación stateless con JWT y cookies HttpOnly; Argon2 para
 * hashes.</li>
 * <li>Protección anti‑XSS en todas las entradas de texto mediante
 * {@code @NoHtml}.</li>
 * <li>Respuestas API uniformes con el formato estándar
 * {@link org.adultofuncional.main.shared.response.ApiResponse}.</li>
 * <li>Validación de ownership centralizada y reutilizable.</li>
 * <li>Identificadores UUID v7 generados en la aplicación.</li>
 * <li>Migraciones de base de datos gestionadas con Flyway.</li>
 * </ul>
 *
 * <h3>Tecnologías</h3>
 * <ul>
 * <li>Spring Boot 3.5.13, Spring Data JPA, Spring Security</li>
 * <li>MariaDB 11.8</li>
 * <li>Flyway para migraciones</li>
 * <li>JWT (JJWT) para autenticación</li>
 * <li>Argon2 (Spring Security) para hash de contraseñas</li>
 * <li>Jsoup para validación anti‑HTML</li>
 * <li>Lombok para reducción de boilerplate</li>
 * <li>Spring Boot Actuator para health checks</li>
 * <li>Testcontainers para pruebas de integración</li>
 * </ul>
 *
 * <h3>Base de datos</h3>
 * <p>
 * Esquema principal: {@code accounts}, {@code categories}, {@code movements},
 * {@code fixed_expenses}, {@code events}, {@code passwords}. Todas las tablas
 * utilizan UUID v7 como clave primaria y mantienen integridad referencial con
 * eliminación en cascada controlada por JPA.
 *
 * <h3>Organización de paquetes</h3>
 * <ul>
 * <li>{@code account} — Módulo de cuentas de usuario (Clean Architecture
 * completa).</li>
 * <li>{@code auth} — Autenticación y registro con JWT.</li>
 * <li>{@code config} — Configuraciones de Spring (beans, seguridad, CORS).</li>
 * <li>{@code finances} — Módulo financiero (en desarrollo).</li>
 * <li>{@code agenda} — Módulo de agenda (en desarrollo).</li>
 * <li>{@code security} — Gestor de contraseñas con Master Key (en
 * desarrollo).</li>
 * <li>{@code shared} — Componentes transversales: excepciones,
 * {@code ApiResponse}, validación de ownership y anti‑XSS.</li>
 * </ul>
 *
 * <h3>Seguridad</h3>
 * <ul>
 * <li>JWT siempre en cookie HttpOnly. Clientes nativos reciben además el token
 * en el body.</li>
 * <li>Headers OWASP: CSP, HSTS, X-Frame-Options, X-Content-Type-Options,
 * X-XSS-Protection.</li>
 * <li>Validación de entrada con {@code @NoHtml} (Jsoup) para prevenir Stored
 * XSS.</li>
 * <li>No se distingue entre email inexistente y contraseña incorrecta (anti
 * user‑enumeration).</li>
 * </ul>
 *
 * @author Jeronimo Ospina Zapata, Lydis Ester Jaraba, Juan Sebastian Rios,
 *         Miguel Angel Blandon Montes, Daniel Salazar
 * @since 0.0.1
 */
package org.adultofuncional.main;
