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
 * Cuenta de usuario con datos personales, hash de contraseña (Argon2), master
 * key opcional y relaciones {@code @OneToMany} con cascada hacia movimientos,
 * gastos fijos, eventos y contraseñas.</li>
 * </ul>
 *
 * <h2>Características</h2>
 * <ul>
 * <li><strong>Fecha de creación automática:</strong> El campo
 * {@code account_created_at} se establece mediante {@code @PrePersist} en el
 * momento del primer {@code INSERT}, no por la base de datos.</li>
 * <li><strong>Cascada:</strong> Las relaciones con las entidades dependientes
 * usan {@code CascadeType.ALL} y {@code orphanRemoval = true}, de modo que al
 * eliminar una cuenta se eliminan automáticamente todos sus datos
 * asociados.</li>
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
