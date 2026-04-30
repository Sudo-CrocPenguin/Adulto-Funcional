package org.adultofuncional.main.account.application.usecase;

import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.account.application.dto.AccountResponse;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetAccountUseCase {

    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public AccountResponse execute(UUID accountId) {
        AccountEntity entity = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Cuenta no encontrada con id: " + accountId));

        return mapToResponse(entity);
    }

    private AccountResponse mapToResponse(AccountEntity entity) {
        return AccountResponse.builder()
                .id(entity.getAccount_id())
                .names(entity.getAccount_names())
                .lastnames(entity.getAccount_lastnames())
                .email(entity.getAccount_email())
                .phone(entity.getAccount_phone())
                .createdAt(entity.getAccount_created_at())
                .build();
    }
}