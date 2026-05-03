package org.adultofuncional.main.config.security;

import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DatabaseUserDetailsService implements UserDetailsService {

  private final AccountRepository accountRepository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    var account = accountRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException(
            "Usuario no encontrado: " + email));

    String[] roles = account.getRoles().toArray(String[]::new);

    return User.builder()
        .username(account.getEmail())
        .password(account.getPasswordHash())
        .roles(roles.length > 0 ? roles : new String[] { "USER" })
        .build();
  }
}
