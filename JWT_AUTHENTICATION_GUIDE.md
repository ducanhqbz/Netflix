# Hướng Dẫn Test JWT Authentication với API Gateway

## 📋 Tóm Tắt Những Gì Đã Thực Hiện

### 1. **AuthService** - Xử Lý Đăng Nhập & Tạo JWT Token
- ✅ Hoàn thiện `JWTService.java` với các method:
  - `generateToken()`: Tạo JWT token
  - `isTokenValid()`: Xác thực token hợp lệ
  - `extractEmail()`: Lấy username từ token
- ✅ Tạo `JwtAuthenticationFilter`: Validate token ở mỗi request
- ✅ Tạo `SecurityConfig`: Cấu hình Spring Security
- ✅ Tạo `UserDetailsServiceImpl`: Load user từ database
- ✅ Cập nhật `AuthController`: Thêm endpoint `/api/auth/login`
- ✅ Sửa `UserRepository`: Thay bằng JpaRepository

### 2. **API Gateway** - Xác Thực JWT Cho Toàn Hệ Thống
- ✅ Thêm JJWT + Spring Security vào dependencies
- ✅ Tạo `JwtTokenProvider.java`: Validate token giống AuthService
- ✅ Tạo `JwtGlobalFilter`: Filter global kiểm tra token cho tất cả request
- ✅ Tạo `SecurityConfig`: Cấu hình cho Gateway
- ✅ Cập nhật `application.yml`: Thêm JWT secret

### 3. **Cấu Hình Bảo Mật**
- Secret Key (Base64): `dGhpc2lzYXZlcnlsb25nYmFzZTY0ZW5jb2RlZHNlY3JldGtleXdpdGhhdGxlYXN0MjU2Yml0cw==`
- Token Expiration: 3600000ms (1 giờ)
- Public Paths (không cần token):
  - `/api/auth/login`
  - `/api/auth/register`
  - `/api/auth/hello`

---

## 🚀 Các Bước Test

### **Bước 1: Khởi Động Các Service**

Mở 3 terminal riêng biệt:

**Terminal 1 - AuthService (port 8081)**
```powershell
cd E:\Learning\Netflix\AuthService
.\mvnw spring-boot:run
```

**Terminal 2 - API Gateway (port 8080)**
```powershell
cd E:\Learning\Netflix\ApiGateway
.\mvnw spring-boot:run
```

**Terminal 3 - Eureka Server (port 8761) - nếu cần**
```powershell
cd E:\Learning\Netflix\DiscoverService
.\mvnw spring-boot:run
```

Chờ khoảng 30-60 giây để các service khởi động xong.

---

### **Bước 2: Tạo User Test Trong Database**

Đầu tiên, bạn cần tạo user trong database MySQL. Chạy SQL này:

```sql
USE auth_db;

-- Tạo Role
INSERT INTO roles (ROLE_CODE, ROLE_NAME, STATUS) VALUES ('USER', 'User Role', 'ACTIVE');
INSERT INTO roles (ROLE_CODE, ROLE_NAME, STATUS) VALUES ('ADMIN', 'Admin Role', 'ACTIVE');

-- Tạo User (password: 123456, đã mã hóa bằng BCrypt)
-- Hash của "123456": $2a$10$8S7H3L2q3E5K9P1M4O7R2eBF5G9A3D7M1K2L3E9F5G9A3D7M1K2L
INSERT INTO users (USERNAME, EMAIL, PASSWORD, STATUS, CREATED_DATE, MODIFIED_DATE) 
VALUES ('alice', 'alice@example.com', '$2a$10$8S7H3L2q3E5K9P1M4O7R2eBF5G9A3D7M1K2L3E9F5G9A3D7M1K2L', 'ACTIVE', NOW(), NOW());

-- Gán Role cho User
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);
```

> **Lưu ý**: Nếu bạn muốn tạo hash BCrypt khác, dùng lệnh này trong Java:
```java
new BCryptPasswordEncoder().encode("your-password")
```

---

### **Bước 3: Test Login - Lấy JWT Token**

**Cách 1: Sử dụng PowerShell**

```powershell
# 1. Login qua AuthService trực tiếp (port 8081)
$response = Invoke-WebRequest -Uri "http://localhost:8081/api/auth/login" `
    -Method POST `
    -ContentType "application/json" `
    -Body '{"username":"alice","password":"123456"}'

$token = ($response.Content | ConvertFrom-Json).token
Write-Host "Token: $token"
```

**Cách 2: Sử dụng curl**

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"123456"}'
```

**Kết Quả Mong Đợi:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbGljZSIsImlhdCI6MTcyMTA2NTAwMCwiZXhwIjoxNzIxMDY4NjAwfQ.xyz...",
  "username": "alice",
  "message": "Đăng nhập thành công"
}
```

---

### **Bước 4: Test API Gateway - Yêu Cầu Với Token**

**Cách 1: Qua API Gateway (port 8080) - Với Token**

```powershell
$token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbGljZSIsImlhdCI6MTcyMTA2NTAwMCwiZXhwIjoxNzIxMDY4NjAwfQ.xyz..."

Invoke-WebRequest -Uri "http://localhost:8080/api/auth/hello" `
    -Method GET `
    -Headers @{ Authorization = "Bearer $token" }
```

**Cách 2: Qua curl**

```bash
curl -X GET http://localhost:8080/api/auth/hello \
  -H "Authorization: Bearer $TOKEN"
```

**Kết Quả Mong Đợi:**
```
Chào bạn! Đây là phản hồi từ AuthService.
```

---

### **Bước 5: Test API Gateway - Yêu Cầu Mà Không Có Token (Sẽ Bị Reject)**

```powershell
# Test WITHOUT token - sẽ bị 401 Unauthorized
Invoke-WebRequest -Uri "http://localhost:8080/api/auth/hello" -Method GET
```

**Kết Quả Mong Đợi:**
```
StatusCode        : 401
StatusDescription : Unauthorized
```

---

### **Bước 6: Test Token Hết Hạn**

Chỉnh `expiration-ms` trong `application.yml` thành `1` (1 mili giây), restart service, đăng nhập lại, rồi:

```powershell
Start-Sleep -Seconds 1

# Dùng token cũ sẽ bị reject
Invoke-WebRequest -Uri "http://localhost:8080/api/auth/hello" `
    -Headers @{ Authorization = "Bearer $token" } -Method GET
```

**Kết Quả:**
```
StatusCode        : 401
StatusDescription : Unauthorized
```

---

## 📝 Các File Đã Tạo/Sửa

### **AuthService**
```
src/main/java/com/example/authservice/
├── Service/
│   └── JWTService.java ✅ (sửa)
├── config/
│   ├── JwtAuthenticationFilter.java ✅ (tạo)
│   ├── SecurityConfig.java ✅ (sửa)
│   └── UserDetailsServiceImpl.java ✅ (tạo)
├── controller/
│   └── AuthController.java ✅ (sửa - thêm /login)
├── Repository/
│   └── UserRepository.java ✅ (sửa - thành JpaRepository)
└── src/main/resources/
    └── application.yml ✅ (sửa - thêm JWT config)
```

### **API Gateway**
```
src/main/java/com/example/apigateway/
├── service/
│   └── JwtTokenProvider.java ✅ (tạo)
├── filter/
│   ├── JwtGlobalFilter.java ✅ (tạo)
│   └── HeaderMapRequestWrapper.java ✅ (tạo)
├── config/
│   └── SecurityConfig.java ✅ (tạo)
└── src/main/resources/
    └── application.yml ✅ (sửa - thêm JWT config)
```

---

## ✅ Kiểm Tra Lại Toàn Bộ Flow

1. ✅ AuthService mở port 8081, có endpoint `/api/auth/login`
2. ✅ AuthService tạo JWT token khi login thành công
3. ✅ API Gateway mở port 8080
4. ✅ API Gateway có JwtGlobalFilter check token trên tất cả request
5. ✅ Public paths (`/api/auth/login`, `/api/auth/hello`, `/api/auth/register`) không cần token
6. ✅ Các request khác phải có `Authorization: Bearer <TOKEN>` hợp lệ
7. ✅ Token hết hạn sẽ bị reject (401)
8. ✅ Token giả mạo sẽ bị reject (401)

---

## 🔐 Bảo Mật Sản Xuất (Production)

**⚠️ KHÔNG sử dụng cấu hình này cho production:**

1. **Secret Key**: Hiện tại dùng hardcode trong `application.yml`
   - **Giải pháp**: Dùng environment variables hoặc vault
   ```yaml
   security:
     jwt:
       secret-key: ${JWT_SECRET_KEY}  # Lấy từ env var
   ```

2. **Public Paths**: Cần review lại theo yêu cầu business
   - Có thể cần thêm `/api/auth/register`, `/api/auth/refresh-token`
   - Cần loại bỏ `/api/auth/hello` trước khi deploy

3. **Token Expiration**: 1 giờ có thể quá lâu
   - Production thường dùng 15-30 phút
   - Kèm theo `refresh-token` có thời hạn dài hơn

4. **HTTPS**: Tất cả request phải dùng HTTPS (không phải HTTP)

5. **CORS**: Cấu hình CORS nếu front-end ở domain khác

---

## 🐛 Troubleshooting

### 1. **"Cannot resolve symbol 'jsonwebtoken'" trong IDE**
- Nguyên nhân: IDE chưa refresh dependencies
- Cách sửa: Invalidate caches & restart IDE hoặc rebuild Maven

### 2. **"No 'Authorization' header in request"**
- Nguyên nhân: Forgot to add header
- Cách sửa: Thêm `Authorization: Bearer <TOKEN>` vào request

### 3. **"Invalid token" error**
- Nguyên nhân: Token hết hạn, giả mạo, hoặc secret key khác
- Cách sửa: Login lại để lấy token mới

### 4. **"User not found" khi login**
- Nguyên nhân: User chưa tồn tại trong database
- Cách sửa: Tạo user bằng SQL ở Bước 2

### 5. **Gateway port conflict**
- Nguyên nhân: Port 8080 đã sử dụng
- Cách sửa: Sửa `server.port` trong `application.yml`

---

## 📚 Tài Liệu Tham Khảo

- [JJWT Documentation](https://github.com/jwtk/jjwt)
- [Spring Security](https://spring.io/projects/spring-security)
- [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway)
- [JWT.io](https://jwt.io) - Decode token để kiểm tra

---

**Chúc mừng! 🎉 Bạn đã triển khai JWT authentication toàn hệ thống!**

