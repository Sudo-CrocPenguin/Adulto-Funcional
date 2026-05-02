package org.adultofuncional.main.security.application.usecase;

import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
import org.adultofuncional.main.security.application.dto.PasswordRequest;
import org.adultofuncional.main.security.application.dto.PasswordResponse;
import org.adultofuncional.main.security.domain.repository.PasswordRepository;
import org.adultofuncional.main.security.infrastructure.persistence.entity.PasswordEntity;
import org.adultofuncional.main.shared.exception.BusinessException;
import org.adultofuncional.main.shared.exception.ForbiddenException;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SavePasswordUseCase {

    private final PasswordRepository passwordRepository;
    private final AccountRepository accountRepository;

    // TODO: Inyectar servicio de encriptación (AesEncryptionService) cuando esté disponible
    // TODO: Inyectar servicio de sesión para verificar Master Key (MasterKeyVerificationService)

    @Transactional
    public PasswordResponse execute(UUID accountId, PasswordRequest request) {
        // 1. Verificar que la cuenta existe
        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Cuenta no encontrada con id: " + accountId));

        // TODO: Implementar verificación de Master Key en sesión

        boolean applicationExists = passwordRepository.existsByAccountIdAndApplicationName(
                accountId, request.getApplicationName()
        );
        if (applicationExists) {
            throw new BusinessException(
                    "Ya existe una contraseña guardada para la aplicación: " + request.getApplicationName()
            );
        }

        // TODO: Implementar encriptación AES-256

        String encryptedPassword = "ENCRYPTED_" + request.getPassword(); // Placeholder

        PasswordEntity entity = new PasswordEntity();
        entity.setAccount(account);
        entity.setPasswordApplicationName(request.getApplicationName());
        entity.setPasswordApplication(encryptedPassword);

        LocalDate lastChangeDate = request.getLastChangeDate() != null
                ? request.getLastChangeDate()
                : LocalDate.now();
        entity.setPasswordLastChangeDate(lastChangeDate);

        PasswordEntity savedEntity = passwordRepository.save(entity);

        return mapToResponse(savedEntity, request.getPassword());
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