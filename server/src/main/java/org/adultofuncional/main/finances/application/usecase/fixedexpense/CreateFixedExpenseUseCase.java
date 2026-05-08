package org.adultofuncional.main.finances.application.usecase.fixedexpense;

import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.finances.application.dto.category.CategoryResponse;
import org.adultofuncional.main.finances.application.dto.fixedexpense.CreateFixedExpenseRequest;
import org.adultofuncional.main.finances.application.dto.fixedexpense.FixedExpenseResponse;
import org.adultofuncional.main.finances.domain.model.Category;
import org.adultofuncional.main.finances.domain.model.FixedExpense;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.adultofuncional.main.finances.domain.repository.FixedExpenseRepository;
import org.adultofuncional.main.shared.exception.BusinessException;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso responsable de la creación de un nuevo gasto fijo
 * asociado a una cuenta del usuario en el sistema financiero.
 *
 * <p>
 * Esta clase implementa el caso de uso de creación de gastos fijos dentro
 * de la capa de aplicación, orquestando las validaciones de negocio necesarias,
 * la construcción de la entidad de dominio y su persistencia.
 * </p>
 *
 * <p>
 * <b>¿Qué es?</b><br>
 * Un servicio de aplicación de responsabilidad única que encapsula
 * exclusivamente la lógica del caso de uso "crear gasto fijo". Es gestionado
 * por el contenedor de Spring mediante {@code @Service}, e inyecta sus
 * dependencias a través del constructor generado por
 * {@code @RequiredArgsConstructor}
 * de Lombok.
 * </p>
 *
 * <p>
 * <b>¿Para qué sirve?</b><br>
 * Recibe el identificador de una cuenta y una solicitud de creación de gasto
 * fijo, valida que la cuenta exista, que la fecha de cierre sea futura y que
 * la categoría asociada exista si fue proporcionada. Con los datos validados,
 * construye y persiste la entidad {@link FixedExpense} y retorna su
 * representación como {@link FixedExpenseResponse}.
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
 * <li>Valida que la fecha de cierre proporcionada sea posterior a la fecha
 * actual, lanzando {@link BusinessException} si la regla no se cumple.</li>
 * <li>Si se proporcionó un {@code categoryId}, verifica que la categoría
 * exista en el sistema, lanzando {@link NotFoundException} si no se
 * encuentra. Para garantizar compatibilidad con lambdas, el UUID se
 * extrae en una variable efectivamente final antes de la consulta.</li>
 * <li>Define la fecha de inicio como la fecha actual y la fecha de próximo
 * vencimiento como la fecha de cierre solicitada, con cero días de
 * recordatorio por defecto.</li>
 * <li>Construye la entidad {@link FixedExpense} mediante el método de fábrica
 * {@code FixedExpense.create()} del dominio y la persiste a través
 * del repositorio.</li>
 * <li>Retorna un {@link FixedExpenseResponse} con los datos de la entidad
 * persistida.</li>
 * </ol>
 * <p>
 * La anotación {@code @Transactional} garantiza que todas las operaciones
 * se ejecuten dentro de una única transacción de base de datos.
 * </p>
 *
 * @author Miguel Angel Blandon Montes
 */
@Service
@RequiredArgsConstructor
public class CreateFixedExpenseUseCase {

  /**
   * Repositorio de gastos fijos utilizado para persistir la nueva entidad.
   *
   * <p>
   * Abstracción de la capa de persistencia que desacopla el caso de uso
   * de la implementación concreta del almacenamiento de gastos fijos.
   * Es inyectado automáticamente por Spring a través del constructor generado
   * por {@code @RequiredArgsConstructor}.
   * </p>
   */
  private final FixedExpenseRepository fixedExpenseRepository;

  /**
   * Repositorio de cuentas utilizado para validar la existencia de la cuenta
   * a la que se asociará el gasto fijo.
   *
   * <p>
   * Abstracción de la capa de persistencia del módulo de cuentas.
   * Es inyectado automáticamente por Spring a través del constructor generado
   * por {@code @RequiredArgsConstructor}.
   * </p>
   */
  private final AccountRepository accountRepository;

  /**
   * Repositorio de categorías utilizado para validar la existencia de la
   * categoría asociada al gasto fijo cuando se proporciona.
   *
   * <p>
   * Abstracción de la capa de persistencia que permite verificar que
   * el identificador de categoría recibido en la solicitud corresponde
   * a una categoría registrada en el sistema. Es inyectado automáticamente
   * por Spring a través del constructor generado por
   * {@code @RequiredArgsConstructor}.
   * </p>
   */
  private final CategoryRepository categoryRepository;

  /**
   * Ejecuta el caso de uso de creación de un nuevo gasto fijo asociado
   * a una cuenta del usuario.
   *
   * <p>
   * Aplica las siguientes validaciones de negocio antes de persistir
   * la entidad: verifica la existencia de la cuenta, valida que la fecha
   * de cierre sea futura y, si se proporcionó, verifica la existencia de
   * la categoría. Con los datos validados, construye la entidad de dominio
   * mediante el método de fábrica {@code FixedExpense.create()}, la persiste
   * y retorna su representación como {@link FixedExpenseResponse}.
   * </p>
   *
   * <p>
   * La fecha de inicio se establece automáticamente como la fecha actual
   * del sistema ({@code LocalDate.now()}), la fecha de próximo vencimiento
   * corresponde a la fecha de cierre solicitada, y los días de recordatorio
   * se inicializan en cero por defecto.
   * </p>
   *
   * <p>
   * La operación se ejecuta dentro de una transacción de base de datos
   * gracias a {@code @Transactional}, garantizando que cualquier fallo
   * en alguna de las validaciones o en la persistencia revierta todos
   * los cambios y mantenga la integridad del sistema.
   * </p>
   *
   * @param accountId identificador UUID de la cuenta del usuario a la que
   *                  se asociará el nuevo gasto fijo.
   * @param request   objeto {@link CreateFixedExpenseRequest} que contiene
   *                  los datos validados del gasto fijo a crear: nombre,
   *                  frecuencia, monto, estado, fecha de cierre y categoría
   *                  opcional.
   * @return {@link FixedExpenseResponse} con los datos completos del gasto
   *         fijo recién creado, incluyendo su identificador UUID, nombre,
   *         frecuencia, monto, estado y fecha de cierre. La categoría se
   *         retorna como {@code null} en esta respuesta.
   * @throws NotFoundException si la cuenta identificada por {@code accountId}
   *                           no existe, o si la categoría identificada por
   *                           {@code categoryId} no existe en el sistema.
   * @throws BusinessException si la fecha de cierre proporcionada es anterior
   *                           o igual a la fecha actual del sistema.
   */
  @Transactional
  public FixedExpenseResponse execute(UUID accountId, CreateFixedExpenseRequest request) {
    accountRepository.findById(accountId)
        .orElseThrow(() -> new NotFoundException("Cuenta no encontrada con id: " + accountId));

    if (request.getNextDueDate().isBefore(LocalDate.now())) {
      throw new BusinessException("La fecha de cierre debe ser posterior a la fecha actual");
    }

    Category category = categoryRepository.findById(request.getCategoryId())
        .orElseThrow(() -> new NotFoundException("Categoría no encontrada con id: " + request.getCategoryId()));

    FixedExpense expense = FixedExpense.create(
        request.getName(),
        request.getAmount(),
        request.getCategoryId(),
        accountId,
        request.getFrequency(),
        LocalDate.now(),
        request.getNextDueDate(),
        0);

    FixedExpense saved = fixedExpenseRepository.save(expense);

    CategoryResponse categoryResponse = CategoryResponse.builder()
        .id(category.getId())
        .name(saved.getName())
        .type(category.getType())
        .build();

    return FixedExpenseResponse.builder()
        .id(saved.getId())
        .name(saved.getName())
        .frequency(saved.getFrequency())
        .amount(saved.getAmount())
        .status(saved.getStatus())
        .nextDueDate(saved.getNextDueDate())
        .category(categoryResponse)
        .build();
  }
}
