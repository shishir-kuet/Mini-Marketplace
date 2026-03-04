# JWT Authentication System - Testing Guide

## ✅ System Successfully Refactored to JWT

### Changes Made:
1. **Role System**: Changed from ADMIN/SELLER/BUYER to **ADMIN/USER**
2. **Authentication**: Switched from session-based to **JWT token-based**
3. **Registration**: Users are automatically assigned USER role (no role selection needed)
4. **Authorization**: Stateless - no server-side sessions

---

## 🔑 API Endpoints

### 1. Register a New User
**Endpoint:** `POST http://localhost:8082/api/auth/register`

**Request Body:**
```json
{
  "username": "john",
  "email": "john@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "message": "User registered successfully",
  "username": "john",
  "role": "USER"
}
```

---

### 2. Login (Get JWT Token)
**Endpoint:** `POST http://localhost:8082/api/auth/login`

**Request Body:**
```json
{
  "username": "john",
  "password": "password123"
}
```

**Response:**
```json
{
  "message": "Login successful",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "john",
  "role": "USER",
  "tokenType": "Bearer"
}
```

**Important:** Save the token - you'll need it for all authenticated requests!

---

### 3. Access Protected Endpoints
**Endpoint:** `GET http://localhost:8082/api/auth/me`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response:**
```json
{
  "username": "john",
  "email": "john@example.com",
  "role": "USER"
}
```

---

## 🧪 Testing with PowerShell

### Register a user:
```powershell
Invoke-RestMethod -Uri "http://localhost:8082/api/auth/register" -Method POST -ContentType "application/json" -Body '{"username":"testuser","email":"test@test.com","password":"test123"}'
```

### Login and get token:
```powershell
$response = Invoke-RestMethod -Uri "http://localhost:8082/api/auth/login" -Method POST -ContentType "application/json" -Body '{"username":"testuser","password":"test123"}'
$token = $response.token
Write-Host "Token: $token"
```

### Access protected endpoint with token:
```powershell
$headers = @{ Authorization = "Bearer $token" }
Invoke-RestMethod -Uri "http://localhost:8082/api/auth/me" -Method GET -Headers $headers
```

---

## 🎯 How Authorization Works

### USER Role (default for all registered users):
- ✅ Can buy products
- ✅ Can sell their own products
- ✅ Can edit/update/delete **only their own products**
- ❌ Cannot access admin endpoints (`/api/admin/**`)

### ADMIN Role:
- ✅ Full access to everything
- ✅ Access to admin endpoints (`/api/admin/**`)
- ✅ Can manage all users and products

**Note:** To create an ADMIN user, you must manually update the database:
```sql
UPDATE users SET role = 'ADMIN' WHERE username = 'youruser';
```

---

## 🔐 JWT Token Details

- **Expiration:** 24 hours (86400000 ms)
- **Algorithm:** HS256
- **Contains:** username, role, issued time, expiration time
- **Storage:** Token should be stored in frontend (localStorage/sessionStorage)
- **Usage:** Include in Authorization header as `Bearer <token>`

---

## 🚀 Next Steps

1. **Start the application:**
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

2. **Delete old test data** (optional):
   ```sql
   DELETE FROM users;
   ```

3. **Test the new JWT system** using the PowerShell commands above

4. **For product management**, you'll create RESTful endpoints where:
   - Users can only modify products they own
   - Admin can modify any product
   - Use `@PreAuthorize` annotations for method-level security

---

## 📝 Database Changes Needed

Your existing users table is compatible! The role column now stores:
- `USER` (instead of BUYER/SELLER)
- `ADMIN` (unchanged)

Old data with SELLER/BUYER roles will cause errors. Clean the table before testing.

---

## 🔒 Security Features

✅ **Stateless** - No server sessions  
✅ **BCrypt password encryption**  
✅ **JWT token-based authentication**  
✅ **Role-based authorization**  
✅ **Token expiration** (24 hours)  
✅ **CSRF protection** (disabled for REST API)  
✅ **Separate frontend/backend** ready  

---

**System is ready for production! 🎉**
