update schema.sql file
-- ================= ROLES TABLE =================
DROP TABLE IF EXISTS roles CASCADE;
CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

-- ================= USERS TABLE =================
DROP TABLE IF EXISTS users CASCADE;
CREATE TABLE users (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    contact_number VARCHAR(10) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    branch_name VARCHAR(255) NOT NULL,
    gst_number VARCHAR(15),
    shipping_address TEXT NOT NULL,
    contact_person_name VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    enabled BOOLEAN DEFAULT TRUE,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ================= USER ADDITIONAL NUMBERS TABLE =================
DROP TABLE IF EXISTS user_additional_numbers;
CREATE TABLE user_additional_numbers (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    phone_number VARCHAR(15) NOT NULL,
    PRIMARY KEY (user_id, phone_number)
);

-- ================= USER_ROLES TABLE =================
DROP TABLE IF EXISTS user_roles;
CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id INT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- ================= PRODUCT TABLE =================
DROP TABLE IF EXISTS product CASCADE;
CREATE TABLE product (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category_name VARCHAR(255) NOT NULL,
    status VARCHAR(50) DEFAULT 'Full',
    is_published BOOLEAN DEFAULT false,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ================= PRODUCT_IMAGE TABLE =================
DROP TABLE IF EXISTS product_image CASCADE;
CREATE TABLE product_image (
    id SERIAL PRIMARY KEY,
    product_id INT NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    image_path VARCHAR(500) NOT NULL,
    image_order INTEGER DEFAULT 0,
    is_primary BOOLEAN DEFAULT false,
    alt_text VARCHAR(255)
);

-- ================= PRODUCT_SIZE TABLE =================
DROP TABLE IF EXISTS product_size CASCADE;
CREATE TABLE product_size (
    id SERIAL PRIMARY KEY,
    product_id INT NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    size_value DOUBLE PRECISION,
    weight VARCHAR(100)
);

-- ================= PRODUCT_COLORS TABLE =================
DROP TABLE IF EXISTS product_colors CASCADE;
CREATE TABLE product_colors (
    product_id INT NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    color VARCHAR(100) NOT NULL,
    PRIMARY KEY (product_id, color)
);

-- ================= ORDERS TABLE =================
DROP TABLE IF EXISTS orders CASCADE;
CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    order_number VARCHAR(50) UNIQUE NOT NULL,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    total_amount DOUBLE PRECISION NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    shipping_address TEXT NOT NULL,
    contact_person_name VARCHAR(255),
    contact_number VARCHAR(15) NOT NULL,
    additional_contact_numbers VARCHAR(500),
    special_instructions TEXT,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ================= ORDER_ITEMS TABLE =================
DROP TABLE IF EXISTS order_items CASCADE;
CREATE TABLE order_items (
    id SERIAL PRIMARY KEY,
    order_id INT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id INT NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price DOUBLE PRECISION NOT NULL CHECK (unit_price >= 0),
    item_total DOUBLE PRECISION NOT NULL CHECK (item_total >= 0),
    selected_size VARCHAR(100),
    selected_color VARCHAR(100)
);

-- ================= ORDER_TRACKING TABLE =================
DROP TABLE IF EXISTS order_tracking CASCADE;
CREATE TABLE order_tracking (
    id SERIAL PRIMARY KEY,
    order_id INT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL,
    notes TEXT,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ================= INDEXES FOR PERFORMANCE =================

-- Users table indexes
CREATE INDEX idx_users_contact_number ON users(contact_number);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_created_date ON users(created_date);

-- User roles indexes
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);

-- Product table indexes
CREATE INDEX idx_product_category ON product(category_name);
CREATE INDEX idx_product_status ON product(status, is_published);
CREATE INDEX idx_product_published ON product(is_published);
CREATE INDEX idx_product_created_date ON product(created_date);

-- Product image indexes
CREATE INDEX idx_product_image_product_id ON product_image(product_id);
CREATE INDEX idx_product_image_order ON product_image(product_id, image_order);
CREATE INDEX idx_product_image_primary ON product_image(is_primary);

-- Product size indexes
CREATE INDEX idx_product_size_product_id ON product_size(product_id);
CREATE INDEX idx_product_size_value ON product_size(size_value);

-- Product colors indexes
CREATE INDEX idx_product_colors_product_id ON product_colors(product_id);

-- Orders table indexes
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_order_number ON orders(order_number);
CREATE INDEX idx_orders_created_date ON orders(created_date);
CREATE INDEX idx_orders_user_status ON orders(user_id, status);

-- Order items indexes
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);
CREATE INDEX idx_order_items_order_product ON order_items(order_id, product_id);

-- Order tracking indexes
CREATE INDEX idx_order_tracking_order_id ON order_tracking(order_id);
CREATE INDEX idx_order_tracking_status ON order_tracking(status);
CREATE INDEX idx_order_tracking_created_date ON order_tracking(created_date);

-- ================= SAMPLE DATA =================

-- Insert default roles
INSERT INTO roles (name) VALUES 
('ROLE_ADMIN'),
('ROLE_USER'),
('ROLE_VENDOR');

-- Insert sample user (password should be properly encrypted in real application)
INSERT INTO users (contact_number, password, company_name, branch_name, shipping_address, status, enabled) 
VALUES 
('9876543210', '$2a$10$encryptedPasswordExample', 'Akeshya Corp', 'Main Branch', '123 Main Street, City, State', 'APPROVED', true);

-- Insert sample products


-- Insert sample product images
INSERT INTO product_image (product_id, image_path, image_order, is_primary, alt_text)
VALUES 
(1, '/images/products/1/phone-front.jpg', 1, true, 'Smartphone X Front View'),
(1, '/images/products/1/phone-back.jpg', 2, false, 'Smartphone X Back View'),
(2, '/images/products/2/tshirt-front.jpg', 1, true, 'Men''s T-Shirt Front');

-- Insert sample product sizes
INSERT INTO product_size (product_id, size_value, weight)
VALUES 
(1, 6.1, '180g'),
(1, 6.7, '200g'),
(2, 38.0, '150g'),
(2, 40.0, '160g');

-- Insert sample colors
INSERT INTO product_colors (product_id, color)
VALUES 
(1, 'Black'),
(1, 'Blue'),
(1, 'Silver'),
(2, 'White'),
(2, 'Black'),
(2, 'Navy Blue');

-- Assign roles to sample user
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.contact_number = '9876543210' 
AND r.name IN ('ROLE_ADMIN', 'ROLE_VENDOR');

-- Insert sample additional phone numbers
INSERT INTO user_additional_numbers (user_id, phone_number)
SELECT id, '9876543211' FROM users WHERE contact_number = '9876543210'
UNION ALL
SELECT id, '9876543212' FROM users WHERE contact_number = '9876543210';

-- Insert sample orders
INSERT INTO orders (order_number, user_id, total_amount, status, shipping_address, contact_person_name, contact_number, special_instructions)
SELECT 
    'ORD-001', 
    id, 
    29999.00, 
    'CONFIRMED', 
    shipping_address, 
    'John Doe', 
    contact_number, 
    'Please deliver between 10 AM to 5 PM'
FROM users WHERE contact_number = '9876543210';

INSERT INTO orders (order_number, user_id, total_amount, status, shipping_address, contact_person_name, contact_number)
SELECT 
    'ORD-002', 
    id, 
    1599.00, 
    'PENDING', 
    shipping_address, 
    'Jane Smith', 
    contact_number
FROM users WHERE contact_number = '9876543210';

-- Insert sample order items
INSERT INTO order_items (order_id, product_id, quantity, unit_price, item_total, selected_size, selected_color)
VALUES 
(1, 1, 1, 29999.00, 29999.00, '6.1', 'Black'),
(2, 2, 2, 799.50, 1599.00, '40.0', 'Navy Blue');

-- Insert sample order tracking
INSERT INTO order_tracking (order_id, status, notes)
VALUES 
(1, 'PENDING', 'Order placed successfully'),
(1, 'CONFIRMED', 'Order confirmed by admin'),
(2, 'PENDING', 'Order placed successfully');

-- ================= TABLE COMMENTS =================
COMMENT ON TABLE users IS 'Stores user information for the application';
COMMENT ON COLUMN users.contact_number IS 'Primary contact number used as username';
COMMENT ON COLUMN users.status IS 'User status: PENDING, APPROVED, REJECTED';

COMMENT ON TABLE product IS 'Stores product information';
COMMENT ON COLUMN product.status IS 'Product availability status';
COMMENT ON COLUMN product.is_published IS 'Whether product is published and visible to users';

COMMENT ON TABLE product_image IS 'Stores product images with order and primary image flag';
COMMENT ON TABLE product_size IS 'Stores product size variations';
COMMENT ON TABLE product_colors IS 'Stores available colors for products';

COMMENT ON TABLE orders IS 'Stores order information with status and customer details';
COMMENT ON COLUMN orders.order_number IS 'Unique order identifier for customer reference';
COMMENT ON COLUMN orders.status IS 'Order status: PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED, REFUNDED';
COMMENT ON COLUMN orders.total_amount IS 'Total order amount calculated from order items';

COMMENT ON TABLE order_items IS 'Stores individual items within an order with pricing and selections';
COMMENT ON COLUMN order_items.unit_price IS 'Price per unit at time of order';
COMMENT ON COLUMN order_items.item_total IS 'Calculated total for this line item (unit_price * quantity)';

COMMENT ON TABLE order_tracking IS 'Stores order status history and tracking information';
COMMENT ON COLUMN order_tracking.status IS 'Status at this tracking point';
COMMENT ON COLUMN order_tracking.notes IS 'Additional notes or comments for this status change';