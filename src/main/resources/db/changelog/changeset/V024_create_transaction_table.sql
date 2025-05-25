CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS payment_schema.transactions(
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    paycom_transaction_id VARCHAR(50) NOT NULL UNIQUE,
    order_id VARCHAR(50) NOT NULL,
    amount BIGINT NOT NULL,
    transaction_state VARCHAR(100) NOT NULL,
    create_time TIMESTAMP WITH TIME ZONE NOT NULL,
    perform_time TIMESTAMP WITH TIME ZONE,
    cancel_time TIMESTAMP WITH TIME ZONE,
    reason INTEGER
)