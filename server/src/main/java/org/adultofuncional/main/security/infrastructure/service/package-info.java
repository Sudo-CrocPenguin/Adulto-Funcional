/**
 * Implementaciones concretas de los puertos de servicio del dominio de
 * seguridad.
 *
 * <p>
 * Contiene las clases que implementan las interfaces definidas en
 * {@code org.adultofuncional.main.security.domain.service}. Estas
 * implementaciones usan bibliotecas de infraestructura (JCA, mapas
 * concurrentes) y son inyectadas por Spring en los casos de uso.
 *
 * <h2>Servicios implementados</h2>
 * <ul>
 * <li>{@link org.adultofuncional.main.security.infrastructure.service.AesEncryptionService}
 * —
 * Cifrado AES‑256‑GCM con derivación PBKDF2.</li>
 * <li>{@link org.adultofuncional.main.security.infrastructure.service.InMemoryMasterKeyService}
 * —
 * Gestión en memoria de la Master Key por cuenta.</li>
 * </ul>
 *
 * @author Equipo de desarrollo Adulto Funcional
 * @since 0.0.1
 * @see org.adultofuncional.main.security.domain.service
 */
package org.adultofuncional.main.security.infrastructure.service;
