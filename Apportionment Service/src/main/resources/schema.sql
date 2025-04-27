CREATE TABLE IF NOT EXISTS apportionments (
    id UUID PRIMARY KEY,
    account VARCHAR(255) NOT NULL,
    cost_center VARCHAR(255) NOT NULL,
    business_unit VARCHAR(255) NOT NULL,
    year INT NOT NULL,
    origin_id UUID,
    is_origin BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS value_distributions (
    id UUID PRIMARY KEY,
    apportionment_id UUID NOT NULL,
    month INT NOT NULL,
    percentage NUMERIC(19, 16) NOT NULL,
    destination_id UUID NOT NULL,
    CONSTRAINT fk_value_distributions_apportionment
        FOREIGN KEY (apportionment_id)
        REFERENCES apportionments(id)
        ON DELETE CASCADE
);