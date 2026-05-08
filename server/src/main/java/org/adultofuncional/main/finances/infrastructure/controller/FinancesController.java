package org.adultofuncional.main.finances.infrastructure.controller;

import java.util.List;
import java.util.UUID;

import org.adultofuncional.main.account.domain.repository.AccountRepository;
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
@RequestMapping("/api/finances")
@RequiredArgsConstructor
public class FinancesController {

    private final CreateMovementUseCase createMovementUseCase;
    private final GetMovementUseCase getMovementUseCase;
    private final ListMovementsUseCase listMovementUseCase;
    private final UpdateMovementUseCase updateMovementUseCase;
    private final DeleteMovementUseCase deleteMovementUseCase;

    private final CreateCategoryUseCase createCategoryUseCase;
    private final GetCategoryUseCase getCategoryUseCase;
    private final ListCategoriesUseCase listCategoriesUseCase;
    private final UpdateCategoryUseCase updateCategoryUseCase;
    private final DeleteCategoryUseCase deleteCategoryUseCase;

    private final CreateFixedExpenseUseCase createFixedExpenseUseCase;
    private final GetFixedExpenseUseCase getFixedExpenseUseCase;
    private final ListFixedExpensesUseCase listFixedExpensesUseCase;
    private final UpdateFixedExpenseUseCase updateFixedExpenseUseCase;
    private final DeleteFixedExpenseUseCase deleteFixedExpenseUseCase;

    private final AccountRepository accountRepository;

    private UUID resolveAccountId(String email) {
        return accountRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException("Cuenta no encontrada para el email: " + email))
            .getId();
    }

    //Movimientos

    @PostMapping("/movements")
    public ResponseEntity<ApiResponse<MovementResponse>> createMovement(@Validated @RequestBody CreateMovementRequest request,
         @AuthenticationPrincipal String loggedEmail) {

        UUID accountId = resolveAccountId(loggedEmail);
        MovementResponse response = createMovementUseCase.execute(accountId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<MovementResponse>builder()
            .status(HttpStatus.CREATED.value())
            .message("Movimiento registrado exitosamente")
            .data(response)
            .build());
    }

    @GetMapping("/movements/{id}")
    public ResponseEntity<ApiResponse<MovementResponse>> getMovement(@PathVariable UUID id,
        @AuthenticationPrincipal String loggedEmail) {
        
        UUID accountId = resolveAccountId(loggedEmail);
        MovementResponse response = getMovementUseCase.execute(accountId, id);

       return ResponseEntity.ok(ApiResponse.<MovementResponse>builder()
            .status(HttpStatus.OK.value())
            .message("Movimiento obtenido exitosamente")
            .data(response)
            .build());
    }


    @GetMapping("/movements")
    public ResponseEntity<ApiResponse<List<MovementResponse>>> listMovements(MovementFilterRequest filter, 
        @AuthenticationPrincipal String loggedEmail) {

        UUID accountId = resolveAccountId(loggedEmail);
        List<MovementResponse> response = listMovementUseCase.execute(accountId, filter);

        return ResponseEntity.ok(ApiResponse.<List<MovementResponse>>builder()
            .status(HttpStatus.OK.value())
            .message("Movimientos listados exitosamente")
            .data(response)
            .build());
    }


    @PatchMapping("/movements/{id}")
    public ResponseEntity<ApiResponse<MovementResponse>> updateMovement(@PathVariable UUID id, @Validated @RequestBody 
        UpdateMovementRequest request, @AuthenticationPrincipal String loggedEmail) {

        UUID accountId = resolveAccountId(loggedEmail);
        MovementResponse response = updateMovementUseCase.execute(accountId, id, request);

        return ResponseEntity.ok(ApiResponse.<MovementResponse>builder()
            .status(HttpStatus.OK.value())
            .message("Movimiento actualizado exitosamente")
            .data(response)
            .build());
    }


    @DeleteMapping("/movements/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMovement(@PathVariable UUID id, @AuthenticationPrincipal String loggedEmail) {

        UUID accountId = resolveAccountId(loggedEmail);
        deleteMovementUseCase.execute(accountId, id); 

        return ResponseEntity.ok(ApiResponse.<Void>builder()
            .status(HttpStatus.OK.value())
            .message("Movimiento eliminado exitosamente")
            .build());
    }

    //Categorias

    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@Validated @RequestBody 
        CreateCategoryRequest request, @AuthenticationPrincipal String loggedEmail) {

        CategoryResponse response = createCategoryUseCase.execute(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<CategoryResponse>builder()
            .status(HttpStatus.CREATED.value())
            .message("Categoría creada exitosamente")
            .data(response)
            .build());
    }


    @GetMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategory(@PathVariable UUID id, @AuthenticationPrincipal String loggedEmail) {

        CategoryResponse response = getCategoryUseCase.execute(id);

        return ResponseEntity.ok(ApiResponse.<CategoryResponse>builder()
            .status(HttpStatus.OK.value())
            .message("Categoría obtenida exitosamente")
            .data(response)
            .build());
    }


    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> listCategory(CategoryFilterRequest filter, 
        @AuthenticationPrincipal String loggedEmail) {

        List<CategoryResponse> response = listCategoriesUseCase.execute(filter);

        return ResponseEntity.ok(ApiResponse.<List<CategoryResponse>>builder()
            .status(HttpStatus.OK.value())
            .message("Categorías listadas exitosamente")
            .data(response)
            .build());
    }

    @PatchMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(@PathVariable UUID id, @Validated
        @RequestBody UpdateCategoryRequest request, @AuthenticationPrincipal String loggedEmail) {

        CategoryResponse response = updateCategoryUseCase.execute(id,request);

        return ResponseEntity.ok(ApiResponse.<CategoryResponse>builder()
            .status(HttpStatus.OK.value())
            .message("Categoría actualizada exitosamente")
            .data(response)
            .build());
    }


    @DeleteMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable UUID id, @AuthenticationPrincipal String loggedEmail) {

        deleteCategoryUseCase.execute(id);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
            .status(HttpStatus.OK.value())
            .message("Categoría eliminada exitosamente")
            .build());
    }


    //Gastos fijos

    @PostMapping("/fixed-expenses")
    public ResponseEntity<ApiResponse<FixedExpenseResponse>> createFixedExpense(@Validated @RequestBody 
        CreateFixedExpenseRequest request, @AuthenticationPrincipal String loggedEmail) {

        UUID accountId = resolveAccountId(loggedEmail);
        FixedExpenseResponse response = createFixedExpenseUseCase.execute(accountId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<FixedExpenseResponse>builder()
            .status(HttpStatus.CREATED.value())
            .message("Gasto fijo creado exitosamente")
            .data(response)
            .build());
    }


    @GetMapping("/fixed-expenses/{id}")
    public ResponseEntity<ApiResponse<FixedExpenseResponse>> getFixedExpense(@PathVariable UUID id, @AuthenticationPrincipal String loggedEmail) {

        UUID accountId = resolveAccountId(loggedEmail);
        FixedExpenseResponse response = getFixedExpenseUseCase.execute(accountId, id);

        return ResponseEntity.ok(ApiResponse.<FixedExpenseResponse>builder()
            .status(HttpStatus.OK.value())
            .message("Gasto fijo obtenido exitosamente")
            .data(response)
            .build());
    }


    @GetMapping("/fixed-expenses")
    public ResponseEntity<ApiResponse<List<FixedExpenseResponse>>> listFixedExpenses(FixedExpenseFilterRequest filter,
        @AuthenticationPrincipal String loggedEmail) {

        UUID accountId = resolveAccountId(loggedEmail);
        List<FixedExpenseResponse> response = listFixedExpensesUseCase.execute(accountId, filter);


        return ResponseEntity.ok(ApiResponse.<List<FixedExpenseResponse>>builder()
            .status(HttpStatus.OK.value())
            .message("Gastos fijos listados exitosamente")
            .data(response)
            .build());
    }


    @PatchMapping("/fixed-expenses/{id}")
    public ResponseEntity<ApiResponse<FixedExpenseResponse>> updateFixedExpense(@PathVariable UUID id, @Validated
        @RequestBody UpdateFixedExpenseRequest request, @AuthenticationPrincipal String loggedEmail) {
        
        UUID accountId = resolveAccountId(loggedEmail);
        FixedExpenseResponse response = updateFixedExpenseUseCase.execute(accountId, id, request); 
        
        return ResponseEntity.ok(ApiResponse.<FixedExpenseResponse>builder()
            .status(HttpStatus.OK.value())
            .message("Gasto fijo actualizado exitosamente")
            .data(response)
            .build());
    }


    @DeleteMapping("/fixed-expenses/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFixedExpense(@PathVariable UUID id, @AuthenticationPrincipal String loggedEmail) {
        
        UUID accountId = resolveAccountId(loggedEmail);
        deleteFixedExpenseUseCase.execute(accountId, id);
        
        return ResponseEntity.ok(ApiResponse.<Void>builder()
            .status(HttpStatus.OK.value())
            .message("Gasto fijo eliminado exitosamente")
            .build());
    }
    


}
