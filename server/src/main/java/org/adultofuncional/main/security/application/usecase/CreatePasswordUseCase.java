package org.adultofuncional.main.security.application.usecase;

import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.security.application.dto.PasswordRequest;
import org.adultofuncional.main.security.application.dto.PasswordResponse;
import org.adultofuncional.main.security.domain.model.Password;
import org.adultofuncional.main.security.domain.repository.PasswordRepository;
import org.adultofuncional.main.shared.exception.BusinessException;
import org.adultofuncional.main.shared.exception.ForbiddenException;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Caso de uso: Guardar una nueva contraseña en el gestor.
 *
 * <p><strong>¿Qué es?</strong><br>
 * Servicio que encapsula la lógica de negocio para almacenar una nueva credencial.
 *
 * <p><strong>¿Para qué sirve?</strong><br>
 * Verifica la existencia de la cuenta, la verificación de la Master Key,
 * la unicidad del nombre de la aplicación, encripta la contraseña y la persiste.
 *
 * <p><strong>¿Cómo funciona?</strong><br>
 * <ol>
 *   <li>Valida que la cuenta exista y que la Master Key haya sido verificada.</li>
 *   <li>Comprueba que no exista otra entrada con el mismo nombre de aplicación.</li>
 *   <li>Encripta la contraseña (actualmente placeholder).</li>
 *   <li>Crea la entidad de dominio y la guarda.</li>
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
    // TODO: Inyectar servicio de verificación de Master Key (sesión)

    @Transactional
    public PasswordResponse execute(UUID accountId, PasswordRequest request) {
        // 1. Verificar cuenta
        accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Cuenta no encontrada con id: " + accountId));

        // 2. Verificar Master Key (placeholder)
        // TODO: if(!masterKeyService.isVerified(accountId)) throw new ForbiddenException(...);

        // 3. Verificar unicidad del nombre de aplicación
        if (passwordRepository.existsByAccountIdAndApplicationName(accountId, request.getApplicationName())) {
            throw new BusinessException("Ya existe una contraseña para la aplicación: " + request.getApplicationName());
        }

        // 4. Encriptar contraseña (placeholder)
        // TODO: String encrypted = encryptionService.encrypt(request.getPassword(), masterKey);
        String encrypted = "ENCRYPTED_" + request.getPassword();

        // 5. Crear modelo de dominio
        LocalDate lastChange = request.getLastChangeDate() != null ? request.getLastChangeDate() : LocalDate.now();
        Password password = Password.create(
                accountId,
                request.getApplicationName(),
                encrypted,
                request.getCategory(),
                lastChange
        );

        Password saved = passwordRepository.save(password);

        // 6. Retornar DTO (con contraseña desencriptada solo para confirmación)
        return PasswordResponse.builder()
                .id(saved.getId())
                .applicationName(saved.getApplicationName())
                .password(request.getPassword())
                .category(saved.getCategory())
                .lastChangeDate(saved.getLastChangeDate())
                .build();
    }
}
