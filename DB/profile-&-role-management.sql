IF OBJECT_ID('order_items', 'U') IS NOT NULL DROP TABLE order_items;
IF OBJECT_ID('orders', 'U') IS NOT NULL DROP TABLE orders;
IF OBJECT_ID('trust_score_log', 'U') IS NOT NULL DROP TABLE trust_score_log;
IF OBJECT_ID('role_history', 'U') IS NOT NULL DROP TABLE role_history;
IF OBJECT_ID('menu_items', 'U') IS NOT NULL DROP TABLE menu_items;
IF OBJECT_ID('users', 'U') IS NOT NULL DROP TABLE users;
GO

CREATE TABLE users (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'STAFF', 'CUSTOMER')) DEFAULT 'CUSTOMER',
    address VARCHAR(500),
    telephone VARCHAR(20),
    status VARCHAR(20) CHECK (status IN ('ACTIVE', 'INACTIVE')) DEFAULT 'ACTIVE',
    reset_token VARCHAR(255),
    reset_token_expiry DATETIME2,
    trust_score INT NOT NULL DEFAULT 75,
    trust_level VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    total_orders INT NOT NULL DEFAULT 0,
    total_spending DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    completed_tasks INT NOT NULL DEFAULT 0,
    performance_rating FLOAT NOT NULL DEFAULT 0,
    complaints_count INT NOT NULL DEFAULT 0,
    cancellations INT NOT NULL DEFAULT 0,
    is_restricted BIT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    updated_at DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);
GO

CREATE TABLE role_history (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    old_role VARCHAR(30) NOT NULL,
    new_role VARCHAR(30) NOT NULL,
    changed_by VARCHAR(30) NOT NULL DEFAULT 'SYSTEM',
    reason VARCHAR(MAX),
    trust_score_at INT,
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT fk_role_history_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
GO

CREATE TABLE trust_score_log (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    old_score INT NOT NULL,
    new_score INT NOT NULL,
    change_by INT NOT NULL,
    reason VARCHAR(200) NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT fk_trust_log_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
GO

CREATE TABLE menu_items (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    item_code VARCHAR(40) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL UNIQUE,
    description VARCHAR(1000),
    category VARCHAR(60) NOT NULL,
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    cost_price DECIMAL(10,2) CHECK (cost_price >= 0),
    image_url VARCHAR(500),
    is_available BIT NOT NULL DEFAULT 1,
    preparation_time INT DEFAULT 0 CHECK (preparation_time >= 0),
    stock_qty INT DEFAULT 0 CHECK (stock_qty >= 0),
    deleted BIT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    updated_at DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);
GO

CREATE INDEX idx_menu_items_name ON menu_items(name);
CREATE INDEX idx_menu_items_category ON menu_items(category);
CREATE INDEX idx_menu_items_available ON menu_items(is_available);
GO

CREATE TABLE orders (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_number VARCHAR(30) NOT NULL UNIQUE,
    customer_name VARCHAR(120) NOT NULL,
    customer_phone VARCHAR(30) NOT NULL,
    customer_email VARCHAR(150),
    order_type VARCHAR(20) NOT NULL CHECK (order_type IN ('DINE_IN', 'TAKEAWAY', 'DELIVERY')),
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'CONFIRMED', 'PREPARING', 'READY', 'COMPLETED', 'CANCELLED')),
    payment_status VARCHAR(20) NOT NULL CHECK (payment_status IN ('UNPAID', 'PAID', 'REFUNDED', 'PARTIAL')),
    subtotal DECIMAL(10,2) NOT NULL DEFAULT 0,
    tax_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
    delivery_fee DECIMAL(10,2) NOT NULL DEFAULT 0,
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
    notes VARCHAR(1000),
    table_number VARCHAR(30),
    delivery_address VARCHAR(500),
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    updated_at DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);
GO

CREATE INDEX idx_orders_number ON orders(order_number);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);
GO

CREATE TABLE order_items (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_id BIGINT NOT NULL,
    menu_item_id BIGINT NOT NULL,
    item_name_snapshot VARCHAR(120) NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    line_total DECIMAL(10,2) NOT NULL,
    notes VARCHAR(500),
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_menu FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)
);
GO

CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_menu_item_id ON order_items(menu_item_id);
GO

INSERT INTO menu_items (item_code, name, description, category, price, cost_price, image_url, is_available, preparation_time, stock_qty)
VALUES
('BRG-001', 'Classic Beef Burger', 'Juicy grilled beef patty with cheese and house sauce.', 'Burgers', 1250.00, 700.00, 'https://images.unsplash.com/photo-1568901346375-23c9450c58cd', 1, 15, 30),
('BRG-002', 'Crispy Chicken Burger', 'Golden fried chicken, slaw, pickles and spicy mayo.', 'Burgers', 1180.00, 650.00, 'https://images.unsplash.com/photo-1550547660-d9450f859349', 1, 14, 25),
('BRG-003', 'BBQ Bacon Burger', 'Beef burger with smoky BBQ glaze and bacon.', 'Burgers', 1480.00, 840.00, 'https://images.unsplash.com/photo-1572802419224-296b0aeee0d9', 1, 18, 20),
('RIC-001', 'Chicken Fried Rice', 'Wok-tossed rice with chicken, vegetables and egg.', 'Rice', 980.00, 540.00, 'https://images.unsplash.com/photo-1512058564366-18510be2db19', 1, 12, 40),
('RIC-002', 'Seafood Nasi Goreng', 'Spiced Indonesian-style fried rice with prawns and squid.', 'Rice', 1450.00, 860.00, 'https://images.unsplash.com/photo-1603133872878-684f208fb84b', 1, 18, 18),
('RIC-003', 'Veg Curry Rice Bowl', 'Rice bowl with seasonal curries and sambol.', 'Rice', 890.00, 420.00, 'https://images.unsplash.com/photo-1512621776951-a57141f2eefd', 1, 11, 24),
('DRK-001', 'Fresh Lime Soda', 'Sparkling fresh lime with mint and a touch of sugar.', 'Drinks', 320.00, 110.00, 'https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd', 1, 4, 80),
('DRK-002', 'Iced Mocha', 'Cold mocha with espresso, chocolate and cream.', 'Drinks', 480.00, 190.00, 'https://images.unsplash.com/photo-1461023058943-07fcbe16d735', 1, 6, 60),
('DES-001', 'Chocolate Lava Cake', 'Warm chocolate cake with a molten center.', 'Desserts', 690.00, 260.00, 'https://images.unsplash.com/photo-1614707267537-b85aaf00c4b7', 1, 10, 22),
('DES-002', 'Vanilla Cheesecake', 'Creamy cheesecake with berry compote.', 'Desserts', 720.00, 280.00, 'https://images.unsplash.com/photo-1533134242443-d4fd215305ad', 1, 8, 20),
('KOT-001', 'Chicken Kottu', 'Sri Lankan chopped roti with chicken and vegetables.', 'Kottu', 1100.00, 620.00, 'https://images.unsplash.com/photo-1504674900247-0877df9cc836', 1, 16, 35),
('KOT-002', 'Cheese Kottu', 'Loaded kottu with melted cheese and chili flakes.', 'Kottu', 1280.00, 740.00, 'https://images.unsplash.com/photo-1544025162-d76694265947', 1, 17, 15),
('PIZ-001', 'Margherita Pizza', 'Tomato, mozzarella and basil on a thin crust.', 'Pizza', 1650.00, 890.00, 'https://images.unsplash.com/photo-1548365328-9f547fb0953b', 1, 20, 16),
('PIZ-002', 'Pepperoni Pizza', 'Pepperoni, mozzarella and oregano.', 'Pizza', 1890.00, 1020.00, 'https://images.unsplash.com/photo-1513104890138-7c749659a591', 1, 22, 14),
('PIZ-003', 'Veggie Supreme Pizza', 'Bell peppers, onion, olives, mushroom and corn.', 'Pizza', 1740.00, 930.00, 'https://images.unsplash.com/photo-1511689660979-10d2b1aada49', 0, 21, 12);
GO

INSERT INTO orders (order_number, customer_name, customer_phone, customer_email, order_type, status, payment_status, subtotal, tax_amount, discount_amount, delivery_fee, total_amount, notes, table_number, delivery_address)
VALUES
('DEMO-001', 'Nimal Perera', '0771234567', 'nimal@example.com', 'TAKEAWAY', 'PENDING', 'UNPAID', 2820.00, 282.00, 0.00, 0.00, 3102.00, 'Extra ketchup', NULL, NULL),
('DEMO-002', 'Sahan Fernando', '0710000000', 'sahan@example.com', 'DELIVERY', 'PREPARING', 'PAID', 2080.00, 208.00, 0.00, 250.00, 2538.00, 'Call on arrival', NULL, 'No. 12, Lake Road, Colombo 08'),
('DEMO-003', 'Asha Silva', '0765555555', 'asha@example.com', 'DINE_IN', 'COMPLETED', 'PAID', 3030.00, 303.00, 0.00, 0.00, 3333.00, NULL, 'T12', NULL),
('DEMO-004', 'Ravi Senanayake', '0759999999', NULL, 'TAKEAWAY', 'CANCELLED', 'REFUNDED', 2140.00, 214.00, 0.00, 0.00, 2354.00, 'Customer cancelled', NULL, NULL);
GO

INSERT INTO order_items (order_id, menu_item_id, item_name_snapshot, unit_price, quantity, line_total, notes)
VALUES
(1, 1, 'Classic Beef Burger', 1250.00, 2, 2500.00, NULL),
(1, 8, 'Iced Mocha', 320.00, 1, 320.00, NULL),
(2, 4, 'Chicken Fried Rice', 980.00, 1, 980.00, NULL),
(2, 11, 'Chicken Kottu', 1100.00, 1, 1100.00, NULL),
(3, 13, 'Margherita Pizza', 1650.00, 1, 1650.00, NULL),
(3, 9, 'Chocolate Lava Cake', 690.00, 2, 1380.00, NULL),
(4, 2, 'Crispy Chicken Burger', 1180.00, 1, 1180.00, NULL),
(4, 7, 'Fresh Lime Soda', 480.00, 2, 960.00, NULL);
GO
