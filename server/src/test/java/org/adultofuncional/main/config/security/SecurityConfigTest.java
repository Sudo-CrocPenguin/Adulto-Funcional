package org.adultofuncional.main.config.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;

class SecurityConfigTest {

  @Test
  void corsConfigurationNormalizesTrailingSlashOrigins() {
    SecurityConfig securityConfig = new SecurityConfig(null);
    ReflectionTestUtils.setField(securityConfig, "allowedOrigins",
        List.of(" http://localhost:5173/ "));

    CorsConfiguration corsConfiguration = securityConfig
        .corsConfigurationSource()
        .getCorsConfiguration(new MockHttpServletRequest("GET", "/api/finances/categories"));

    assertThat(corsConfiguration).isNotNull();
    assertThat(corsConfiguration.getAllowedOrigins())
        .containsExactly("http://localhost:5173");
  }
}
