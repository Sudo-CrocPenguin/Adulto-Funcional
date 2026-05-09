package org.adultofuncional.main.security.application.usecase;

import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.security.application.dto.PasswordResponse;
import org.adultofuncional.main.security.domain.repository.PasswordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Caso de uso: Listar todas las contraseñas de una cuenta.
 *
 * <p><strong>¿Qué es?</strong><br>
 * Servicio que recupera todas las entradas del gestor de contraseñas para un usuario.
 *
 * <p><strong>¿Para qué sirve?</strong><br>
 * Devuelve una lista de DTOs con los datos (contraseñas desencriptadas).
 *
 * <p><strong>¿Cómo funciona?</strong><br>
 * Usa el repositorio para obtener todas las entradas de la cuenta,
 * luego desencripta cada una y las mapea a DTOs.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Service
@RequiredArgsConstructor
public class ListPasswordsUseCase {

    private final PasswordRepository passwordRepository;
    // TODO: Inyectar servicio de desencriptación

    @Transactional(readOnly = true)
    public List<PasswordResponse> execute(UUID accountId) {
        var passwords = passwordRepository.findAllByAccountId(accountId);

        return passwords.stream()
                .map(p -> {
                    // TODO: String decrypted = decryptionService.decrypt(p.getEncryptedPassword(), masterKey);
                    String decrypted = "DECRYPTED_" + p.getEncryptedPassword();
                    return PasswordResponse.builder()
                            .id(p.getId())
                            .applicationName(p.getApplicationName())
                            .password(decrypted)
                            .category(p.getCategory())
                            .lastChangeDate(p.getLastChangeDate())
                            .build();
                })
                .collect(Collectors.toList());
    }
}
