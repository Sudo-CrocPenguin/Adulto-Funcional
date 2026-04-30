package org.adultofuncional.main.account.application.usecase;

import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.account.application.dto.AccountResponse;
import org.adultofuncional.main.account.application.dto.UpdateAccountRequest;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
import org.adultofuncional.main.shared.exception.BusinessException;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateAccountUseCase {

    private final AccountRepository accountRepository;

    @Transactional
    public AccountResponse execute(UUID accountId, UpdateAccountRequest request) {
        // 1. Buscar la cuenta existente
        AccountEntity entity = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Cuenta no encontrada con id: " + accountId));

        // 2. Si el email ha cambiado, verificar que no esté ya usado por otra cuenta
        if (!entity.getAccount_email().equals(request.getEmail())) {
            boolean emailExists = accountRepository.existsByEmail(request.getEmail());
            if (emailExists) {
                throw new BusinessException("El email " + request.getEmail() + " ya está registrado por otra cuenta");
            }
            entity.setAccount_email(request.getEmail());
        }

        // 3. Actualizar solo los campos permitidos
        entity.setAccount_names(request.getNames());
        entity.setAccount_lastnames(request.getLastnames());
        entity.setAccount_phone(request.getPhone());

        // 4. Guardar cambios (el repositorio debe tener un método save)
        AccountEntity updatedEntity = accountRepository.save(entity);

        // 5. Devolver el DTO de respuesta
        return mapToResponse(updatedEntity);
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