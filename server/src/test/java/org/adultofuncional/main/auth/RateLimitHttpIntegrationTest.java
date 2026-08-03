package org.adultofuncional.main.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.adultofuncional.main.testsupport.MariaDbIntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class RateLimitHttpIntegrationTest extends MariaDbIntegrationTestSupport {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void throttlesRepeatedLoginAttemptsWithRetryAfter() throws Exception {
    String body = """
        {
          "email": "rate-limit-account@example.com",
          "password": "password-seguro"
        }
        """;

    for (int attempt = 0; attempt < 5; attempt++) {
      mockMvc.perform(post("/api/auth/login")
              .with(request -> {
                request.setRemoteAddr("192.0.2.80");
                return request;
              })
              .contentType(MediaType.APPLICATION_JSON)
              .content(body))
          .andExpect(status().isUnauthorized());
    }

    mockMvc.perform(post("/api/auth/login")
            .with(request -> {
              request.setRemoteAddr("192.0.2.80");
              return request;
            })
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isTooManyRequests())
        .andExpect(header().string(HttpHeaders.RETRY_AFTER, "5"))
        .andExpect(jsonPath("$.code").value("RATE_LIMIT_EXCEEDED"));
  }
}
