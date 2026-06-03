package com.alianca.nforaprazo.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utilitario para gerar hashes BCrypt.
 * Execute esta classe para gerar hashes para uso em migrations Flyway.
 *
 * Uso via Maven:
 *   mvn compile exec:java -Dexec.mainClass="com.alianca.nforaprazo.util.PasswordHashGenerator"
 *
 * Ou execute diretamente na IDE.
 */
public class PasswordHashGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String senha = args.length > 0 ? args[0] : "Test@123";
        String hash = encoder.encode(senha);

        System.out.println("========================================");
        System.out.println("Senha:        " + senha);
        System.out.println("Hash BCrypt:  " + hash);
        System.out.println("Verificacao:  " + encoder.matches(senha, hash));
        System.out.println("========================================");
        System.out.println();
        System.out.println("Use este hash no SQL:");
        System.out.println("'" + hash + "'");
    }
}
