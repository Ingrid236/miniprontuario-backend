package com.miniprontuario.miniprontuario_backend.util;

/**
 * Validates Brazilian CPF numbers using the official digit-verification algorithm.
 */
public final class CpfValidator {

    private CpfValidator() {}

    /**
     * Returns {@code true} if the given CPF string is valid.
     * Accepts CPF with or without formatting (digits only or ###.###.###-##).
     */
    public static boolean isValid(String cpf) {
        if (cpf == null) return false;

        // Strip non-numeric characters
        String digits = cpf.replaceAll("[^0-9]", "");

        if (digits.length() != 11) return false;

        // Reject all-same-digit CPFs (e.g., 00000000000, 11111111111)
        if (digits.chars().distinct().count() == 1) return false;

        // Validate first check digit
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += Character.getNumericValue(digits.charAt(i)) * (10 - i);
        }
        int firstDigit = (sum * 10) % 11;
        if (firstDigit == 10 || firstDigit == 11) firstDigit = 0;
        if (firstDigit != Character.getNumericValue(digits.charAt(9))) return false;

        // Validate second check digit
        sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += Character.getNumericValue(digits.charAt(i)) * (11 - i);
        }
        int secondDigit = (sum * 10) % 11;
        if (secondDigit == 10 || secondDigit == 11) secondDigit = 0;
        return secondDigit == Character.getNumericValue(digits.charAt(10));
    }
}
