package br.unitins.tp2.service;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SenhaService {
    private static final int ITERACOES = 600_000;
    private static final SecureRandom RANDOM = new SecureRandom();

    public String gerarHash(String senha) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return "pbkdf2-sha256$" + ITERACOES + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(calcular(senha, salt));
    }

    public boolean verificar(String senha, String hash) {
        if (senha == null || hash == null) return false;
        String[] partes = hash.split("\\$", -1);
        if (partes.length != 4 || !"pbkdf2-sha256".equals(partes[0])
                || !Integer.toString(ITERACOES).equals(partes[1])) return false;
        try {
            byte[] salt = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            return salt.length == 16 && esperado.length == 32
                    && MessageDigest.isEqual(esperado, calcular(senha, salt));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private byte[] calcular(String senha, byte[] salt) {
        char[] caracteres = senha.toCharArray();
        PBEKeySpec spec = new PBEKeySpec(caracteres, salt, ITERACOES, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Nao foi possivel proteger a senha.", e);
        } finally {
            spec.clearPassword();
            Arrays.fill(caracteres, '\0');
        }
    }
}
