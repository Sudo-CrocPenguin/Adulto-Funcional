package org.adultofuncional.main.security.infrastructure.persistence.mapper;

import java.util.UUID;

import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
import org.adultofuncional.main.security.domain.model.Password;
import org.adultofuncional.main.security.infrastructure.persistence.entity.PasswordEntity;
import org.springframework.stereotype.Component;

/**
 * Componente que convierte entre las distintas representaciones de una credencial.
 *
 * <p>Traduce entre:
 * <ul>
 *   <li>{@link PasswordEntity} (JPA) → {@link Password} (dominio) mediante {@link #toDomain}</li>
 *   <li>{@link Password} (dominio) → {@link PasswordEntity} (JPA) mediante {@link #toEntity}</li>
 * </ul>
 *
 * <p>Los campos criptográficos ({@code salt}, {@code iv}, {@code ciphertext}) se mapean
 * directamente sin transformación, ya que la encriptación y desencriptación es
 * responsabilidad exclusiva de los casos de uso a través del servicio AES-256.
 *
 * @author Lidys Jaraba
 * @since 0.0.1
 * @see Password
 * @see PasswordEntity
 */

@Component
public class PasswordMapper {

    /**
     * Convierte una {@link PasswordEntity} al modelo de dominio {@link Password}.
     *
     * <p>Usa el método de fábrica {@link Password#reconstitute} para respetar
     * el constructor privado del modelo de dominio.
     *
     * <p>Si la relación {@code account} es {@code null}, el {@code accountId}
     * se establece como {@code null} en el dominio.
     *
     * @param entity entidad JPA recuperada del repositorio; si es {@code null} retorna {@code null}
     * @return modelo de dominio reconstituido, o {@code null} si la entidad es {@code null}
     */

    public Password toDomain(PasswordEntity entity) {
        if (entity == null) return null;

        UUID accountId = entity.getAccount() != null
                ? entity.getAccount().getAccountId()
                : null;

        return Password.reconstitute(
                entity.getPasswordId(),
                entity.getPasswordApplicationName(),
                entity.getPasswordSalt(),
                entity.getPasswordIv(),
                entity.getPasswordCiphertext(),
                entity.getPasswordLastChangeDate(),
                accountId
        );
    }

    /**
     * Convierte el modelo de dominio {@link Password} a una {@link PasswordEntity} lista para persistir.
     *
     * <p>El {@code accountId} se obtiene directamente de {@link Password#getAccountId()}.
     * Se construye una referencia JPA con solo el ID para {@code account}, suficiente
     * para que Hibernate resuelva la FK al persistir.
     *
     * @param password modelo de dominio a convertir; si es {@code null} retorna {@code null}
     * @return entidad JPA lista para persistir, o {@code null} si el modelo es {@code null}
     */
    
    public PasswordEntity toEntity(Password password) {
        if (password == null) return null;

        PasswordEntity entity = new PasswordEntity();
        entity.setPasswordId(password.getId());
        entity.setPasswordApplicationName(password.getApplicationName());
        entity.setPasswordSalt(password.getSalt());
        entity.setPasswordIv(password.getIv());
        entity.setPasswordCiphertext(password.getCiphertext());
        entity.setPasswordLastChangeDate(password.getLastChangeDate());

        AccountEntity accountRef = new AccountEntity();
        accountRef.setAccountId(password.getAccountId());
        entity.setAccount(accountRef);

        return entity;
    }
}