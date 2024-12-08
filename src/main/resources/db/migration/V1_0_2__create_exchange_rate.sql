CREATE TABLE exchange_rate
(
    id                 BIGSERIAL PRIMARY KEY,
    base_currency_id   BIGINT         NOT NULL REFERENCES currency (id) ON DELETE CASCADE,
    target_currency_id BIGINT         NOT NULL REFERENCES currency (id) ON DELETE CASCADE,
    rate               DECIMAL(10, 6) NOT NULL CHECK (rate > 0),

    UNIQUE (base_currency_id, target_currency_id),
    CHECK (base_currency_id <> target_currency_id)
);