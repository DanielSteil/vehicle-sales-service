package br.com.fiap.soat.vehiclesales.application.usecase.fakes;

import br.com.fiap.soat.vehiclesales.application.port.UnitOfWork;

import java.util.function.Supplier;

public class DirectUnitOfWork implements UnitOfWork {

    @Override
    public <T> T execute(Supplier<T> work) {
        return work.get();
    }
}
