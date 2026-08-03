/**
 * Paquete raíz de Adulto Funcional Server.
 *
 * <p>Monolito modular Java 21/Spring Boot 3.5 para cuentas, autenticación,
 * finanzas, agenda y bóveda. Los módulos se organizan en dominio, aplicación e
 * infraestructura; {@code shared} concentra contratos transversales y
 * {@code config} integra el framework.</p>
 *
 * <h2>Propiedades principales</h2>
 * <ul>
 * <li>Sesiones persistidas con JWT de corta duración, refresh rotativo,
 * revocación y CSRF para cookies.</li>
 * <li>Ownership por cuenta en consultas y eliminaciones.</li>
 * <li>MariaDB versionada por Flyway y estado de seguridad efímero en Redis.</li>
 * <li>Paginación SQL, UUID v7, timestamps UTC y control optimista.</li>
 * <li>Bóveda AES-256-GCM protegida por Master Key aislada por sesión.</li>
 * <li>Errores uniformes y trazabilidad mediante {@code X-Trace-Id}.</li>
 * </ul>
 *
 * @author Equipo de desarrollo Adulto Funcional
 * @since 0.0.1
 */
package org.adultofuncional.main;
