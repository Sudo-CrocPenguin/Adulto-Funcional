package org.adultofuncional.main.security.infrastructure.controller;

import org.adultofuncional.main.config.security.AuthenticatedAccount;
import org.adultofuncional.main.security.application.dto.ChangeMasterKeyRequest;
import org.adultofuncional.main.security.application.dto.ConfigureMasterKeyRequest;
import org.adultofuncional.main.security.application.dto.MasterKeyStatusResponse;
import org.adultofuncional.main.security.application.dto.VerifyMasterKeyRequest;
import org.adultofuncional.main.security.application.usecase.ChangeMasterKeyUseCase;
import org.adultofuncional.main.security.application.usecase.CloseMasterKeySessionUseCase;
import org.adultofuncional.main.security.application.usecase.ConfigureMasterKeyUseCase;
import org.adultofuncional.main.security.application.usecase.GetMasterKeyStatusUseCase;
import org.adultofuncional.main.security.application.usecase.VerifyMasterKeyUseCase;
import org.adultofuncional.main.shared.ratelimit.RateLimitGuard;
import org.adultofuncional.main.shared.ratelimit.RateLimitPolicy;
import org.adultofuncional.main.shared.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Contrato canónico para configurar, verificar, rotar y cerrar la Master Key. */
@RestController
@RequestMapping("/api/security/master-key")
@RequiredArgsConstructor
public class MasterKeyController {

  private final GetMasterKeyStatusUseCase statusUseCase;
  private final ConfigureMasterKeyUseCase configureUseCase;
  private final VerifyMasterKeyUseCase verifyUseCase;
  private final ChangeMasterKeyUseCase changeUseCase;
  private final CloseMasterKeySessionUseCase closeSessionUseCase;
  private final RateLimitGuard rateLimitGuard;

  @GetMapping("/status")
  public ResponseEntity<ApiResponse<MasterKeyStatusResponse>> status(
      @AuthenticationPrincipal AuthenticatedAccount account) {
    return ok("Estado de Master Key consultado", statusUseCase.execute(
        account.accountId(), account.sessionId()));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<MasterKeyStatusResponse>> configure(
      @Valid @RequestBody ConfigureMasterKeyRequest request,
      @AuthenticationPrincipal AuthenticatedAccount account) {
    consumeCrypto(account);
    MasterKeyStatusResponse status = configureUseCase.execute(account.accountId(), request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new ApiResponse<>(201, "Master Key configurada", status));
  }

  @PostMapping("/verify")
  public ResponseEntity<ApiResponse<MasterKeyStatusResponse>> verify(
      @Valid @RequestBody VerifyMasterKeyRequest request,
      @AuthenticationPrincipal AuthenticatedAccount account) {
    return ok("Master Key verificada", verifyUseCase.execute(
        account.accountId(), account.sessionId(), request));
  }

  @PatchMapping
  public ResponseEntity<ApiResponse<MasterKeyStatusResponse>> change(
      @Valid @RequestBody ChangeMasterKeyRequest request,
      @AuthenticationPrincipal AuthenticatedAccount account) {
    consumeCrypto(account);
    return ok("Master Key cambiada y bóveda recifrada", changeUseCase.execute(
        account.accountId(), request));
  }

  @DeleteMapping("/session")
  public ResponseEntity<ApiResponse<MasterKeyStatusResponse>> closeSession(
      @AuthenticationPrincipal AuthenticatedAccount account) {
    return ok("Gestor bloqueado en la sesión actual", closeSessionUseCase.execute(
        account.accountId(), account.sessionId()));
  }

  private void consumeCrypto(AuthenticatedAccount account) {
    rateLimitGuard.consume(
        RateLimitPolicy.VAULT_CRYPTO_SESSION,
        account.accountId() + ":" + account.sessionId());
  }

  private ResponseEntity<ApiResponse<MasterKeyStatusResponse>> ok(
      String message,
      MasterKeyStatusResponse data) {
    return ResponseEntity.ok(new ApiResponse<>(200, message, data));
  }
}
