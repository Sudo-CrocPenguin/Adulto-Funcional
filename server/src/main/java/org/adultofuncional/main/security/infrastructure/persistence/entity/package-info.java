/**
 * Entidades JPA del módulo de seguridad (gestor de contraseñas).
 *
 * <p>
 * Contiene la entidad
 * {@link org.adultofuncional.main.security.infrastructure.persistence.entity.PasswordEntity},
 * que mapea la tabla {@code passwords} de MariaDB. Almacena credenciales de
 * servicios externos cifradas con AES-256, utilizando un salt único, un vector
 * de inicialización (IV) de 16 bytes y el texto cifrado que puede incluir el
 * tag de autenticación (AES-GCM). La entidad se relaciona obligatoriamente con
 * una cuenta propietaria a través de {@code passwords_fk_account_id}.
 *
 * <p>
 * La entidad JPA se usa exclusivamente dentro de la capa de infraestructura,
 * nunca se expone a las capas de aplicación o dominio. La conversión con el
 * modelo de dominio
 * {@link org.adultofuncional.main.security.domain.model.Password}
 * se realiza mediante el
 * {@link org.adultofuncional.main.security.infrastructure.persistence.mapper.PasswordMapper}.
 *
 * <h2>Entidad incluida</h2>
 * <ul>
 * <li>{@link org.adultofuncional.main.security.infrastructure.persistence.entity.PasswordEntity}
 * —
 * Credenciales de aplicaciones externas con cifrado AES-256.</li>
 * </ul>
 *
 * <h2>Características de seguridad</h2>
 * <ul>
 * <li><strong>Salt único:</strong> cada registro tiene un salt aleatorio
 * (almacenado en Base64) para derivar una clave AES independiente incluso si
 * dos usuarios usan la misma Master Key.</li>
 * <li><strong>IV aleatorio:</strong> el vector de inicialización es diferente
 * en cada cifrado, evitando patrones reconocibles.</li>
 * <li><strong>Ciphertext + tag:</strong> el texto cifrado incluye el tag de
 * autenticación cuando se usa AES-GCM, protegiendo la integridad.</li>
 * <li><strong>Relación obligatoria:</strong> la clave foránea no permite
 * registros huérfanos.</li>
 * </ul>
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 * @see org.adultofuncional.main.security.infrastructure.persistence.entity.PasswordEntity
 * @see org.adultofuncional.main.security.infrastructure.persistence.mapper.PasswordMapper
 * @see org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity
 */
package org.adultofuncional.main.security.infrastructure.persistence.entity;
