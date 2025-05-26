CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS payment_schema.orders(
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    external_order_id VARCHAR(255) NOT NULL,
    amount BIGINT NOT NULL,
    source VARCHAR(20) NOT NULL,
    order_status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    UNIQUE (external_order_id, source)
);