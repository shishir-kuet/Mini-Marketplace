# Mini Marketplace — REST API Design

## Base URL
```
http://localhost:8083/api
```

## Authentication
- **Type**: JWT Bearer Token
- **Header**: `Authorization: Bearer <token>`
- **Expiry**: 24 hours

## Authorization Levels
| Level | Description |
|---|---|
| Public | No token required |
| Authenticated | Valid JWT required (any logged-in user) |
| Owner/Admin | Owner of resource OR admin role |
| Admin | `role = "admin"` required |

---

## Auth Endpoints

### Register
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "ratul",
  "email": "ratul@example.com",
  "password": "pass123"
}
```
Response `201 Created`
```json
{ "message": "User registered successfully", "username": "ratul", "role": "user" }
```
Error `400` — username/email already taken, validation fail

---

### Login
```http
POST /api/auth/login
Content-Type: application/json

{ "username": "ratul", "password": "pass123" }
```
Response `200 OK`
```json
{
  "message": "Login successful",
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "username": "ratul",
  "role": "user"
}
```
Error `401` — wrong credentials

---

## User Endpoints

### GET /api/users/me — Get My Profile *(Authenticated)*
```json
{ "id": 1, "username": "ratul", "email": "ratul@example.com", "role": "user", "createdAt": "..." }
```

### PUT /api/users/me — Update My Profile *(Authenticated)*
```json
{ "email": "newemail@example.com", "password": "newpass123" }
```
Both fields optional.

### GET /api/users — All Users *(Admin only)*

### GET /api/users/{id} — User by ID *(Admin only)*

### PUT /api/users/{id}/role — Change Role *(Admin only)*
```json
{ "role": "admin" }
```
role must be `"user"` or `"admin"`

### DELETE /api/users/{id} — Delete User *(Admin only)*
Response `204 No Content`

---

## Product Endpoints

### GET /api/products — All Products *(Public)*
```json
[{ "id": 1, "title": "Gaming Laptop", "description": "...", "price": 75000.00, "sellerId": 1, "sellerUsername": "ratul", "createdAt": "..." }]
```

### GET /api/products/{id} — By ID *(Public)*

### GET /api/products/seller/{sellerId} — By Seller *(Public)*

### GET /api/products/search?title=laptop — Search *(Public)*

### GET /api/products/my — My Products *(Authenticated)*

### POST /api/products — Create Product *(Authenticated)*
```json
{ "title": "Gaming Laptop", "description": "RTX 4060, 16GB RAM", "price": 75000.00 }
```
Response `201 Created` — product object

### PUT /api/products/{id} — Update Product *(Owner or Admin)*
```json
{ "title": "Gaming Laptop Pro", "description": "Updated", "price": 80000.00 }
```
Error `403` — if not owner and not admin

### DELETE /api/products/{id} — Delete Product *(Owner or Admin)*
Response `204 No Content`

---

## Order Endpoints

### POST /api/orders — Place Order *(Authenticated)*
```json
{
  "items": [
    { "productId": 1, "quantity": 2 }
  ]
}
```
Response `201 Created`
```json
{
  "id": 1, "buyerId": 6, "buyerUsername": "testuser", "status": "pending",
  "items": [{ "id": 1, "productId": 1, "productTitle": "Gaming Laptop", "quantity": 2, "price": 75000.00, "totalPrice": 150000.00 }],
  "totalAmount": 150000.00, "createdAt": "..."
}
```
Price is **snapshotted** at time of purchase.

### GET /api/orders/my — My Orders *(Authenticated)*

### GET /api/orders/{id} — Order Detail *(Own orders; Admin sees all)*
Error `403` — viewing another user's order without admin role

### PUT /api/orders/{id}/cancel — Cancel Order *(Own pending orders only)*
Error `400` — if order is not `pending`

### GET /api/orders — All Orders *(Admin only)*

### PUT /api/orders/{id}/complete — Complete Order *(Admin only)*
Error `400` — if order is not `pending`

---

## Category Endpoints

### GET /api/categories — All Categories *(Public)*
```json
[{ "id": 1, "name": "Electronics", "description": "Phones, laptops etc", "createdAt": "..." }]
```

### GET /api/categories/{id} — By ID *(Public)*

### POST /api/categories — Create *(Admin only)*
```json
{ "name": "Electronics", "description": "Phones, laptops etc" }
```
Response `201 Created`

### PUT /api/categories/{id} — Update *(Admin only)*

### DELETE /api/categories/{id} — Delete *(Admin only)*
Response `204 No Content`

---

## Authorization Summary

| Endpoint | Public | Auth | Owner/Admin | Admin |
|---|---|---|---|---|
| GET /products, /products/search, /categories | ✅ | | | |
| POST /auth/register, /auth/login | ✅ | | | |
| POST /products | | ✅ | | |
| GET /products/my, /orders/my, /users/me | | ✅ | | |
| POST /orders, PUT /users/me | | ✅ | | |
| PUT /products/{id}, DELETE /products/{id} | | | ✅ | |
| GET /orders/{id}, PUT /orders/{id}/cancel | | | ✅ | |
| GET /orders, PUT /orders/{id}/complete | | | | ✅ |
| POST/PUT/DELETE /categories | | | | ✅ |
| GET /users, PUT /users/{id}/role, DELETE /users/{id} | | | | ✅ |

## Error Response Format
```json
{ "error": "Error message here" }
```

| Status | When |
|---|---|
| `400` | Validation fail, business logic error |
| `401` | Invalid/missing token or wrong credentials |
| `403` | Insufficient permission |
| `404` | Resource not found |
| `500` | Unexpected server error |
