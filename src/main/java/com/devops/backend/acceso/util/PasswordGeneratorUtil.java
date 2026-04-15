package com.devops.backend.acceso.util;

import java.security.SecureRandom;

public class PasswordGeneratorUtil {

    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL_CHARS = "!@#$%^&*()_+-=[]{}|;:,.<>?";
    private static final String ALL_CHARS = UPPERCASE + LOWERCASE + DIGITS + SPECIAL_CHARS;

    private static final SecureRandom random = new SecureRandom();
    private static final int PASSWORD_LENGTH = 12;

    /**
     * Genera una contraseña aleatoria segura que contiene:
     * - Al menos 1 mayúscula
     * - Al menos 1 minúscula
     * - Al menos 1 dígito
     * - Al menos 1 carácter especial
     * - Longitud total: 12 caracteres
     *
     * @return contraseña aleatoria generada
     */
    public static String generateSecurePassword() {
        StringBuilder password = new StringBuilder();

        // Garantizar al menos un carácter de cada tipo
        password.append(UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));
        password.append(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));
        password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        password.append(SPECIAL_CHARS.charAt(random.nextInt(SPECIAL_CHARS.length())));

        // Completar el resto de la contraseña
        for (int i = 4; i < PASSWORD_LENGTH; i++) {
            password.append(ALL_CHARS.charAt(random.nextInt(ALL_CHARS.length())));
        }

        // Mezclar los caracteres para que no estén en orden previsible
        return shufflePassword(password.toString());
    }

    /**
     * Mezcla los caracteres de la contraseña aleatoriamente
     *
     * @param password la contraseña a mezclar
     * @return contraseña con caracteres mezclados
     */
    private static String shufflePassword(String password) {
        char[] chars = password.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    }
}
