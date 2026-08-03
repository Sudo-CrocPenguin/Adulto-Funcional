package org.adultofuncional.main.security.infrastructure.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.adultofuncional.main.security.domain.service.EncryptionException;
import org.adultofuncional.main.security.domain.service.EncryptionService.EncryptedData;
import org.adultofuncional.main.security.domain.service.EncryptionService.EncryptionContext;
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

  @Test
  void encryptsUnicodeWithCurrentVersionAndStableContext() {
    EncryptionContext context = context("00000000-0000-0000-0000-000000000001");

    EncryptedData data = service.encrypt("contraseña-🔐-漢字", "frase maestra extensa", context);

    assertThat(data.cryptoVersion()).isEqualTo(AesEncryptionService.CURRENT_VERSION);
    assertThat(service.decrypt(
        data.salt(),
        data.iv(),
        data.ciphertext(),
        "frase maestra extensa",
        data.cryptoVersion(),
        context)).isEqualTo("contraseña-🔐-漢字");
  }

  @Test
  void rejectsCiphertextMovedToAnotherCredential() {
    EncryptionContext original = context("00000000-0000-0000-0000-000000000001");
    EncryptionContext substituted = context("00000000-0000-0000-0000-000000000002");
    EncryptedData data = service.encrypt("secreto", "frase maestra extensa", original);

    assertThatThrownBy(() -> service.decrypt(
        data.salt(),
        data.iv(),
        data.ciphertext(),
        "frase maestra extensa",
        data.cryptoVersion(),
        substituted))
        .isInstanceOf(EncryptionException.class)
        .extracting("reason")
        .isEqualTo(EncryptionException.Reason.AUTHENTICATION_FAILED);
  }

  @Test
  void rejectsTamperedCiphertext() {
    EncryptionContext context = context("00000000-0000-0000-0000-000000000001");
    EncryptedData data = service.encrypt("secreto", "frase maestra extensa", context);
    byte[] tampered = data.ciphertext().clone();
    tampered[0] ^= 1;

    assertThatThrownBy(() -> service.decrypt(
        data.salt(),
        data.iv(),
        tampered,
        "frase maestra extensa",
        data.cryptoVersion(),
        context))
        .isInstanceOf(EncryptionException.class)
        .extracting("reason")
        .isEqualTo(EncryptionException.Reason.AUTHENTICATION_FAILED);
  }

  private EncryptionContext context(String credentialId) {
    return new EncryptionContext(
        UUID.fromString("10000000-0000-0000-0000-000000000001"),
        UUID.fromString(credentialId));
  }
}
