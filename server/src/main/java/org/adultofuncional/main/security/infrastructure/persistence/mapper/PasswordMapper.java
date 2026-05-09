package org.adultofuncional.main.security.infrastructure.persistence.mapper;

import java.util.UUID;

import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
import org.adultofuncional.main.security.domain.model.Password;
import org.adultofuncional.main.security.infrastructure.persistence.entity.PasswordEntity;
import org.springframework.stereotype.Component;

@Component
public class PasswordMapper {

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