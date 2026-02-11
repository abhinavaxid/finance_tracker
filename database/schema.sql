-- =========================================
-- SMART EXPENSE TRACKER DATABASE SCHEMA
-- PostgreSQL DDL
-- =========================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =========================================
-- 1. USERS TABLE
-- =========================================
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    email_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    CONSTRAINT email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

-- Index for faster email lookups
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_is_active ON users(is_active);

-- =========================================
-- 2. ROLES TABLE
-- =========================================
CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(20) UNIQUE NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT role_name_check CHECK (name IN ('USER', 'ADMIN'))
);

-- Insert default roles
INSERT INTO roles (name, description) VALUES 
    ('USER', 'Regular user with access to personal finance management'),
    ('ADMIN', 'Administrator with access to system management');

-- =========================================
-- 3. USER_ROLES TABLE (Many-to-Many)
-- =========================================
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id INT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);

-- =========================================
-- 4. CATEGORIES TABLE
-- =========================================
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    type VARCHAR(10) NOT NULL,
    color VARCHAR(7) DEFAULT '#000000',
    icon VARCHAR(50),
    user_id BIGINT,
    is_default BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT category_type_check CHECK (type IN ('INCOME', 'EXPENSE')),
    CONSTRAINT unique_user_category UNIQUE (user_id, name, type)
);

CREATE INDEX idx_categories_user_id ON categories(user_id);
CREATE INDEX idx_categories_type ON categories(type);
CREATE INDEX idx_categories_is_default ON categories(is_default);

-- Insert default categories
INSERT INTO categories (name, type, color, icon, is_default) VALUES 
    -- Income Categories
    ('Salary', 'INCOME', '#10B981', 'attach_money', TRUE),
    ('Business', 'INCOME', '#3B82F6', 'business_center', TRUE),
    ('Investment', 'INCOME', '#8B5CF6', 'trending_up', TRUE),
    ('Freelance', 'INCOME', '#F59E0B', 'work', TRUE),
    ('Other Income', 'INCOME', '#6366F1', 'account_balance_wallet', TRUE),
    
    -- Expense Categories
    ('Food & Dining', 'EXPENSE', '#EF4444', 'restaurant', TRUE),
    ('Transportation', 'EXPENSE', '#F97316', 'directions_car', TRUE),
    ('Shopping', 'EXPENSE', '#EC4899', 'shopping_cart', TRUE),
    ('Entertainment', 'EXPENSE', '#8B5CF6', 'movie', TRUE),
    ('Healthcare', 'EXPENSE', '#06B6D4', 'local_hospital', TRUE),
    ('Utilities', 'EXPENSE', '#14B8A6', 'bolt', TRUE),
    ('Rent', 'EXPENSE', '#F59E0B', 'home', TRUE),
    ('Education', 'EXPENSE', '#3B82F6', 'school', TRUE),
    ('Insurance', 'EXPENSE', '#6366F1', 'security', TRUE),
    ('Personal Care', 'EXPENSE', '#A855F7', 'spa', TRUE),
    ('Other Expense', 'EXPENSE', '#6B7280', 'category', TRUE);

-- =========================================
-- 5. TRANSACTIONS TABLE
-- =========================================
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    type VARCHAR(10) NOT NULL,
    description TEXT,
    transaction_date DATE NOT NULL,
    payment_method VARCHAR(20),
    reference_number VARCHAR(50),
    tags TEXT[],
    is_recurring BOOLEAN DEFAULT FALSE,
    recurring_transaction_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
    CONSTRAINT transaction_type_check CHECK (type IN ('INCOME', 'EXPENSE')),
    CONSTRAINT transaction_amount_check CHECK (amount > 0),
    CONSTRAINT payment_method_check CHECK (payment_method IN ('CASH', 'CREDIT_CARD', 'DEBIT_CARD', 'BANK_TRANSFER', 'UPI', 'WALLET', 'OTHER'))
);

CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE INDEX idx_transactions_category_id ON transactions(category_id);
CREATE INDEX idx_transactions_date ON transactions(transaction_date);
CREATE INDEX idx_transactions_type ON transactions(type);
CREATE INDEX idx_transactions_user_date ON transactions(user_id, transaction_date DESC);

-- =========================================
-- 6. RECURRING_TRANSACTIONS TABLE
-- =========================================
CREATE TABLE recurring_transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    type VARCHAR(10) NOT NULL,
    description TEXT,
    frequency VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    next_occurrence DATE NOT NULL,
    day_of_month INT,
    is_active BOOLEAN DEFAULT TRUE,
    payment_method VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
    CONSTRAINT recurring_type_check CHECK (type IN ('INCOME', 'EXPENSE')),
    CONSTRAINT recurring_amount_check CHECK (amount > 0),
    CONSTRAINT frequency_check CHECK (frequency IN ('DAILY', 'WEEKLY', 'MONTHLY', 'QUARTERLY', 'YEARLY')),
    CONSTRAINT day_of_month_check CHECK (day_of_month IS NULL OR (day_of_month >= 1 AND day_of_month <= 31))
);

CREATE INDEX idx_recurring_transactions_user_id ON recurring_transactions(user_id);
CREATE INDEX idx_recurring_transactions_next_occurrence ON recurring_transactions(next_occurrence);
CREATE INDEX idx_recurring_transactions_is_active ON recurring_transactions(is_active);

-- =========================================
-- 7. BUDGETS TABLE
-- =========================================
CREATE TABLE budgets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    month INT NOT NULL,
    year INT NOT NULL,
    spent_amount DECIMAL(12, 2) DEFAULT 0,
    alert_threshold DECIMAL(5, 2) DEFAULT 80.00,
    alert_sent BOOLEAN DEFAULT FALSE,
    exceeded_alert_sent BOOLEAN DEFAULT FALSE,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
    CONSTRAINT budget_amount_check CHECK (amount > 0),
    CONSTRAINT month_check CHECK (month >= 1 AND month <= 12),
    CONSTRAINT year_check CHECK (year >= 2000 AND year <= 2100),
    CONSTRAINT alert_threshold_check CHECK (alert_threshold >= 0 AND alert_threshold <= 100),
    CONSTRAINT unique_user_category_month UNIQUE (user_id, category_id, month, year)
);

CREATE INDEX idx_budgets_user_id ON budgets(user_id);
CREATE INDEX idx_budgets_category_id ON budgets(category_id);
CREATE INDEX idx_budgets_month_year ON budgets(month, year);
CREATE INDEX idx_budgets_user_month_year ON budgets(user_id, month, year);

-- =========================================
-- 8. BUDGET_ALERTS TABLE
-- =========================================
CREATE TABLE budget_alerts (
    id BIGSERIAL PRIMARY KEY,
    budget_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    alert_type VARCHAR(20) NOT NULL,
    message TEXT NOT NULL,
    percentage_used DECIMAL(5, 2),
    is_read BOOLEAN DEFAULT FALSE,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (budget_id) REFERENCES budgets(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT alert_type_check CHECK (alert_type IN ('THRESHOLD', 'EXCEEDED', 'WARNING'))
);

CREATE INDEX idx_budget_alerts_user_id ON budget_alerts(user_id);
CREATE INDEX idx_budget_alerts_budget_id ON budget_alerts(budget_id);
CREATE INDEX idx_budget_alerts_is_read ON budget_alerts(is_read);

-- =========================================
-- 9. FILES TABLE (Receipts/Invoices)
-- =========================================
CREATE TABLE files (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    transaction_id BIGINT,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_type VARCHAR(50),
    file_size BIGINT,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (transaction_id) REFERENCES transactions(id) ON DELETE CASCADE
);

CREATE INDEX idx_files_user_id ON files(user_id);
CREATE INDEX idx_files_transaction_id ON files(transaction_id);

-- =========================================
-- 10. NOTIFICATIONS TABLE
-- =========================================
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(30) NOT NULL,
    title VARCHAR(100) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    is_email_sent BOOLEAN DEFAULT FALSE,
    priority VARCHAR(10) DEFAULT 'NORMAL',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT notification_type_check CHECK (type IN ('BUDGET_ALERT', 'BUDGET_EXCEEDED', 'MONTHLY_SUMMARY', 'SIGNUP_WELCOME', 'SYSTEM', 'INSIGHT')),
    CONSTRAINT priority_check CHECK (priority IN ('LOW', 'NORMAL', 'HIGH', 'URGENT'))
);

CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);
CREATE INDEX idx_notifications_created_at ON notifications(created_at DESC);

-- =========================================
-- 11. AUDIT_LOG TABLE
-- =========================================
CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    old_value JSONB,
    new_value JSONB,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT action_check CHECK (action IN ('LOGIN', 'LOGOUT', 'CREATE', 'UPDATE', 'DELETE', 'VIEW', 'EXPORT', 'FAILED_LOGIN'))
);

CREATE INDEX idx_audit_log_user_id ON audit_log(user_id);
CREATE INDEX idx_audit_log_action ON audit_log(action);
CREATE INDEX idx_audit_log_entity_type ON audit_log(entity_type);
CREATE INDEX idx_audit_log_created_at ON audit_log(created_at DESC);

-- =========================================
-- 12. INSIGHTS TABLE (Smart Insights)
-- =========================================
CREATE TABLE insights (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    insight_type VARCHAR(30) NOT NULL,
    title VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    severity VARCHAR(10) DEFAULT 'INFO',
    category_id BIGINT,
    amount DECIMAL(12, 2),
    percentage DECIMAL(5, 2),
    start_date DATE,
    end_date DATE,
    is_dismissed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    CONSTRAINT insight_type_check CHECK (insight_type IN ('OVERSPENDING', 'TREND_UP', 'TREND_DOWN', 'LOW_SAVINGS', 'HIGH_SPENDING', 'UNUSUAL_ACTIVITY')),
    CONSTRAINT severity_check CHECK (severity IN ('INFO', 'WARNING', 'CRITICAL'))
);

CREATE INDEX idx_insights_user_id ON insights(user_id);
CREATE INDEX idx_insights_insight_type ON insights(insight_type);
CREATE INDEX idx_insights_is_dismissed ON insights(is_dismissed);

-- =========================================
-- 13. USER_PREFERENCES TABLE
-- =========================================
CREATE TABLE user_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    date_format VARCHAR(20) DEFAULT 'MM/DD/YYYY',
    timezone VARCHAR(50) DEFAULT 'UTC',
    email_notifications BOOLEAN DEFAULT TRUE,
    budget_alerts BOOLEAN DEFAULT TRUE,
    monthly_summary BOOLEAN DEFAULT TRUE,
    theme VARCHAR(10) DEFAULT 'LIGHT',
    language VARCHAR(5) DEFAULT 'en',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT theme_check CHECK (theme IN ('LIGHT', 'DARK'))
);

CREATE INDEX idx_user_preferences_user_id ON user_preferences(user_id);

-- =========================================
-- TRIGGERS FOR UPDATED_AT
-- =========================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_categories_updated_at BEFORE UPDATE ON categories
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_transactions_updated_at BEFORE UPDATE ON transactions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_recurring_transactions_updated_at BEFORE UPDATE ON recurring_transactions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_budgets_updated_at BEFORE UPDATE ON budgets
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_user_preferences_updated_at BEFORE UPDATE ON user_preferences
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =========================================
-- VIEWS FOR ANALYTICS
-- =========================================

-- View for monthly income vs expense
CREATE OR REPLACE VIEW v_monthly_summary AS
SELECT 
    user_id,
    DATE_TRUNC('month', transaction_date) AS month,
    SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END) AS total_income,
    SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END) AS total_expense,
    SUM(CASE WHEN type = 'INCOME' THEN amount ELSE -amount END) AS net_savings
FROM transactions
GROUP BY user_id, DATE_TRUNC('month', transaction_date);

-- View for category-wise spending
CREATE OR REPLACE VIEW v_category_spending AS
SELECT 
    t.user_id,
    c.name AS category_name,
    c.type,
    c.color,
    DATE_TRUNC('month', t.transaction_date) AS month,
    SUM(t.amount) AS total_amount,
    COUNT(*) AS transaction_count
FROM transactions t
JOIN categories c ON t.category_id = c.id
GROUP BY t.user_id, c.name, c.type, c.color, DATE_TRUNC('month', t.transaction_date);

-- View for budget status
CREATE OR REPLACE VIEW v_budget_status AS
SELECT 
    b.id,
    b.user_id,
    c.name AS category_name,
    b.month,
    b.year,
    b.amount AS budget_amount,
    b.spent_amount,
    ROUND((b.spent_amount / b.amount * 100), 2) AS percentage_used,
    (b.amount - b.spent_amount) AS remaining_amount,
    CASE 
        WHEN b.spent_amount >= b.amount THEN 'EXCEEDED'
        WHEN (b.spent_amount / b.amount * 100) >= b.alert_threshold THEN 'WARNING'
        ELSE 'NORMAL'
    END AS status
FROM budgets b
JOIN categories c ON b.category_id = c.id;

-- =========================================
-- FUNCTION TO UPDATE BUDGET SPENT AMOUNT
-- =========================================

CREATE OR REPLACE FUNCTION update_budget_spent_amount()
RETURNS TRIGGER AS $$
BEGIN
    -- Update budget spent amount when transaction is inserted/updated
    IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') AND NEW.type = 'EXPENSE' THEN
        UPDATE budgets
        SET spent_amount = (
            SELECT COALESCE(SUM(amount), 0)
            FROM transactions
            WHERE user_id = NEW.user_id
            AND category_id = NEW.category_id
            AND type = 'EXPENSE'
            AND EXTRACT(MONTH FROM transaction_date) = month
            AND EXTRACT(YEAR FROM transaction_date) = year
        )
        WHERE user_id = NEW.user_id
        AND category_id = NEW.category_id
        AND EXTRACT(MONTH FROM NEW.transaction_date) = month
        AND EXTRACT(YEAR FROM NEW.transaction_date) = year;
    END IF;
    
    -- Update budget spent amount when transaction is deleted
    IF TG_OP = 'DELETE' AND OLD.type = 'EXPENSE' THEN
        UPDATE budgets
        SET spent_amount = (
            SELECT COALESCE(SUM(amount), 0)
            FROM transactions
            WHERE user_id = OLD.user_id
            AND category_id = OLD.category_id
            AND type = 'EXPENSE'
            AND EXTRACT(MONTH FROM transaction_date) = month
            AND EXTRACT(YEAR FROM transaction_date) = year
        )
        WHERE user_id = OLD.user_id
        AND category_id = OLD.category_id
        AND EXTRACT(MONTH FROM OLD.transaction_date) = month
        AND EXTRACT(YEAR FROM OLD.transaction_date) = year;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_budget_spent
AFTER INSERT OR UPDATE OR DELETE ON transactions
FOR EACH ROW EXECUTE FUNCTION update_budget_spent_amount();

-- =========================================
-- COMMENTS
-- =========================================

COMMENT ON TABLE users IS 'Stores user account information';
COMMENT ON TABLE roles IS 'Defines system roles (USER, ADMIN)';
COMMENT ON TABLE user_roles IS 'Maps users to roles (many-to-many)';
COMMENT ON TABLE categories IS 'Income and expense categories (default + user-defined)';
COMMENT ON TABLE transactions IS 'All financial transactions (income/expense)';
COMMENT ON TABLE recurring_transactions IS 'Recurring transaction templates';
COMMENT ON TABLE budgets IS 'Monthly budgets by category';
COMMENT ON TABLE budget_alerts IS 'Budget threshold and exceeded alerts';
COMMENT ON TABLE files IS 'Uploaded receipts and invoices';
COMMENT ON TABLE notifications IS 'User notifications and email queue';
COMMENT ON TABLE audit_log IS 'Audit trail for all user actions';
COMMENT ON TABLE insights IS 'AI-generated spending insights';
COMMENT ON TABLE user_preferences IS 'User-specific settings and preferences';

-- =========================================
-- END OF SCHEMA
-- =========================================
