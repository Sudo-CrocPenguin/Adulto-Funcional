package org.adultofuncional.main.account.infrastructure.controller;

//por ahora en comentarios hasta que esten disponibles
//import org.adultofuncional.main.account.application.usecase.GetAccountUseCase;
// import org.adultofuncional.main.account.application.usecase.UpdateAccountUseCase;
//import org.adultofuncional.main.account.application.dto.AccountResponse;
//import org.adultofuncional.main.account.application.dto.UpdateAccountRequest;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST que expone los endpoints del módulo de cuentas.
 *
 * <p>
 * Base path: {@code /api/account}. Las respuestas nunca incluyen
 * {@code account_password} ni {@code account_master_key}.
 *
 * <p>
 * Endpoints:
 * <ul>
 * <li>{@code GET /api/account/{id}} — obtener datos de una cuenta</li>
 * <li>{@code PUT /api/account/{id}} — actualizar datos de una cuenta</li>
 * <li>{@code DELETE /api/account/{id}} — eliminar una cuenta</li>
 * </ul>
 *
 * @author Lydis Jaraba
 * @since 0.0.1
 */
@RestController
@RequestMapping("/api/account")
public class AccountController {

  /*
   * por ahora en comentarios hasta que los use cases esten disponibles
   * 
   * private final GetAccountUseCase getAccountUseCase;
   * private final UpdateAccountUseCase updateAccountUseCase;
   * 
   * public AccountController(GetAccountUseCase getAccountUseCase,
   * UpdateAccountUseCase updateAccountUseCase) {
   * this.getAccountUseCase = getAccountUseCase;
   * this.updateAccountUseCase = updateAccountUseCase;
   * 
   * }
   * 
   */

  /**
   * Obtiene los datos de una cuenta por su ID.
   *
   * @param id UUID de la cuenta a consultar
   * @return datos de la cuenta con status 200, o null como placeholder temporal
   */
  // TODO: conectar con GetAccountUseCase cuando esté disponible
  @GetMapping("/{id}")
  public ResponseEntity<Object> getAccount(@PathVariable UUID id) {
    /*
     * REEMPLAZAR cuando GetAccountUseCase este disponible
     * 
     * AccountResponse response = getAccountUseCase.execute(id);
     * return ResponseEntity.ok(response);
     * 
     */

    return ResponseEntity.ok(null); // cuando funcione las lineas de arriba se elimina esta
  }

  /**
   * Actualiza los datos de una cuenta.
   *
   * @param id      UUID de la cuenta a actualizar
   * @param request nuevos datos de la cuenta
   * @return datos actualizados con status 200, o null como placeholder temporal
   */
  // TODO: conectar con UpdateAccountUseCase cuando esté disponible
  @PutMapping("/{id}")
  public ResponseEntity<Object> updateAccount(@PathVariable UUID id, @RequestBody Object request) {

    /*
     * AccountResponse response = updateAccountUseCase.execute(id, request);
     * return ResponseEntity.ok(response);
     * 
     */

    return ResponseEntity.ok(null);
  }

  /**
   * Elimina una cuenta por su ID. Al eliminarla se eliminan en cascada
   * todos los datos asociados (movimientos, eventos, gastos fijos y contraseñas).
   *
   * @param id UUID de la cuenta a eliminar
   * @return respuesta vacía con status 204
   */
  // TODO: conectar con DeleteAccountUseCase cuando esté disponible
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteAccount(@PathVariable UUID id) {
    // agregar DeleteAccountUseCase cuando este disponible:
    // deleteAccountUseCase.execute(id);

    return ResponseEntity.noContent().build();
  }

}
