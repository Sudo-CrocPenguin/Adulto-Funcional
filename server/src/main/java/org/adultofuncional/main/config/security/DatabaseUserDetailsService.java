package org.adultofuncional.main.config.security;

import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * Implementación de {@link UserDetailsService} que carga los datos del usuario
 * desde la base de datos para su uso en el proceso de autenticación de Spring
 * Security.
 *
 * <p>
 * Se utiliza durante el login para verificar que el usuario existe y obtener
 * su contraseña hasheada (Argon2), que luego Spring Security compara con la
 * contraseña proporcionada usando el {@code PasswordEncoder} configurado en
 * {@link org.adultofuncional.main.config.beans.AppConfig}.
 *
 * <p>
 * Este servicio solo se invoca durante el proceso de
 * login.
 * Las requests autenticadas posteriores no consultan la base de datos — el
 * usuario
 * se reconstruye directamente desde los claims del JWT en
 * {@link JwtAuthenticationFilter}.
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 * @see JwtAuthenticationFilter
 * @see org.adultofuncional.main.config.beans.AppConfig
 */
@Service
@RequiredArgsConstructor
public class DatabaseUserDetailsService implements UserDetailsService {

  /** Repositorio de cuentas para buscar el usuario por email. */
  private final AccountRepository accountRepository;

  /**
   * Carga los detalles del usuario a partir de su email.
   *
   * <p>
   * Busca la cuenta en la base de datos por email y construye un objeto
   * {@link UserDetails} con el email como username, el hash Argon2 como
   * password, y el rol {@code USER} por defecto.
   *
   * @param email correo electrónico del usuario (usado como username)
   * @return {@link UserDetails} con las credenciales y autoridades del usuario
   * @throws UsernameNotFoundException si no existe una cuenta con el email dado
   */
  // TODO: Cargar roles dinámicamente desde la base de datos cuando se implemente
  // el sistema de roles en la entidad Account.
  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    var account = accountRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException(
            "Usuario no encontrado: " + email));

    return User.builder()
        .username(account.getEmail())
        .password(account.getPasswordHash())
        .roles("USER")
        .build();
  }
}
