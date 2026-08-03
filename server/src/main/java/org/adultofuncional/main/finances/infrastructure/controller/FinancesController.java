package org.adultofuncional.main.finances.infrastructure.controller;

import java.util.List;
import java.util.UUID;

import org.adultofuncional.main.config.security.AuthenticatedAccount;
import org.adultofuncional.main.finances.application.dto.category.CategoryFilterRequest;
import org.adultofuncional.main.finances.application.dto.category.CategoryResponse;
import org.adultofuncional.main.finances.application.dto.category.CreateCategoryRequest;
import org.adultofuncional.main.finances.application.dto.category.UpdateCategoryRequest;
import org.adultofuncional.main.finances.application.dto.fixedexpense.CreateFixedExpenseRequest;
import org.adultofuncional.main.finances.application.dto.fixedexpense.FixedExpenseFilterRequest;
import org.adultofuncional.main.finances.application.dto.fixedexpense.FixedExpenseResponse;
import org.adultofuncional.main.finances.application.dto.fixedexpense.UpdateFixedExpenseRequest;
import org.adultofuncional.main.finances.application.dto.movement.CreateMovementRequest;
import org.adultofuncional.main.finances.application.dto.movement.MovementFilterRequest;
import org.adultofuncional.main.finances.application.dto.movement.MovementResponse;
import org.adultofuncional.main.finances.application.dto.movement.UpdateMovementRequest;
import org.adultofuncional.main.finances.application.usecase.category.CreateCategoryUseCase;
import org.adultofuncional.main.finances.application.usecase.category.DeleteCategoryUseCase;
import org.adultofuncional.main.finances.application.usecase.category.GetCategoryUseCase;
import org.adultofuncional.main.finances.application.usecase.category.ListCategoriesUseCase;
import org.adultofuncional.main.finances.application.usecase.category.UpdateCategoryUseCase;
import org.adultofuncional.main.finances.application.usecase.fixedexpense.CreateFixedExpenseUseCase;
import org.adultofuncional.main.finances.application.usecase.fixedexpense.DeleteFixedExpenseUseCase;
import org.adultofuncional.main.finances.application.usecase.fixedexpense.GetFixedExpenseUseCase;
import org.adultofuncional.main.finances.application.usecase.fixedexpense.ListFixedExpensesUseCase;
import org.adultofuncional.main.finances.application.usecase.fixedexpense.UpdateFixedExpenseUseCase;
import org.adultofuncional.main.finances.application.usecase.movement.CreateMovementUseCase;
import org.adultofuncional.main.finances.application.usecase.movement.DeleteMovementUseCase;
import org.adultofuncional.main.finances.application.usecase.movement.GetMovementUseCase;
import org.adultofuncional.main.finances.application.usecase.movement.ListMovementsUseCase;
import org.adultofuncional.main.finances.application.usecase.movement.UpdateMovementUseCase;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.adultofuncional.main.shared.pagination.PageMetadata;
import org.adultofuncional.main.shared.pagination.PageResult;
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
import jakarta.validation.Valid;


/**
 * Controlador REST del módulo de finanzas personales.
 *
 * <p>Expone endpoints para gestionar movimientos, categorías y gastos fijos
 * bajo la ruta base {@code /api/finances}. Delega la lógica de negocio a los
 * casos de uso correspondientes y retorna respuestas envueltas en
 * {@link ApiResponse}.</p>
 *
   * <p>Los endpoints que operan sobre recursos de una cuenta toman el
   * {@code accountId} estable desde el claim {@code sub} del JWT mediante
   * {@link AuthenticatedAccount}. Las categorías son globales y
 * no requieren esta resolución.</p>
 *
 * @author Lidys Jaraba
 * @since 0.0.1
 */

@RestController
@RequestMapping("/api/finances")
@RequiredArgsConstructor
public class FinancesController {

     /**
     * Caso de uso para registrar un nuevo movimiento financiero.
     */
    private final CreateMovementUseCase createMovementUseCase;
    /**
     * Caso de uso para obtener un movimiento financiero por su identificador.
     */
    private final GetMovementUseCase getMovementUseCase;
    /**
     * Caso de uso para obtener un movimiento financiero por su identificador.
     */
    private final ListMovementsUseCase listMovementUseCase;
    /**
     * Caso de uso para actualizar parcialmente un movimiento financiero.
     */
    private final UpdateMovementUseCase updateMovementUseCase;
    /**
     * Caso de uso para eliminar un movimiento financiero.
     */
    private final DeleteMovementUseCase deleteMovementUseCase;

    /**
     * Caso de uso para crear una nueva categoría financiera.
     */
    private final CreateCategoryUseCase createCategoryUseCase;
    /**
     * Caso de uso para obtener una categoría financiera por su identificador.
     */
    private final GetCategoryUseCase getCategoryUseCase;
    /**
     * Caso de uso para listar las categorías financieras del sistema.
     */
    private final ListCategoriesUseCase listCategoriesUseCase;
    /**
     * Caso de uso para actualizar parcialmente una categoría financiera.
     */
    private final UpdateCategoryUseCase updateCategoryUseCase;
    /**
     * Caso de uso para eliminar una categoría financiera.
     */
    private final DeleteCategoryUseCase deleteCategoryUseCase;

    /**
     * Caso de uso para registrar un nuevo gasto fijo.
     */
    private final CreateFixedExpenseUseCase createFixedExpenseUseCase;
    /**
     * Caso de uso para obtener un gasto fijo por su identificador.
     */
    private final GetFixedExpenseUseCase getFixedExpenseUseCase;
    /**
     * Caso de uso para listar los gastos fijos de una cuenta.
     */
    private final ListFixedExpensesUseCase listFixedExpensesUseCase;
     /**
     * Caso de uso para actualizar parcialmente un gasto fijo.
     */
    private final UpdateFixedExpenseUseCase updateFixedExpenseUseCase;
    /**
     * Caso de uso para eliminar un gasto fijo.
     */
    private final DeleteFixedExpenseUseCase deleteFixedExpenseUseCase;

     /**
     * Retorna el identificador estable de la cuenta autenticada.
     */
    private UUID resolveAccountId(AuthenticatedAccount authenticatedAccount) {
        return authenticatedAccount.accountId();
    }

    //Movimientos

    /**
     * Registra un nuevo movimiento financiero (ingreso o egreso) en la cuenta
     * del usuario autenticado.
     *
     * @param request    objeto {@link CreateMovementRequest} con los datos
     *                   validados del movimiento a registrar.
     * @param authenticatedAccount cuenta autenticada.
     * @return {@link ResponseEntity} con estado {@code 201 Created} y el
     *         {@link MovementResponse} del movimiento creado.
     * @throws NotFoundException si la cuenta del usuario no existe.
     */

    @PostMapping("/movements")
    public ResponseEntity<ApiResponse<MovementResponse>> createMovement(@Validated @RequestBody CreateMovementRequest request,
         @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount) {

        UUID accountId = resolveAccountId(authenticatedAccount);
        MovementResponse response = createMovementUseCase.execute(accountId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<MovementResponse>builder()
            .status(HttpStatus.CREATED.value())
            .message("Movimiento registrado exitosamente")
            .data(response)
            .build());
    }

    /**
     * Obtiene el detalle de un movimiento financiero específico de la cuenta
     * del usuario autenticado.
     *
     * @param id          UUID del movimiento que se desea consultar.
     * @param authenticatedAccount cuenta autenticada.
     * @return {@link ResponseEntity} con estado {@code 200 OK} y el
     *         {@link MovementResponse} del movimiento encontrado.
     * @throws NotFoundException si el movimiento no existe o no pertenece
     *                           a la cuenta del usuario.
     */

    @GetMapping("/movements/{id}")
    public ResponseEntity<ApiResponse<MovementResponse>> getMovement(@PathVariable UUID id,
        @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount) {

        UUID accountId = resolveAccountId(authenticatedAccount);
        MovementResponse response = getMovementUseCase.execute(accountId, id);

       return ResponseEntity.ok(ApiResponse.<MovementResponse>builder()
            .status(HttpStatus.OK.value())
            .message("Movimiento obtenido exitosamente")
            .data(response)
            .build());
    }


     /**
     * Lista los movimientos financieros de la cuenta del usuario autenticado,
     * con soporte de filtrado opcional por tipo, categoría, rango de fechas
     * y término de búsqueda.
     *
     * @param filter      objeto {@link MovementFilterRequest} con los criterios
     *                    de filtrado opcionales. Puede ser {@code null}.
     * @param authenticatedAccount cuenta autenticada.
     * @return {@link ResponseEntity} con estado {@code 200 OK} y la lista de
     *         {@link MovementResponse} que cumplen los criterios del filtro.
     * @throws NotFoundException si la cuenta del usuario no existe.
     */

    @GetMapping("/movements")
    public ResponseEntity<ApiResponse<List<MovementResponse>>> listMovements(
        @Valid MovementFilterRequest filter,
        @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount) {

        UUID accountId = resolveAccountId(authenticatedAccount);
        PageResult<MovementResponse> response = listMovementUseCase.execute(accountId, filter);

        return ResponseEntity.ok(ApiResponse.<List<MovementResponse>>builder()
            .status(HttpStatus.OK.value())
            .message("Movimientos listados exitosamente")
            .data(response.content())
            .page(PageMetadata.from(response))
            .build());
    }

    /**
     * Actualiza parcialmente un movimiento financiero existente en la cuenta
     * del usuario autenticado.
     *
     * @param id          UUID del movimiento que se desea actualizar.
     * @param request     objeto {@link UpdateMovementRequest} con los campos
     *                    a modificar. Los campos no enviados permanecen sin cambios.
     * @param authenticatedAccount cuenta autenticada.
     * @return {@link ResponseEntity} con estado {@code 200 OK} y el
     *         {@link MovementResponse} con los datos actualizados.
     * @throws NotFoundException si el movimiento no existe o no pertenece
     *                           a la cuenta del usuario.
     */

    @PatchMapping("/movements/{id}")
    public ResponseEntity<ApiResponse<MovementResponse>> updateMovement(@PathVariable UUID id, @Validated @RequestBody
        UpdateMovementRequest request, @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount) {

        UUID accountId = resolveAccountId(authenticatedAccount);
        MovementResponse response = updateMovementUseCase.execute(accountId, id, request);

        return ResponseEntity.ok(ApiResponse.<MovementResponse>builder()
            .status(HttpStatus.OK.value())
            .message("Movimiento actualizado exitosamente")
            .data(response)
            .build());
    }

    /**
     * Elimina un movimiento financiero de la cuenta del usuario autenticado.
     *
     * @param id          UUID del movimiento que se desea eliminar.
     * @param authenticatedAccount cuenta autenticada.
     * @return {@link ResponseEntity} con estado {@code 200 OK} confirmando
     *         la eliminación.
     * @throws NotFoundException si el movimiento no existe o no pertenece
     *                           a la cuenta del usuario.
     */

    @DeleteMapping("/movements/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMovement(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount) {

        UUID accountId = resolveAccountId(authenticatedAccount);
        deleteMovementUseCase.execute(accountId, id);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
            .status(HttpStatus.OK.value())
            .message("Movimiento eliminado exitosamente")
            .build());
    }

    //Categorias

    /**
     * Crea una categoría personal para la cuenta autenticada.
     *
     * @param request     objeto {@link CreateCategoryRequest} con los datos
     *                    validados de la categoría a crear.
     * @param authenticatedAccount cuenta autenticada.
     * @return {@link ResponseEntity} con estado {@code 201 Created} y el
     *         {@link CategoryResponse} de la categoría creada.
     */

    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@Validated @RequestBody
        CreateCategoryRequest request, @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount) {

        UUID accountId = resolveAccountId(authenticatedAccount);
        CategoryResponse response = createCategoryUseCase.execute(accountId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<CategoryResponse>builder()
            .status(HttpStatus.CREATED.value())
            .message("Categoría creada exitosamente")
            .data(response)
            .build());
    }

    /**
     * Obtiene el detalle de una categoría financiera por su identificador.
     *
     * @param id          UUID de la categoría que se desea consultar.
     * @param authenticatedAccount cuenta autenticada.
     * @return {@link ResponseEntity} con estado {@code 200 OK} y el
     *         {@link CategoryResponse} de la categoría encontrada.
     * @throws NotFoundException si no existe ninguna categoría con el
     *                           identificador proporcionado.
     */

    @GetMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategory(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount) {

        UUID accountId = resolveAccountId(authenticatedAccount);
        CategoryResponse response = getCategoryUseCase.execute(accountId, id);

        return ResponseEntity.ok(ApiResponse.<CategoryResponse>builder()
            .status(HttpStatus.OK.value())
            .message("Categoría obtenida exitosamente")
            .data(response)
            .build());
    }

    /**
     * Lista las categorías financieras del sistema con filtrado opcional por tipo.
     *
     * @param filter      objeto {@link CategoryFilterRequest} con los criterios
     *                    de filtrado opcionales. Puede ser {@code null}.
     * @param authenticatedAccount cuenta autenticada.
     * @return {@link ResponseEntity} con estado {@code 200 OK} y la lista de
     *         {@link CategoryResponse} que cumplen los criterios del filtro.
     */

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> listCategory(CategoryFilterRequest filter,
        @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount) {

        UUID accountId = resolveAccountId(authenticatedAccount);
        List<CategoryResponse> response = listCategoriesUseCase.execute(accountId, filter);

        return ResponseEntity.ok(ApiResponse.<List<CategoryResponse>>builder()
            .status(HttpStatus.OK.value())
            .message("Categorías listadas exitosamente")
            .data(response)
            .build());
    }

    /**
     * Actualiza parcialmente una categoría financiera existente.
     *
     * <p>Solo permite renombrar una categoría PERSONAL de la cuenta.</p>
     *
     * @param id          UUID de la categoría que se desea actualizar.
     * @param request     objeto {@link UpdateCategoryRequest} con los campos
     *                    a modificar. Los campos no enviados permanecen sin cambios.
     * @param authenticatedAccount cuenta autenticada.
     * @return {@link ResponseEntity} con estado {@code 200 OK} y el
     *         {@link CategoryResponse} con los datos actualizados.
     * @throws NotFoundException si no existe ninguna categoría con el
     *                           identificador proporcionado.
     */

    @PatchMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(@PathVariable UUID id, @Validated
        @RequestBody UpdateCategoryRequest request, @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount) {

        UUID accountId = resolveAccountId(authenticatedAccount);
        CategoryResponse response = updateCategoryUseCase.execute(accountId, id, request);

        return ResponseEntity.ok(ApiResponse.<CategoryResponse>builder()
            .status(HttpStatus.OK.value())
            .message("Categoría actualizada exitosamente")
            .data(response)
            .build());
    }

    /**
     * Elimina una categoría PERSONAL de la cuenta autenticada.
     *
     * @param id          UUID de la categoría que se desea eliminar.
     * @param authenticatedAccount cuenta autenticada.
     * @return {@link ResponseEntity} con estado {@code 200 OK} confirmando
     *         la eliminación.
     * @throws NotFoundException si no existe ninguna categoría con el
     *                           identificador proporcionado.
     */

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount) {

        UUID accountId = resolveAccountId(authenticatedAccount);
        deleteCategoryUseCase.execute(accountId, id);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
            .status(HttpStatus.OK.value())
            .message("Categoría eliminada exitosamente")
            .build());
    }


    //Gastos fijos

    /**
     * Registra un nuevo gasto fijo recurrente en la cuenta del usuario autenticado.
     *
     * @param request     objeto {@link CreateFixedExpenseRequest} con los datos
     *                    validados del gasto fijo a registrar.
     * @param authenticatedAccount cuenta autenticada.
     * @return {@link ResponseEntity} con estado {@code 201 Created} y el
     *         {@link FixedExpenseResponse} del gasto fijo creado.
     * @throws NotFoundException si la cuenta del usuario no existe.
     */

    @PostMapping("/fixed-expenses")
    public ResponseEntity<ApiResponse<FixedExpenseResponse>> createFixedExpense(@Validated @RequestBody
        CreateFixedExpenseRequest request, @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount) {

        UUID accountId = resolveAccountId(authenticatedAccount);
        FixedExpenseResponse response = createFixedExpenseUseCase.execute(accountId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<FixedExpenseResponse>builder()
            .status(HttpStatus.CREATED.value())
            .message("Gasto fijo creado exitosamente")
            .data(response)
            .build());
    }

     /**
     * Obtiene el detalle de un gasto fijo específico de la cuenta del usuario
     * autenticado.
     *
     * @param id          UUID del gasto fijo que se desea consultar.
     * @param authenticatedAccount cuenta autenticada.
     * @return {@link ResponseEntity} con estado {@code 200 OK} y el
     *         {@link FixedExpenseResponse} del gasto fijo encontrado.
     * @throws NotFoundException si el gasto fijo no existe.
     */

    @GetMapping("/fixed-expenses/{id}")
    public ResponseEntity<ApiResponse<FixedExpenseResponse>> getFixedExpense(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount) {

        UUID accountId = resolveAccountId(authenticatedAccount);
        FixedExpenseResponse response = getFixedExpenseUseCase.execute(accountId, id);

        return ResponseEntity.ok(ApiResponse.<FixedExpenseResponse>builder()
            .status(HttpStatus.OK.value())
            .message("Gasto fijo obtenido exitosamente")
            .data(response)
            .build());
    }

    /**
     * Lista los gastos fijos de la cuenta del usuario autenticado, con soporte
     * de filtrado opcional por estado, categoría y término de búsqueda.
     *
     * @param filter      objeto {@link FixedExpenseFilterRequest} con los criterios
     *                    de filtrado opcionales. Puede ser {@code null}.
     * @param authenticatedAccount cuenta autenticada.
     * @return {@link ResponseEntity} con estado {@code 200 OK} y la lista de
     *         {@link FixedExpenseResponse} que cumplen los criterios del filtro.
     * @throws NotFoundException si la cuenta del usuario no existe.
     */


    @GetMapping("/fixed-expenses")
    public ResponseEntity<ApiResponse<List<FixedExpenseResponse>>> listFixedExpenses(
        @Valid FixedExpenseFilterRequest filter,
        @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount) {

        UUID accountId = resolveAccountId(authenticatedAccount);
        PageResult<FixedExpenseResponse> response = listFixedExpensesUseCase.execute(accountId, filter);


        return ResponseEntity.ok(ApiResponse.<List<FixedExpenseResponse>>builder()
            .status(HttpStatus.OK.value())
            .message("Gastos fijos listados exitosamente")
            .data(response.content())
            .page(PageMetadata.from(response))
            .build());
    }

     /**
     * Actualiza parcialmente un gasto fijo existente en la cuenta del usuario
     * autenticado.
     *
     * @param id          UUID del gasto fijo que se desea actualizar.
     * @param request     objeto {@link UpdateFixedExpenseRequest} con los campos
     *                    a modificar. Los campos no enviados permanecen sin cambios.
     * @param authenticatedAccount cuenta autenticada.
     * @return {@link ResponseEntity} con estado {@code 200 OK} y el
     *         {@link FixedExpenseResponse} con los datos actualizados.
     * @throws NotFoundException si el gasto fijo no existe.
     */

    @PatchMapping("/fixed-expenses/{id}")
    public ResponseEntity<ApiResponse<FixedExpenseResponse>> updateFixedExpense(@PathVariable UUID id, @Validated
        @RequestBody UpdateFixedExpenseRequest request, @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount) {

        UUID accountId = resolveAccountId(authenticatedAccount);
        FixedExpenseResponse response = updateFixedExpenseUseCase.execute(accountId, id, request);

        return ResponseEntity.ok(ApiResponse.<FixedExpenseResponse>builder()
            .status(HttpStatus.OK.value())
            .message("Gasto fijo actualizado exitosamente")
            .data(response)
            .build());
    }

    /**
     * Elimina un gasto fijo de la cuenta del usuario autenticado.
     *
     * @param id          UUID del gasto fijo que se desea eliminar.
     * @param authenticatedAccount cuenta autenticada.
     * @return {@link ResponseEntity} con estado {@code 200 OK} confirmando
     *         la eliminación.
     * @throws NotFoundException si el gasto fijo no existe.
     */


    @DeleteMapping("/fixed-expenses/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFixedExpense(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount) {

        UUID accountId = resolveAccountId(authenticatedAccount);
        deleteFixedExpenseUseCase.execute(accountId, id);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
            .status(HttpStatus.OK.value())
            .message("Gasto fijo eliminado exitosamente")
            .build());
    }

}
