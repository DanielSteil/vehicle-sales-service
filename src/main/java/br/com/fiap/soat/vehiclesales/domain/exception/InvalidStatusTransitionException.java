package br.com.fiap.soat.vehiclesales.domain.exception;

public class InvalidStatusTransitionException extends ConflictException {

    public InvalidStatusTransitionException(String message) {
        super(message);
    }
}
