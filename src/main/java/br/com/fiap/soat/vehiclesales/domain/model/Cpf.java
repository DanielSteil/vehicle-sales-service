package br.com.fiap.soat.vehiclesales.domain.model;

import br.com.fiap.soat.vehiclesales.domain.exception.InvalidCpfException;

/** CPF validado pelos dígitos verificadores e armazenado só com os 11 números. */
public record Cpf(String value) {

    public Cpf {
        if (value == null) {
            throw new InvalidCpfException();
        }
        value = value.replaceAll("\\D", "");
        if (!isValid(value)) {
            throw new InvalidCpfException();
        }
    }

    /** Formato exibido em listagens públicas, para não expor o documento inteiro (LGPD). */
    public String masked() {
        return "***." + value.substring(3, 6) + "." + value.substring(6, 9) + "-**";
    }

    private static boolean isValid(String digits) {
        if (digits.length() != 11 || digits.chars().distinct().count() == 1) {
            return false;
        }
        return digitAt(digits, 9) == checkDigit(digits, 9)
                && digitAt(digits, 10) == checkDigit(digits, 10);
    }

    private static int checkDigit(String digits, int length) {
        int sum = 0;
        for (int i = 0; i < length; i++) {
            sum += digitAt(digits, i) * (length + 1 - i);
        }
        int remainder = (sum * 10) % 11;
        return remainder == 10 ? 0 : remainder;
    }

    private static int digitAt(String digits, int index) {
        return digits.charAt(index) - '0';
    }
}
