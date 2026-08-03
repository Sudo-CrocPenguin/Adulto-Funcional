package org.adultofuncional.main.security.application.dto;

import org.adultofuncional.main.shared.security.NoHtml;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Búsqueda y paginación del listado no sensible de credenciales.
 *
 * <p>Permite buscar por aplicación y ordenar exclusivamente por nombre,
 * fecha de cambio o UUID. Usa página 0 y tamaño 20 por defecto, con máximo
 * de 100 elementos conforme al ADR 0005.</p>
 */
@Getter
@Setter
public class PasswordFilterRequest {

  @Size(max = 35, message = "El término de búsqueda no puede exceder 35 caracteres")
  @NoHtml
  private String searchTerm;

  private String sortBy;
  private String sortDirection;
  private Integer page;
  private Integer size;
}
