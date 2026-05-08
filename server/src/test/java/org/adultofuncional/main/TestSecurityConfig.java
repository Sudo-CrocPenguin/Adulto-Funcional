// package org.adultofuncional.main;
//
// import org.springframework.boot.test.context.TestConfiguration;
// import org.springframework.context.annotation.Bean;
// import
// org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.core.userdetails.User;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.core.userdetails.UserDetailsService;
// import org.springframework.security.provisioning.InMemoryUserDetailsManager;
// import org.springframework.security.web.SecurityFilterChain;
//
// /**
// * Configuración de seguridad para entornos de prueba.
// *
// * <p>
// * Deshabilita CSRF y permite todas las solicitudes sin autenticación,
// * lo que facilita las pruebas de integración de los controladores REST
// * sin necesidad de proporcionar tokens JWT u otras credenciales.
// * </p>
// *
// * <p>
// * Se usa junto con {@code @Import(TestSecurityConfig.class)} en las clases de
// * test.
// * </p>
// *
// * @author Equipo de desarrollo Adulto Funcional
// * @since 0.0.1
// */
// @TestConfiguration
// public class TestSecurityConfig {
//
// /**
// * Configura la cadena de filtros de seguridad para tests.
// *
// * <p>
// * Deshabilita CSRF y permite cualquier solicitud sin autenticación.
// * No incluye {@code @EnableWebSecurity} porque {@code @WebMvcTest}
// * ya auto-configura Spring Security.
// * </p>
// *
// * @param http instancia de {@link HttpSecurity} a configurar
// * @return cadena de filtros configurada
// * @throws Exception si ocurre un error al configurar
// */
// @Bean
// public SecurityFilterChain securityFilterChain(HttpSecurity http) throws
// Exception {
// return http
// .csrf(csrf -> csrf.disable())
// .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
// .build();
// }
//
// /**
// * Provee un UserDetailsService in-memory para que {@code
// @AuthenticationPrincipal}
// * pueda resolver el usuario autenticado en los tests del controlador.
// * El email coincide con el formato esperado por {@code
// AccountController.validateOwnership}.
// */
// @Bean
// public UserDetailsService userDetailsService() {
// UserDetails user = User.builder()
// .username("test@example.com")
// .password("password")
// .roles("USER")
// .build();
// return new InMemoryUserDetailsManager(user);
// }
// }
