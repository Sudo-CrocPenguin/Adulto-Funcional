package org.adultofuncional.main.security.infrastructure.controller;

import java.util.List;
import java.util.UUID;

import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.security.application.dto.PasswordRequest;
import org.adultofuncional.main.security.application.dto.PasswordResponse;
import org.adultofuncional.main.security.application.dto.PasswordUpdateRequest;
import org.adultofuncional.main.security.application.usecase.CreatePasswordUseCase;
import org.adultofuncional.main.security.application.usecase.DeletePasswordUseCase;
import org.adultofuncional.main.security.application.usecase.GetPasswordUseCase;
import org.adultofuncional.main.security.application.usecase.ListPasswordsUseCase;
import org.adultofuncional.main.security.application.usecase.UpdatePasswordUseCase;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.adultofuncional.main.shared.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/security/passwords")
@RequiredArgsConstructor
public class SecurityController {

    private final CreatePasswordUseCase createPasswordUseCase;
    private final ListPasswordsUseCase listPasswordsUseCase;
    private final GetPasswordUseCase getPasswordUseCase;
    private final UpdatePasswordUseCase updatePasswordUseCase;
    private final DeletePasswordUseCase deletePasswordUseCase;

    private final AccountRepository accountRepository;

    private UUID resolveAccountId(String email) {
        return accountRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Cuenta no encontrada para el email: " + email))
                .getId();
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PasswordResponse>> createPassword(
            @Validated @RequestBody PasswordRequest request,
            @AuthenticationPrincipal String loggedEmail) {

        UUID accountId = resolveAccountId(loggedEmail);
        PasswordResponse response = createPasswordUseCase.execute(accountId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>(HttpStatus.CREATED.value(), "Contraseña guardada exitosamente", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PasswordResponse>>> listPasswords(
            @AuthenticationPrincipal String loggedEmail) {

        UUID accountId = resolveAccountId(loggedEmail);
        List<PasswordResponse> response = listPasswordsUseCase.execute(accountId);

        return ResponseEntity.ok(
                new ApiResponse<>(HttpStatus.OK.value(), "Contraseñas listadas exitosamente", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PasswordResponse>> getPassword(
            @PathVariable UUID id,
            @AuthenticationPrincipal String loggedEmail) {

        UUID accountId = resolveAccountId(loggedEmail);
        PasswordResponse response = getPasswordUseCase.execute(accountId, id);

        return ResponseEntity.ok(
                new ApiResponse<>(HttpStatus.OK.value(), "Contraseña obtenida exitosamente", response));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<PasswordResponse>> updatePassword(
            @PathVariable UUID id,
            @Validated @RequestBody PasswordUpdateRequest request,
            @AuthenticationPrincipal String loggedEmail) {

        UUID accountId = resolveAccountId(loggedEmail);
        PasswordResponse response = updatePasswordUseCase.execute(accountId, id, request);

        return ResponseEntity.ok(
                new ApiResponse<>(HttpStatus.OK.value(), "Contraseña actualizada exitosamente", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePassword(
            @PathVariable UUID id,
            @AuthenticationPrincipal String loggedEmail) {

        UUID accountId = resolveAccountId(loggedEmail);
        deletePasswordUseCase.execute(accountId, id);

        return ResponseEntity.ok(
                new ApiResponse<>(HttpStatus.OK.value(), "Contraseña eliminada exitosamente", null));
    }
}