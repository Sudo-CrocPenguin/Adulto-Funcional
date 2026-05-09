package org.adultofuncional.main.security.application.usecase;

import java.time.LocalDate;
import java.util.UUID;

import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.security.application.dto.PasswordRequest;
import org.adultofuncional.main.security.application.dto.PasswordResponse;
import org.adultofuncional.main.security.domain.model.Password;
import org.adultofuncional.main.security.domain.repository.PasswordRepository;
import org.adultofuncional.main.shared.exception.BusinessException;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * Caso de uso: Guardar una nueva contraseña en el gestor.
 *
 * <p><strong>¿Qué es?</strong><br>
 * Servicio que encapsula la lógica de negocio para almacenar una nueva credencial.
 *
 * <p><strong>¿Para qué sirve?</strong><br>
 * Verifica la existencia de la cuenta, la unicidad del nombre de la aplicación,
 * encripta la contraseña y la persiste.
 *
 * <p><strong>¿Cómo funciona?</strong><br>
 * <ol>
 *   <li>Valida que la cuenta exista.</li>
 *   <li>Comprueba que no exista otra entrada con el mismo nombre de aplicación.</li>
 *   <li>Encripta la contraseña (actualmente placeholder).</li>
 *   <li>Crea el modelo de dominio y lo guarda.</li>
 * </ol>
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Service
@RequiredArgsConstructor
public class CreatePasswordUseCase {

    private final PasswordRepository passwordRepository;
    private final AccountRepository accountRepository;

    // TODO: Inyectar servicio de encriptación AES-256
    // TODO: Inyectar servicio de verificación de Master Key

    @Transactional
    public PasswordResponse execute(UUID accountId, PasswordRequest request) {
        // 1. Verificar cuenta
        accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Cuenta no encontrada con id: " + accountId));

        // 2. Verificar Master Key (placeholder)
        // TODO: if (!masterKeyService.isVerified(accountId)) throw new ForbiddenException(...);

        // 3. Verificar unicidad del nombre de aplicación
        if (passwordRepository.existsByAccountIdAndApplicationName(accountId, request.getApplicationName())) {
            throw new BusinessException("Ya existe una contraseña para la aplicación: " + request.getApplicationName());
        }

        // 4. Encriptar contraseña (placeholder hasta implementar AES-256)
        // TODO: usar encryptionService para generar salt, iv y ciphertext reales
        String salt = "SALT_" + request.getApplicationName();
        byte[] iv = new byte[16];
        byte[] ciphertext = request.getPassword().getBytes();

        // 5. Fecha de último cambio
        LocalDate lastChangeDate = request.getLastChangeDate() != null
                ? request.getLastChangeDate()
                : LocalDate.now();

        // 6. Crear modelo de dominio
        Password password = Password.create(
                request.getApplicationName(),
                salt,
                iv,
                ciphertext,
                lastChangeDate,
                accountId
        );

        Password saved = passwordRepository.save(password);

        // 7. Retornar DTO
        return PasswordResponse.builder()
                .id(saved.getId())
                .applicationName(saved.getApplicationName())
                .lastChangeDate(saved.getLastChangeDate())
                .build();
    }
}