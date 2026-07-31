package org.adultofuncional.main.security.infrastructure.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.adultofuncional.main.security.domain.service.EncryptionService.EncryptedData;
import org.junit.jupiter.api.Test;

class AesEncryptionServiceTest {

  private final AesEncryptionService service = new AesEncryptionService();

  @Test
  void encryptGeneratesTwelveByteIvAndDecryptsOriginalPassword() {
    EncryptedData encryptedData = service.encrypt("clave-super-secreta", "master-key-de-prueba");

    assertThat(encryptedData.iv()).hasSize(12);
    assertThat(encryptedData.ciphertext()).isNotEmpty();
    assertThat(service.decrypt(
        encryptedData.salt(),
        encryptedData.iv(),
        encryptedData.ciphertext(),
        "master-key-de-prueba"))
        .isEqualTo("clave-super-secreta");
  }
}
