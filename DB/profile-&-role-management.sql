-- Create User table
CREATE TABLE users (
	id INT IDENTITY(1,1) PRIMARY KEY,
	first_name VARCHAR(50) NOT NULL,
	last_name VARCHAR(50) NOT NULL,
	email VARCHAR(100) NOT NULL UNIQUE,
	password VARCHAR(255) NOT NULL,
	role VARCHAR(20) NOT NULL 
		CHECK (role IN ('ADMIN', 'STAFF', 'CUSTOMER')) DEFAULT 'CUSTOMER',
	address TEXT,
	telephone VARCHAR(20),
	status VARCHAR(20) 
		CHECK (status IN ('ACTIVE', 'INACTIVE')) DEFAULT 'ACTIVE',
	reset_token VARCHAR(255) NULL,
	reset_token_expiry DATETIME NULL,
	created_at DATETIME DEFAULT GETDATE(),
	updated_at DATETIME DEFAULT GETDATE()
);

-- Add trust score columns to existing users table
ALTER TABLE users ADD trust_score INT NOT NULL DEFAULT 75;
ALTER TABLE users ADD trust_level VARCHAR(20) NOT NULL DEFAULT 'NORMAL';
ALTER TABLE users ADD total_orders INT NOT NULL DEFAULT 0;
ALTER TABLE users ADD total_spending DECIMAL(10,2) NOT NULL DEFAULT 0.00;
ALTER TABLE users ADD completed_tasks INT NOT NULL DEFAULT 0;
ALTER TABLE users ADD performance_rating DECIMAL(3,1) NOT NULL DEFAULT 0.0;
ALTER TABLE users ADD complaints_count INT NOT NULL DEFAULT 0;
ALTER TABLE users ADD cancellations INT NOT NULL DEFAULT 0;
ALTER TABLE users ADD is_restricted BIT NOT NULL DEFAULT 0;

-- Create role history table (tracks every role change)
CREATE TABLE role_history (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL,  
    old_role VARCHAR(30) NOT NULL,
    new_role VARCHAR(30) NOT NULL,
    changed_by VARCHAR(20) NOT NULL DEFAULT 'SYSTEM',
    reason VARCHAR(MAX),
    trust_score_at INT,
    created_at DATETIME DEFAULT GETDATE(),

    CONSTRAINT fk_role_history_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
);

-- Create trust score log table 
CREATE TABLE trust_score_log (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL,     
    old_score INT NOT NULL,
    new_score INT NOT NULL,
    change_by INT NOT NULL,    
    reason VARCHAR(200) NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    created_at DATETIME DEFAULT GETDATE(),

    CONSTRAINT fk_trust_log_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
);

-- Update existing users with default trust scores
UPDATE users SET trust_score = 75, trust_level = 'NORMAL' WHERE trust_score = 0;
UPDATE users SET trust_score = 90, trust_level = 'HIGH'   WHERE role = 'ADMIN';