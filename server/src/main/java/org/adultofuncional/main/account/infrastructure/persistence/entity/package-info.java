/**
 * Entidades JPA del módulo de cuentas de usuario.
 *
 * <p>
 * Contiene la entidad
 * {@link org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity},
 * que mapea la tabla {@code accounts} de MariaDB. Es la entidad central del
 * sistema: el resto de módulos (finanzas, agenda, seguridad) referencian una
 * cuenta mediante su {@code account_id}.
 *
 * <p>
 * La entidad JPA se usa exclusivamente dentro de la capa de infraestructura,
 * nunca se expone a las capas de aplicación o dominio. La conversión entre
 * {@code AccountEntity} y el modelo de dominio
 * {@link org.adultofuncional.main.account.domain.model.Account} se realiza
 * mediante el
 * {@link org.adultofuncional.main.account.infrastructure.persistence.mapper.AccountMapper}.
 *
 * <h2>Entidad incluida</h2>
 * <ul>
 * <li>{@link org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity}
 * —
 * Cuenta de usuario con datos personales, hashes Argon2, versión optimista y
 * relaciones JPA con movimientos, gastos fijos, eventos y contraseñas.</li>
 * </ul>
 *
 * <h2>Características</h2>
 * <ul>
 * <li><strong>Fecha de creación automática:</strong> El campo
 * {@code account_created_at} procede del dominio; {@code @PrePersist} solo
 * completa entidades parciales antiguas.</li>
 * <li><strong>Concurrencia:</strong> {@code account_version} implementa
 * bloqueo optimista.</li>
 * <li><strong>Eliminación:</strong> el esquema aplica {@code ON DELETE
 * CASCADE}; las asociaciones JPA mantienen coherencia dentro del agregado.</li>
 * <li><strong>Seguridad:</strong> Los campos {@code account_password} y
 * {@code account_master_key} almacenan hashes Argon2, nunca texto plano.</li>
 * </ul>
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 * @see org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity
 * @see org.adultofuncional.main.account.infrastructure.persistence.mapper.AccountMapper
 */
package org.adultofuncional.main.account.infrastructure.persistence.entity;
