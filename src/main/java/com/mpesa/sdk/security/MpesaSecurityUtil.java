package com.mpesa.sdk.security;

import lombok.extern.slf4j.Slf4j;
import javax.crypto.Cipher;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

/**
 * Utility for generating M-Pesa SecurityCredential values.
 */
@Slf4j
public class MpesaSecurityUtil {

    /**
     * Generates a SecurityCredential by encrypting the initiator password
     * with the M-Pesa public key certificate.
     *
     * @param password        the plaintext initiator password
     * @param certificatePath path to the X509 certificate (.cer)
     * @return Base64-encoded encrypted password
     */
    public static String generateSecurityCredential(String password, String certificatePath) {
        if (certificatePath == null || certificatePath.isBlank()) {
            return password;
        }

        try (InputStream certStream = Files.newInputStream(Paths.get(certificatePath))) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate) cf.generateCertificate(certStream);

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, certificate.getPublicKey());
            byte[] encrypted = cipher.doFinal(password.getBytes());

            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("Failed to generate M-Pesa SecurityCredential using certificate {}", certificatePath, e);
            throw new RuntimeException("Encryption failed for M-Pesa security credential", e);
        }
    }
}
