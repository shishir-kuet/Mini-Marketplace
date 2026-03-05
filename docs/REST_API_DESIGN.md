# Mini Marketplace - Simplified REST API (Academic Project)

## API Base URL
```
http://localhost:8082/api/v1
```

## Authentication
- **Type**: JWT Bearer Token
- **Header**: `Authorization: Bearer <token>`

## Core API Endpoints

### 🔐 Authentication

#### Register User
```http
POST /auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com", 
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+1234567890"
}

Response: 201 Created
{
  "message": "User registered successfully",
  "userId": 1
}
```

#### Login User  
```http
POST /auth/login
Content-Type: application/json

{
  "username": "john_doe",
  "password": "password123"
}

Response: 200 OK
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "role": "USER",
    "firstName": "John",
    "lastName": "Doe"
  }
}
```

### 👤 User Management

#### Get Current User
```http
GET /users/me
Authorization: Bearer <token>

Response: 200 OK
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com", 
  "role": "USER",
  "firstName": "John",
  "lastName": "Doe"
}
```

#### Get All Users (Admin Only)
```http
GET /users
Authorization: Bearer <admin_token>

Response: 200 OK
[
  {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "role": "USER"
  }
]
```

### 📦 Product Management

#### Get All Products (Public)
```http
GET /products?page=0&size=10&category=1&search=laptop

Response: 200 OK
{
  "content": [
    {
      "id": 1,
      "name": "Gaming Laptop",
      "description": "High performance gaming laptop",
      "price": 899.99,
      "quantity": 5,
      "categoryName": "Electronics",
      "sellerName": "tech_seller",
      "status": "ACTIVE",
      "createdAt": "2026-03-05T10:30:00"
    }
  ],
  "totalElements": 25,
  "totalPages": 3
}
```

#### Get Product by ID (Public)
```http
GET /products/{id}

Response: 200 OK
{
  "id": 1,
  "name": "Gaming Laptop",
  "description": "High performance gaming laptop",
  "price": 899.99,
  "quantity": 5,
  "category": {
    "id": 1,
    "name": "Electronics"
  },
  "seller": {
    "id": 2,
    "username": "tech_seller"
  },
  "status": "ACTIVE"
}
```

#### Create Product (Authenticated)
```http
POST /products
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Gaming Laptop",
  "description": "High performance gaming laptop",
  "price": 899.99,
  "quantity": 5,
  "categoryId": 1,
  "imageUrl": "https://example.com/laptop.jpg"
}

Response: 201 Created
{
  "id": 1,
  "message": "Product created successfully"
}
```

#### Update Product (Owner or Admin)
```http  
PUT /products/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Gaming Laptop Updated",
  "price": 849.99,
  "quantity": 3
}

Response: 200 OK
```

#### Delete Product (Owner or Admin)
```http
DELETE /products/{id}
Authorization: Bearer <token>

Response: 204 No Content
```

#### Get My Products (Seller)
```http
GET /products/my-products
Authorization: Bearer <token>

Response: 200 OK
[
  {
    "id": 1,
    "name": "Gaming Laptop",
    "price": 899.99,
    "quantity": 5,
    "status": "ACTIVE"
  }
]
```

### 🏷️ Categories

#### Get All Categories (Public)
```http
GET /categories

Response: 200 OK
[
  {
    "id": 1,
    "name": "Electronics",
    "description": "Electronic devices"
  }
]
```

#### Create Category (Admin Only)
```http
POST /categories  
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "name": "Sports",
  "description": "Sports equipment"
}

Response: 201 Created
```

### 💰 Transactions (Buy/Sell)

#### Buy Product (Create Transaction)
```http
POST /transactions
Authorization: Bearer <token>
Content-Type: application/json

{
  "productId": 1,
  "quantity": 2
}

Response: 201 Created
{
  "id": 1,
  "message": "Purchase successful",
  "totalAmount": 1799.98
}
```

#### Get My Purchases (Buyer)
```http
GET /transactions/purchases
Authorization: Bearer <token>

Response: 200 OK
[
  {
    "id": 1,
    "productName": "Gaming Laptop",
    "sellerName": "tech_seller",
    "quantity": 2,
    "totalAmount": 1799.98,
    "transactionDate": "2026-03-05T10:30:00"
  }
]
```

#### Get My Sales (Seller)
```http
GET /transactions/sales
Authorization: Bearer <token>

Response: 200 OK
[
  {
    "id": 1,
    "productName": "Gaming Laptop", 
    "buyerName": "john_doe",
    "quantity": 2,
    "totalAmount": 1799.98,
    "transactionDate": "2026-03-05T10:30:00"
  }
]
```

## Authorization Rules

| Resource | GET | POST | PUT | DELETE | Notes |
|----------|-----|------|-----|--------|-------|
| Products | Public | Auth | Owner/Admin | Owner/Admin | |
| Transactions | Owner/Admin | Auth | - | Admin | |
| Users | Self/Admin | Public | Self/Admin | Admin | |
| Categories | Public | Admin | Admin | Admin | |

## Simple Error Response
```json
{
  "error": "Unauthorized",
  "message": "You don't have permission to access this resource"
}
```