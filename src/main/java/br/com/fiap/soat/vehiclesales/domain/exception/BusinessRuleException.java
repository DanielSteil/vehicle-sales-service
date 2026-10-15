package br.com.fiap.soat.vehiclesales.domain.exception;

/** Os dados chegaram bem formados, mas violam uma regra de negócio. */
public class BusinessRuleException extends DomainException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
