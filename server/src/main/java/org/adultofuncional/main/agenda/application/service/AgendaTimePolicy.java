package org.adultofuncional.main.agenda.application.service;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneId;

import org.adultofuncional.main.shared.exception.BusinessException;
import org.adultofuncional.main.shared.response.ApiErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Política temporal común de la agenda.
 *
 * <p>Resuelve zonas IANA, aplica la zona de compatibilidad a clientes que aún
 * no la envían y compara fechas de negocio mediante el {@link Clock} inyectado
 * en lugar de depender del reloj o zona predeterminados de la JVM.</p>
 */
@Component
public class AgendaTimePolicy {

  private final Clock clock;
  private final ZoneId defaultZone;

  public AgendaTimePolicy(
      Clock clock,
      @Value("${app.time.default-zone:America/Bogota}") String defaultZone) {
    this.clock = clock;
    this.defaultZone = parseZone(defaultZone);
  }

  /** Resuelve una zona solicitada o devuelve la zona configurada. */
  public ZoneId resolve(String requestedZone) {
    return requestedZone == null || requestedZone.isBlank()
        ? defaultZone
        : parseZone(requestedZone);
  }

  /** Rechaza fechas nuevas anteriores al día actual de la zona del evento. */
  public void requirePresentOrFuture(LocalDate date, ZoneId zoneId) {
    if (date.isBefore(LocalDate.now(clock.withZone(zoneId)))) {
      throw new BusinessException(
          "La fecha del evento no puede ser pasada",
          400,
          ApiErrorCode.PARAMETER_INVALID);
    }
  }

  private ZoneId parseZone(String zone) {
    try {
      return ZoneId.of(zone);
    } catch (DateTimeException exception) {
      throw new BusinessException(
          "La zona horaria debe ser un identificador IANA válido",
          400,
          ApiErrorCode.PARAMETER_INVALID);
    }
  }
}
