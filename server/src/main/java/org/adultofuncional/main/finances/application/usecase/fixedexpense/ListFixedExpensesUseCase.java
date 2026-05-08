package org.adultofuncional.main.finances.application.usecase.fixedexpense;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.finances.application.dto.category.CategoryResponse;
import org.adultofuncional.main.finances.application.dto.fixedexpense.FixedExpenseFilterRequest;
import org.adultofuncional.main.finances.application.dto.fixedexpense.FixedExpenseResponse;
import org.adultofuncional.main.finances.domain.model.Category;
import org.adultofuncional.main.finances.domain.model.FixedExpense;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.adultofuncional.main.finances.domain.repository.FixedExpenseRepository;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;

/**
 * Caso de uso responsable de la consulta y listado de gastos fijos
 * asociados a una cuenta del usuario, con soporte de filtrado opcional
 * por estado, categoría y término de búsqueda.
 *
 * <p>
 * Esta clase implementa el caso de uso de listado de gastos fijos dentro
 * de la capa de aplicación, orquestando la validación de la cuenta, la
 * recuperación de las entidades desde el repositorio, la aplicación de
 * filtros en memoria y la transformación de los resultados a la respuesta
 * esperada por el invocador.
 * </p>
 *
 * <p>
 * <b>¿Qué es?</b><br>
 * Un servicio de aplicación de responsabilidad única que encapsula
 * exclusivamente la lógica del caso de uso "listar gastos fijos de una cuenta".
 * Es gestionado por el contenedor de Spring mediante {@code @Service},
 * e inyecta sus dependencias a través del constructor generado por
 * {@code @RequiredArgsConstructor} de Lombok.
 * </p>
 *
 * <p>
 * <b>¿Para qué sirve?</b><br>
 * Recibe el identificador de una cuenta y criterios de filtrado opcionales,
 * valida que la cuenta exista, recupera todos los gastos fijos asociados
 * a ella y aplica de forma encadenada los filtros disponibles: por estado,
 * por categoría y por término de búsqueda en el nombre. Retorna la lista
 * resultante como una colección de {@link FixedExpenseResponse}.
 * </p>
 *
 * <p>
 * <b>¿Cómo funciona?</b><br>
 * El método {@code execute} es el único punto de entrada del caso de uso
 * y aplica el siguiente flujo:
 * </p>
 * <ol>
 * <li>Verifica que la cuenta identificada por {@code accountId} exista
 * en el sistema, lanzando {@link NotFoundException} si no se encuentra.</li>
 * <li>Recupera todos los gastos fijos asociados a la cuenta mediante
 * {@code findAllByAccountId(accountId)}.</li>
 * <li>Si el filtro no es nulo, aplica de forma encadenada y en memoria
 * los siguientes criterios sobre la lista recuperada:
 * <ul>
 * <li>Filtra por estado si {@code filter.getStatus()} no es nulo.</li>
 * <li>Filtra por categoría si {@code filter.getCategoryId()} no es nulo,
 * ignorando los gastos sin categoría asociada.</li>
 * <li>Filtra por término de búsqueda si {@code filter.getSearchTerm()}
 * tiene texto real, realizando una comparación insensible a
 * mayúsculas sobre el nombre del gasto fijo.</li>
 * </ul>
 * </li>
 * <li>Transforma cada entidad {@link FixedExpense} resultante en un
 * {@link FixedExpenseResponse} mediante un mapeo con Streams
 * y retorna la lista final.</li>
 * </ol>
 * <p>
 * La anotación {@code @Transactional(readOnly = true)} optimiza la operación
 * indicando al motor de persistencia que no habrá escrituras durante
 * su ejecución.
 * </p>
 *
 * @author Miguel Angel Blandon Montes
 */
@Service
@RequiredArgsConstructor
public class ListFixedExpensesUseCase {

  /**
   * Repositorio de gastos fijos utilizado para recuperar todas las entidades
   * asociadas a una cuenta específica del sistema.
   *
   * <p>
   * Abstracción de la capa de persistencia que desacopla el caso de uso
   * de la implementación concreta del almacenamiento. Es inyectado
   * automáticamente por Spring a través del constructor generado
   * por {@code @RequiredArgsConstructor}.
   * </p>
   */
  private final FixedExpenseRepository fixedExpenseRepository;

  /**
   * Repositorio de cuentas utilizado para validar la existencia de la cuenta
   * antes de recuperar sus gastos fijos asociados.
   *
   * <p>
   * Abstracción de la capa de persistencia del módulo de cuentas.
   * Es inyectado automáticamente por Spring a través del constructor generado
   * por {@code @RequiredArgsConstructor}.
   * </p>
   */
  private final AccountRepository accountRepository;
  private final CategoryRepository categoryRepository;

  /**
   * Ejecuta el caso de uso de listado de gastos fijos de una cuenta,
   * con filtrado opcional por estado, categoría y término de búsqueda.
   *
   * <p>
   * Valida primero la existencia de la cuenta, recupera todos sus gastos
   * fijos asociados y aplica de forma encadenada los filtros definidos en el
   * {@link FixedExpenseFilterRequest}. Cada filtro se aplica únicamente si
   * su valor correspondiente está presente. Finalmente transforma las entidades
   * resultantes en {@link FixedExpenseResponse} y retorna la lista.
   * </p>
   *
   * <p>
   * El filtrado por término de búsqueda se realiza de forma insensible a
   * mayúsculas y minúsculas ({@code toLowerCase()}) sobre el nombre del gasto
   * fijo, verificando si contiene el término proporcionado como subcadena.
   * La verificación del término se realiza con {@link StringUtils#hasText},
   * que garantiza que el valor no sea nulo, vacío ni compuesto únicamente
   * por espacios en blanco.
   * </p>
   *
   * <p>
   * La operación se ejecuta dentro de una transacción de solo lectura
   * gracias a {@code @Transactional(readOnly = true)}, lo que permite al
   * motor de persistencia aplicar optimizaciones de rendimiento al garantizar
   * que no se realizarán operaciones de escritura durante su ejecución.
   * </p>
   *
   * @param accountId identificador UUID de la cuenta del usuario cuyos gastos
   *                  fijos se desean listar.
   * @param filter    objeto {@link FixedExpenseFilterRequest} que contiene los
   *                  criterios de filtrado a aplicar sobre el listado. Puede ser
   *                  {@code null}, en cuyo caso se retornan todos los gastos
   *                  fijos
   *                  de la cuenta sin ningún filtro aplicado.
   * @return lista de {@link FixedExpenseResponse} con los datos de los gastos
   *         fijos que cumplen los criterios del filtro. La categoría se retorna
   *         como {@code null} en cada respuesta. Retorna una lista vacía si no
   *         existen gastos fijos que coincidan con los criterios aplicados.
   * @throws NotFoundException si no existe ninguna cuenta registrada con el
   *                           identificador {@code accountId} proporcionado.
   */
  @Transactional(readOnly = true)
  public List<FixedExpenseResponse> execute(UUID accountId, FixedExpenseFilterRequest filter) {
    accountRepository.findById(accountId)
        .orElseThrow(() -> new NotFoundException("Cuenta no encontrada con id: " + accountId));

    List<FixedExpense> expenses = fixedExpenseRepository.findAllByAccountId(accountId);

    if (filter != null) {
      if (filter.getStatus() != null) {
        expenses = expenses.stream()
            .filter(e -> e.getStatus() == filter.getStatus())
            .collect(Collectors.toList());
      }
      if (filter.getCategoryId() != null) {
        expenses = expenses.stream()
            .filter(e -> e.getCategoryId() != null && e.getCategoryId().equals(filter.getCategoryId()))
            .collect(Collectors.toList());
      }
      if (StringUtils.hasText(filter.getSearchTerm())) {
        String term = filter.getSearchTerm().toLowerCase();
        expenses = expenses.stream()
            .filter(e -> e.getName().toLowerCase().contains(term))
            .collect(Collectors.toList());
      }
    }

    Set<UUID> categoryIds = expenses.stream()
        .map(FixedExpense::getCategoryId)
        .collect(Collectors.toSet());
    Map<UUID, Category> categoryMap = categoryRepository.findAllById(categoryIds).stream()
        .collect(Collectors.toMap(Category::getId, Function.identity()));

    return expenses.stream()
        .map(e -> {
          Category cat = categoryMap.get(e.getCategoryId());
          CategoryResponse catResp = cat != null ? CategoryResponse.builder()
              .id(cat.getId())
              .name(cat.getName())
              .type(cat.getType())
              .build() : null;

          return FixedExpenseResponse.builder()
              .id(e.getId())
              .name(e.getName())
              .frequency(e.getFrequency())
              .amount(e.getAmount())
              .status(e.getStatus())
              .nextDueDate(e.getNextDueDate())
              .category(catResp)
              .build();
        })
        .collect(Collectors.toList());
  }
}
