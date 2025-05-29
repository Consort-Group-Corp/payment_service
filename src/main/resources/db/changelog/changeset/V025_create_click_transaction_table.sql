CREATE TABLE IF NOT EXISTS payment_schema.click_transactions (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    click_transaction_id BIGINT NOT NULL UNIQUE,
    service_id BIGINT NOT NULL,
    merchant_transaction_id VARCHAR(100) NOT NULL,
    merchant_prepare_id VARCHAR(100),
    amount BIGINT NOT NULL,
    action SMALLINT NOT NULL,
    sign_time TIMESTAMP WITH TIME ZONE NOT NULL,
    sign_string VARCHAR(255) NOT NULL,
    transaction_state VARCHAR(50) NOT NULL,
    perform_time TIMESTAMP WITH TIME ZONE,
    cancel_time TIMESTAMP WITH TIME ZONE,
    cancel_reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);