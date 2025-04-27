CREATE TABLE IF NOT EXISTS calculations (
    id UUID PRIMARY KEY,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    user_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS budget_opening (
    id UUID PRIMARY KEY,
    year_month INTEGER NOT NULL, -- Format YYYYMM
    account VARCHAR(50) NOT NULL,
    cost_center VARCHAR(50) NOT NULL,
    business_unit VARCHAR(50) NOT NULL,
    planned_value DECIMAL(28, 16) NOT NULL,
    projected_value DECIMAL(28, 16) NOT NULL,
    apportionment_value DECIMAL(28, 16) NOT NULL
);