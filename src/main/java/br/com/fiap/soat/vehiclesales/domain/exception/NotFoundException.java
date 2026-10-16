package br.com.fiap.soat.vehiclesales.domain.exception;

/** O recurso solicitado não existe. */
public abstract class NotFoundException extends DomainException {

    protected NotFoundException(String message) {
        super(message);
    }
}
