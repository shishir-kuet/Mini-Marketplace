# Final Database Schema (Mini Marketplace)

> **Clean final summary of the database schema** for your **mini marketplace academic project**.  
> Keeping it **simple, normalized, and easy to implement**.

---

## 1️⃣ Users

Stores all system users.  
A user can **buy and sell products**.  
Admins have additional permissions.

```sql
Users
-----
id (PK)
name
email (UNIQUE)  
password
role        -- "user" or "admin"
created_at
```

### Role Meaning

| Role  | Permissions                    |
| ----- | ------------------------------ |
| user  | can buy and sell products      |
| admin | manage users, products, orders |

---

## 2️⃣ Products

Stores items listed for sale.

```sql
Products
--------
id (PK)
title
description
price
seller_id (FK → Users.id)
created_at
```

### Key Rule

```text
seller_id = owner of the product
```

Only the seller can **edit or delete** their product.

---

## 3️⃣ Orders

Represents a purchase made by a user.

```sql
Orders
------
id (PK)
buyer_id (FK → Users.id)
status
created_at
```

### Example Status Values

```text
pending
completed
cancelled
```

---

## 4️⃣ Order_Items

Stores the products inside an order.

```sql
Order_Items
-----------
id (PK)
order_id (FK → Orders.id)
product_id (FK → Products.id)
quantity
price
```

### Why store `price` here?

Because product prices may change later, but **order history must remain correct**.

---

## 5️⃣ Reviews

Stores product feedback written by users.

```sql
Reviews
-------
id (PK)
user_id (FK → Users.id)
product_id (FK → Products.id)
rating
comment
created_at
updated_at
```

### Key Rule

```text
One user can review many products.
One product can have reviews from many users.
user_id + product_id should be unique.
```

---

# Relationship Summary

```
Users
 ├── sells → Products
 ├── buys → Orders
 └── writes → Reviews

Orders
 └── contains → Order_Items

Order_Items
 └── references → Products

Products
 └── receives → Reviews
```

---

# Simple ER View

```
Users
  |
  | 1
  |------< Products

Users
  |
  | 1
  |------< Orders
              |
              | 1
              |------< Order_Items >------ Products

Users
  |
  | 1
  |------< Reviews >------ Products
```

---

# Authorization Rules

### Product Edit/Delete

```
Allowed if:
product.seller_id == current_user.id
OR
current_user.role == "admin"
```

---

### Buying Product

```
Any logged-in user can buy products.
```

---

# Final Tables Count

| Table       | Purpose                |
| ----------- | ---------------------- |
| Users       | accounts + admin role  |
| Products    | items for sale         |
| Orders      | purchase records       |
| Order_Items | products inside orders |

Total: **4 tables**

---

✅ **Why this schema is good for an academic project**

* Simple and clear
* Proper **foreign key relationships**
* Supports **RBAC (admin role)**
* Demonstrates **ownership-based permissions**
* Realistic marketplace structure
);
```

### 4. Transactions Table (Simple Buy/Sell Record)
```sql
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id),
    buyer_id BIGINT NOT NULL REFERENCES users(id),
    seller_id BIGINT NOT NULL REFERENCES users(id),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10,2) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Key Relationships

```
Users (1) ──→ (M) Products (as seller)
Users (1) ──→ (M) Transactions (as buyer)
Users (1) ──→ (M) Transactions (as seller)
Categories (1) ──→ (M) Products
Products (1) ──→ (M) Transactions
```

## Essential Indexes
```sql
CREATE INDEX idx_products_seller ON products(seller_id);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_status ON products(status);
CREATE INDEX idx_transactions_buyer ON transactions(buyer_id);
CREATE INDEX idx_transactions_seller ON transactions(seller_id);
CREATE INDEX idx_transactions_product ON transactions(product_id);
```

## Sample Data
```sql
-- Insert categories
INSERT INTO categories (name, description) VALUES
('Electronics', 'Electronic devices and gadgets'),
('Books', 'Educational and entertainment books'),
('Clothing', 'Apparel and accessories');

-- Insert admin user
INSERT INTO users (username, email, password, role, first_name, last_name) VALUES
('admin', 'admin@marketplace.com', '$2a$10$hashedpassword', 'ADMIN', 'System', 'Administrator');
```