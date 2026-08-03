package org.adultofuncional.main.config.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

class CookieUtilsTest {

  @Test
  void rejectsSameSiteNoneWithoutSecureCookie() {
    assertThatThrownBy(() -> new CookieUtils(false, "None"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("requiere APP_COOKIE_SECURE=true");
  }

  @Test
  void rejectsUnknownSameSitePolicy() {
    assertThatThrownBy(() -> new CookieUtils(true, "permissive"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Strict, Lax o None");
  }

  @Test
  void normalizesAndAppliesSecureCookiePolicy() {
    CookieUtils cookies = new CookieUtils(true, "strict");
    MockHttpServletResponse response = new MockHttpServletResponse();

    cookies.addTokenCookie(response, "signed-token", 60_000);

    assertThat(cookies.sameSite()).isEqualTo("Strict");
    assertThat(response.getHeader("Set-Cookie"))
        .contains("HttpOnly", "Secure", "SameSite=Strict", "Max-Age=60");
  }
}
