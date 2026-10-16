CREATE TABLE vehicles (
    id           UUID PRIMARY KEY,
    brand        VARCHAR(60)    NOT NULL,
    model        VARCHAR(60)    NOT NULL,
    vehicle_year INTEGER        NOT NULL,
    color        VARCHAR(30)    NOT NULL,
    price        NUMERIC(12, 2) NOT NULL CHECK (price > 0),
    status       VARCHAR(20)    NOT NULL CHECK (status IN ('AVAILABLE', 'RESERVED', 'SOLD')),
    version      BIGINT         NOT NULL,
    created_at   TIMESTAMPTZ    NOT NULL,
    updated_at   TIMESTAMPTZ    NOT NULL
);

-- atende a listagem de veículos à venda, ordenada por preço
CREATE INDEX idx_vehicles_status_price ON vehicles (status, price, id);

CREATE TABLE sales (
    id             UUID PRIMARY KEY,
    vehicle_id     UUID           NOT NULL REFERENCES vehicles (id),
    buyer_cpf      VARCHAR(11)    NOT NULL,
    sale_date      DATE           NOT NULL,
    price          NUMERIC(12, 2) NOT NULL,
    payment_code   UUID           NOT NULL UNIQUE,
    payment_status VARCHAR(20)    NOT NULL CHECK (payment_status IN ('PENDING', 'PAID', 'CANCELED')),
    version        BIGINT         NOT NULL,
    created_at     TIMESTAMPTZ    NOT NULL,
    updated_at     TIMESTAMPTZ    NOT NULL
);

-- atende a listagem de veículos vendidos, ordenada por preço
CREATE INDEX idx_sales_payment_status_price ON sales (payment_status, price, id);

-- garantia extra no banco: um veículo nunca tem duas vendas ativas ao mesmo tempo
CREATE UNIQUE INDEX ux_sales_active_vehicle ON sales (vehicle_id) WHERE payment_status IN ('PENDING', 'PAID');
