-- data.sql
-- Initial data setup for Akeshya Jewellery Management System

-- Clear existing data (optional - only if you want fresh start)
-- DELETE FROM user_roles;
-- DELETE FROM users;
-- DELETE FROM roles;

-- Insert default roles if they don't exist
INSERT INTO roles (name) VALUES 
    ('ROLE_USER'),
    ('ROLE_ADMIN'),
    ('ROLE_MANAGER')
ON CONFLICT (name) DO NOTHING;

-- Insert default admin user if doesn't exist
INSERT INTO users (username, email, password, first_name, last_name, enabled, created_at, updated_at) 
VALUES (
    'admin',
    'admin@sreesouthindiajewellers.com',
    '$2a$10$8.UnVuG9HHgffUDAlk8qfOuWk6K7.a1uCJ7aC.6bWYVK6x2YzOoOa', -- bcrypt encoded 'admin123'
    'System',
    'Administrator',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (username) DO NOTHING;

-- Insert sample manager user if doesn't exist
INSERT INTO users (username, email, password, first_name, last_name, enabled, created_at, updated_at) 
VALUES (
    'manager',
    'manager@sreesouthindiajewellers.com',
    '$2a$10$8.UnVuG9HHgffUDAlk8qfOuWk6K7.a1uCJ7aC.6bWYVK6x2YzOoOa', -- bcrypt encoded 'admin123'
    'Store',
    'Manager',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (username) DO NOTHING;

-- Insert sample staff user if doesn't exist
INSERT INTO users (username, email, password, first_name, last_name, enabled, created_at, updated_at) 
VALUES (
    'staff',
    'staff@sreesouthindiajewellers.com',
    '$2a$10$8.UnVuG9HHgffUDAlk8qfOuWk6K7.a1uCJ7aC.6bWYVK6x2YzOoOa', -- bcrypt encoded 'admin123'
    'Sales',
    'Staff',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (username) DO NOTHING;

-- Assign roles to admin user (ROLE_ADMIN, ROLE_USER, ROLE_MANAGER)
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.username = 'admin' AND r.name IN ('ROLE_ADMIN', 'ROLE_USER', 'ROLE_MANAGER')
ON CONFLICT (user_id, role_id) DO NOTHING;

-- Assign roles to manager user (ROLE_MANAGER, ROLE_USER)
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.username = 'manager' AND r.name IN ('ROLE_MANAGER', 'ROLE_USER')
ON CONFLICT (user_id, role_id) DO NOTHING;

-- Assign roles to staff user (ROLE_USER only)
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.username = 'staff' AND r.name = 'ROLE_USER'
ON CONFLICT (user_id, role_id) DO NOTHING;

-- Insert sample customers for testing (optional)
INSERT INTO users (username, email, password, first_name, last_name, enabled, created_at, updated_at) 
VALUES 
    ('customer1', 'customer1@example.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuWk6K7.a1uCJ7aC.6bWYVK6x2YzOoOa', 'Raj', 'Kumar', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('customer2', 'customer2@example.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuWk6K7.a1uCJ7aC.6bWYVK6x2YzOoOa', 'Priya', 'Sharma', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('customer3', 'customer3@example.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuWk6K7.a1uCJ7aC.6bWYVK6x2YzOoOa', 'Arun', 'Patel', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;

-- Assign ROLE_USER to all customer accounts
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.username LIKE 'customer%' AND r.name = 'ROLE_USER'
ON CONFLICT (user_id, role_id) DO NOTHING;

-- Verification query to check inserted data (this won't affect the data)
-- SELECT 
--     u.username,
--     u.email,
--     u.first_name,
--     u.last_name,
--     u.enabled,
--     STRING_AGG(r.name, ', ') as roles
-- FROM users u
-- LEFT JOIN user_roles ur ON u.id = ur.user_id
-- LEFT JOIN roles r ON ur.role_id = r.id
-- GROUP BY u.id, u.username, u.email, u.first_name, u.last_name, u.enabled
-- ORDER BY u.username;