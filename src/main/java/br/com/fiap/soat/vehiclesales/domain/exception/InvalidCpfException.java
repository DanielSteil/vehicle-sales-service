package br.com.fiap.soat.vehiclesales.domain.exception;

public class InvalidCpfException extends BusinessRuleException {

    public InvalidCpfException() {
        super("CPF inválido");
    }
}
