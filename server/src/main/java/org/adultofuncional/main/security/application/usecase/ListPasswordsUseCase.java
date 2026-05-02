package org.adultofuncional.main.security.application.usecase;

import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
import org.adultofuncional.main.security.application.dto.PasswordResponse;
import org.adultofuncional.main.security.domain.repository.PasswordRepository;
import org.adultofuncional.main.security.infrastructure.persistence.entity.PasswordEntity;
import org.adultofuncional.main.shared.exception.ForbiddenException;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ListPasswordsUseCase {

    private final PasswordRepository passwordRepository;
    private final AccountRepository accountRepository;

    // TODO: Inyectar servicio de encriptación (AesEncryptionService) cuando esté disponible
    // TODO: Inyectar servicio de sesión para verificar Master Key (MasterKeyVerificationService)

    @Transactional(readOnly = true)
    public List<PasswordResponse> execute(UUID accountId) {
        // 1. Verificar que la cuenta existe
        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Cuenta no encontrada con id: " + accountId));

        // TODO: Implementar verificación de Master Key en sesión

        List<PasswordEntity> passwords = passwordRepository.findAllByAccountId(accountId);

        // TODO: Implementar desencriptación AES-256 usando la Master Key del usuario


        return passwords.stream()
                .map(entity -> mapToResponse(entity, "DECRYPTED_" + entity.getPasswordApplication()))
                .collect(Collectors.toList());
    }

    private PasswordResponse mapToResponse(PasswordEntity entity, String decryptedPassword) {
        return PasswordResponse.builder()
                .id(entity.getPasswordId())
                .applicationName(entity.getPasswordApplicationName())
                .password(decryptedPassword)
                .lastChangeDate(entity.getPasswordLastChangeDate())
                .build();
    }
}