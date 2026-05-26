# 🛒 E-Commerce REST API

> Dự án backend thương mại điện tử xây dựng bằng **Spring Boot 3.x**, triển khai đầy đủ luồng mua hàng từ đăng ký tài khoản → xem sản phẩm → thêm giỏ hàng → đặt hàng → review.

---

## 📌 Mục lục

- [Tech Stack](#-tech-stack)
- [Tính năng](#-tính-năng)
- [Cấu trúc Entity](#-cấu-trúc-entity--quan-hệ)
- [Cài đặt & Chạy](#-cài-đặt--chạy)
- [API Endpoints](#-api-endpoints)
- [Ví dụ Request/Response](#-ví-dụ-requestresponse)
- [Cấu trúc Project](#-cấu-trúc-project)
- [Điểm thiết kế nổi bật](#-điểm-thiết-kế-nổi-bật)
- [Tài khoản mẫu](#-tài-khoản-mẫu-seed-data)

---

## 🧱 Tech Stack

| Thành phần | Công nghệ |
|-----------|-----------|
| **Framework** | Spring Boot 3.x |
| **Language** | Java 21 |
| **Security** | Spring Security 6 + JWT (jjwt 0.12.5) |
| **ORM** | Spring Data JPA / Hibernate |
| **Database** | MySQL 8.0 |
| **Validation** | spring-boot-starter-validation |
| **API Docs** | springdoc-openapi 2.5.0 (Swagger UI) |
| **Boilerplate** | Lombok |
| **Build** | Maven |
| **Container** | Docker Compose (MySQL) |

---

## ✅ Tính năng

### 🔐 Auth
- Đăng ký tài khoản (role mặc định: `USER`)
- Đăng nhập → trả về **JWT token**
- Xem thông tin tài khoản đang đăng nhập

### 📦 Category (ADMIN)
- CRUD danh mục sản phẩm
- Danh sách category public — không cần đăng nhập

### 🛍️ Product
- Xem danh sách có **pagination** + **filter** theo category, giá, từ khóa
- Xem chi tiết sản phẩm kèm **điểm rating trung bình**
- CRUD sản phẩm (ADMIN)

### 🛒 Cart (USER)
- Mỗi user có **1 giỏ hàng riêng**, tạo tự động khi đăng ký
- Thêm / cập nhật số lượng / xóa 1 sản phẩm / xóa toàn bộ giỏ

### 📑 Order (USER + ADMIN)
- **Đặt hàng từ giỏ hàng**: validate tồn kho → tạo đơn → trừ stock → clear cart
- **Hủy đơn** nếu còn trạng thái `PENDING`
- Xem lịch sử + chi tiết đơn hàng
- Admin xem tất cả đơn / cập nhật trạng thái theo luồng

### ⭐ Review (USER)
- Viết review + rating (1-5 sao) cho sản phẩm **đã mua và đã được giao** (`DELIVERED`)
- Mỗi user chỉ được review 1 sản phẩm 1 lần
- Xóa review: chủ review hoặc ADMIN

### ❌ Ngoài scope (cố ý giới hạn)
- Thanh toán / Payment gateway
- Email notification
- Upload ảnh sản phẩm
- Discount / coupon
- Refresh token

---

## 🗂️ Cấu trúc Entity & Quan hệ

```
User ──(1:1)────── Cart
User ──(1:N)────── Order
User ──(1:N)────── Review
Cart ──(1:N)────── CartItem ──(N:1)── Product
Order ─(1:N)────── OrderItem ──(N:1)─ Product
Product ─(N:1)──── Category
Product ─(1:N)──── Review
```

### Mô tả các Entity

| Entity | Các trường chính |
|--------|-----------------|
| `User` | id, name, email, password (BCrypt), role (USER/ADMIN), createdAt |
| `Category` | id, name |
| `Product` | id, name, description, price, stock, **version** (optimistic lock), category, createdAt |
| `Cart` | id, user |
| `CartItem` | id, cart, product, quantity |
| `Order` | id, user, status (enum), items, createdAt |
| `OrderItem` | id, order, product, quantity, **price** (snapshot giá) |
| `Review` | id, user, product, rating (1-5), comment, createdAt |

---

## 🚀 Cài đặt & Chạy

### Yêu cầu
- Java 21+
- Docker & Docker Compose

### Bước 1 — Clone và vào thư mục
```bash
git clone <repo-url>
cd ecommerce-api
```

### Bước 2 — Khởi động MySQL bằng Docker
```bash
docker compose up -d
```

MySQL sẽ chạy tại `localhost:3306` với:
- Database: `ecommerce_db`
- Username: `ecommerce_user`
- Password: `ecommerce_pass`

### Bước 3 — Chạy ứng dụng
```bash
./mvnw spring-boot:run
```

Ứng dụng khởi động tại: **http://localhost:8080**

Lần đầu chạy, `DataInitializer` sẽ tự động seed:
- 2 tài khoản (admin + user)
- 3 danh mục
- 5 sản phẩm mẫu

### Bước 4 — Xem Swagger UI
```
http://localhost:8080/swagger-ui/index.html
```

> Nhấn **Authorize** → nhập `Bearer <token>` để test các endpoint cần xác thực.

---

## 🔑 Tài khoản mẫu (Seed Data)

| Role | Email | Password |
|------|-------|----------|
| ADMIN | `admin@example.com` | `admin123` |
| USER | `user@example.com` | `user123` |

**Sản phẩm mẫu:**

| Tên | Category | Giá | Tồn kho |
|-----|----------|-----|---------|
| iPhone 15 | Electronics | $999.99 | 50 |
| Samsung Galaxy S24 | Electronics | $799.99 | 30 |
| ASUS ROG Strix G15 | Electronics | $1,299.99 | 15 |
| Áo thun unisex cotton | Clothing | $19.99 | 200 |
| Clean Code - Robert C. Martin | Books | $39.99 | 100 |

---

## 📡 API Endpoints

### 🔐 Auth — `/auth`

| Method | URL | Auth | Mô tả |
|--------|-----|------|-------|
| `POST` | `/auth/register` | ❌ | Đăng ký tài khoản mới |
| `POST` | `/auth/login` | ❌ | Đăng nhập, nhận JWT token |
| `GET` | `/auth/me` | ✅ Token | Xem thông tin tài khoản |

### 📦 Category — `/categories`

| Method | URL | Auth | Mô tả |
|--------|-----|------|-------|
| `GET` | `/categories` | ❌ | Danh sách tất cả category |
| `POST` | `/categories` | ✅ ADMIN | Tạo category mới |
| `PUT` | `/categories/{id}` | ✅ ADMIN | Cập nhật category |
| `DELETE` | `/categories/{id}` | ✅ ADMIN | Xóa category |

### 🛍️ Product — `/products`

| Method | URL | Auth | Mô tả |
|--------|-----|------|-------|
| `GET` | `/products` | ❌ | Danh sách + filter + pagination |
| `GET` | `/products/{id}` | ❌ | Chi tiết sản phẩm + avg rating |
| `POST` | `/products` | ✅ ADMIN | Tạo sản phẩm |
| `PUT` | `/products/{id}` | ✅ ADMIN | Cập nhật sản phẩm |
| `DELETE` | `/products/{id}` | ✅ ADMIN | Xóa sản phẩm |

**Query params cho `GET /products`:**

| Param | Kiểu | Mô tả |
|-------|------|-------|
| `keyword` | String | Tìm theo tên sản phẩm (không phân biệt hoa/thường) |
| `categoryId` | Long | Lọc theo category |
| `minPrice` | Decimal | Giá tối thiểu |
| `maxPrice` | Decimal | Giá tối đa |
| `page` | int | Số trang (default: 0) |
| `size` | int | Số item mỗi trang (default: 10) |

### 🛒 Cart — `/cart`

| Method | URL | Auth | Mô tả |
|--------|-----|------|-------|
| `GET` | `/cart` | ✅ Token | Xem giỏ hàng hiện tại |
| `POST` | `/cart/items` | ✅ Token | Thêm sản phẩm vào giỏ |
| `PUT` | `/cart/items/{productId}` | ✅ Token | Cập nhật số lượng |
| `DELETE` | `/cart/items/{productId}` | ✅ Token | Xóa 1 sản phẩm khỏi giỏ |
| `DELETE` | `/cart` | ✅ Token | Xóa toàn bộ giỏ hàng |

### 📑 Order — `/orders`

| Method | URL | Auth | Mô tả |
|--------|-----|------|-------|
| `POST` | `/orders` | ✅ Token | Đặt hàng từ cart |
| `GET` | `/orders/my` | ✅ Token | Lịch sử đơn hàng của mình |
| `GET` | `/orders/my/{id}` | ✅ Token | Chi tiết 1 đơn hàng |
| `PUT` | `/orders/my/{id}/cancel` | ✅ Token | Hủy đơn (chỉ khi PENDING) |
| `GET` | `/orders` | ✅ ADMIN | Tất cả đơn hàng |
| `PUT` | `/orders/{id}/status` | ✅ ADMIN | Cập nhật trạng thái |

**Order Status Flow:**
```
PENDING ──► CONFIRMED ──► SHIPPED ──► DELIVERED
   │
   └──► CANCELLED  (chỉ hủy được khi đang PENDING)
```

> Khi hủy đơn: stock sản phẩm được **hoàn trả** tự động.

### ⭐ Review — `/products/{id}/reviews`

| Method | URL | Auth | Mô tả |
|--------|-----|------|-------|
| `GET` | `/products/{id}/reviews` | ❌ | Xem tất cả reviews của sản phẩm |
| `POST` | `/products/{id}/reviews` | ✅ Token | Viết review (cần đã mua + DELIVERED) |
| `DELETE` | `/products/{id}/reviews/{reviewId}` | ✅ Token | Xóa review |

---

## 📝 Ví dụ Request/Response

### 1. Đăng ký tài khoản

**Request:**
```http
POST /auth/register
Content-Type: application/json

{
  "name": "Nguyen Van A",
  "email": "nguyenvana@example.com",
  "password": "password123"
}
```

**Response `201 Created`:**
```json
{
  "id": 3,
  "name": "Nguyen Van A",
  "email": "nguyenvana@example.com",
  "role": "USER",
  "createdAt": "2026-05-26T15:00:00"
}
```

---

### 2. Đăng nhập

**Request:**
```http
POST /auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "user123"
}
```

**Response `200 OK`:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNzE2..."
}
```

---

### 3. Xem danh sách sản phẩm (có filter)

**Request:**
```http
GET /products?keyword=iPhone&minPrice=500&maxPrice=1200&page=0&size=5
```

**Response `200 OK`:**
```json
{
  "content": [
    {
      "id": 1,
      "name": "iPhone 15",
      "description": "Apple iPhone 15, 128GB, chip A16 Bionic",
      "price": 999.99,
      "stock": 50,
      "categoryName": "Electronics",
      "avgRating": null,
      "createdAt": "2026-05-26T08:00:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 5,
  "number": 0
}
```

---

### 4. Thêm sản phẩm vào giỏ hàng

**Request:**
```http
POST /cart/items
Authorization: Bearer <token>
Content-Type: application/json

{
  "productId": 1,
  "quantity": 2
}
```

**Response `200 OK`:**
```json
{
  "cartId": 1,
  "items": [
    {
      "productId": 1,
      "productName": "iPhone 15",
      "price": 999.99,
      "quantity": 2,
      "subtotal": 1999.98
    }
  ],
  "totalPrice": 1999.98
}
```

---

### 5. Đặt hàng từ giỏ hàng

**Request:**
```http
POST /orders
Authorization: Bearer <token>
```

**Response `201 Created`:**
```json
{
  "id": 1,
  "status": "PENDING",
  "items": [
    {
      "productId": 1,
      "productName": "iPhone 15",
      "quantity": 2,
      "price": 999.99,
      "subtotal": 1999.98
    }
  ],
  "totalPrice": 1999.98,
  "createdAt": "2026-05-26T15:05:00"
}
```

> ✅ Sau khi đặt hàng: stock giảm 2, giỏ hàng tự động xóa.

---

### 6. Cập nhật trạng thái đơn hàng (ADMIN)

**Request:**
```http
PUT /orders/1/status
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "status": "CONFIRMED"
}
```

**Response `200 OK`:**
```json
{
  "id": 1,
  "status": "CONFIRMED",
  ...
}
```

---

### 7. Viết review sản phẩm

**Request:**
```http
POST /products/1/reviews
Authorization: Bearer <token>
Content-Type: application/json

{
  "rating": 5,
  "comment": "Sản phẩm tốt, giao hàng nhanh!"
}
```

**Response `201 Created`:**
```json
{
  "id": 1,
  "rating": 5,
  "comment": "Sản phẩm tốt, giao hàng nhanh!",
  "userName": "Nguyen Van A",
  "createdAt": "2026-05-26T16:00:00"
}
```

**Nếu chưa mua hoặc chưa giao hàng (`400 Bad Request`):**
```json
{
  "timestamp": "2026-05-26T16:00:00",
  "status": 400,
  "message": "Bạn chỉ có thể review sản phẩm đã mua và đã được giao hàng"
}
```

---

### 8. Lỗi thường gặp

| HTTP Status | Trường hợp |
|------------|-----------|
| `400 Bad Request` | Validation lỗi, business logic vi phạm |
| `401 Unauthorized` | Thiếu hoặc sai JWT token |
| `403 Forbidden` | Đúng token nhưng không đủ role |
| `404 Not Found` | Resource không tồn tại |
| `500 Internal Server Error` | Lỗi server (xem log) |

---

## 📁 Cấu trúc Project

```
src/main/java/com/thai2004z/ecommerce_api/
│
├── EcommerceApiApplication.java      ← Main class
├── DataInitializer.java              ← Seed data khi khởi động lần đầu
│
├── config/
│   ├── SecurityConfig.java           ← Spring Security + JWT filter chain
│   └── OpenApiConfig.java            ← Swagger UI config
│
├── security/
│   ├── JwtUtil.java                  ← Tạo / validate JWT token
│   ├── JwtFilter.java                ← Filter chạy mỗi request
│   └── UserDetailsServiceImpl.java   ← Load user từ DB bằng email
│
├── entity/                           ← JPA Entities
│   ├── Role.java                     ← enum: USER, ADMIN
│   ├── OrderStatus.java              ← enum: PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
│   ├── User.java
│   ├── Category.java
│   ├── Product.java                  ← @Version cho optimistic locking
│   ├── Cart.java
│   ├── CartItem.java
│   ├── Order.java                    ← @Table("orders") tránh SQL reserved word
│   ├── OrderItem.java                ← price: snapshot giá tại thời điểm đặt
│   └── Review.java
│
├── repository/                       ← Spring Data JPA Repositories
│   ├── UserRepository.java
│   ├── CategoryRepository.java
│   ├── ProductRepository.java        ← Custom filter query + JOIN FETCH
│   ├── CartRepository.java
│   ├── CartItemRepository.java       ← findByCartIdAndProductId()
│   ├── OrderRepository.java          ← existsByUserIdAndProductIdAndStatus()
│   └── ReviewRepository.java         ← findAverageRatingByProductId()
│
├── dto/
│   ├── request/                      ← Input validation với @Valid
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   ├── CategoryRequest.java
│   │   ├── ProductRequest.java
│   │   ├── CartItemRequest.java
│   │   ├── ReviewRequest.java
│   │   └── OrderStatusRequest.java
│   └── response/                     ← Static from(Entity) factory methods
│       ├── AuthResponse.java
│       ├── UserResponse.java
│       ├── CategoryResponse.java
│       ├── ProductResponse.java
│       ├── CartItemResponse.java
│       ├── CartResponse.java
│       ├── OrderItemResponse.java
│       ├── OrderResponse.java
│       └── ReviewResponse.java
│
├── service/                          ← Business logic — @Transactional ở đây
│   ├── AuthService.java
│   ├── CategoryService.java
│   ├── ProductService.java           ← @Transactional(readOnly=true) cho queries
│   ├── CartService.java
│   ├── OrderService.java             ← @Transactional đặt hàng + rollback
│   └── ReviewService.java            ← Validate đã mua + DELIVERED
│
├── controller/                       ← REST Controllers — không có logic
│   ├── AuthController.java
│   ├── CategoryController.java
│   ├── ProductController.java        ← Bao gồm review sub-endpoints
│   ├── CartController.java
│   └── OrderController.java
│
└── exception/
    ├── ResourceNotFoundException.java  ← 404
    ├── BusinessException.java          ← 400 business logic
    └── GlobalExceptionHandler.java     ← @RestControllerAdvice
```

---

## 💡 Điểm thiết kế nổi bật

### 1. Optimistic Locking — `@Version` trên `Product.stock`
```java
@Version
private Long version;
```
Khi 2 người cùng mua sản phẩm cuối, người thứ 2 sẽ nhận `OptimisticLockException` → hệ thống không cho phép oversell.

---

### 2. `@Transactional` trong `OrderService.placeOrder()`
```
1. Validate tất cả stock TRƯỚC khi làm gì
2. Tạo Order
3. Tạo OrderItems + trừ stock + snapshot giá
4. Clear cart
→ Nếu bất kỳ bước nào fail → rollback toàn bộ
```

---

### 3. Price Snapshot trong `OrderItem`
```java
// Không dùng product.getPrice() sau này vì giá có thể thay đổi
@Column(nullable = false)
private BigDecimal price;  // giá tại thời điểm đặt hàng
```

---

### 4. DTO Layer — Tách biệt hoàn toàn khỏi Entity
- Không bao giờ trả `Entity` trực tiếp từ Controller
- Mỗi Response DTO có static factory method `from(Entity e)` → không cần MapStruct
- Không expose `password`, `version`, các trường nhạy cảm

---

### 5. N+1 Query Prevention
```java
// Dùng JOIN FETCH thay vì findAll() để tránh N+1 queries
@Query("SELECT p FROM Product p JOIN FETCH p.category c " +
       "WHERE (:keyword IS NULL OR LOWER(p.name) LIKE ...)")
Page<Product> findWithFilters(...);
```

---

### 6. Business Validation trong `ReviewService`
```
Điều kiện để review:
✅ User đã có Order với product này
✅ Order đó có status = DELIVERED
✅ User chưa review sản phẩm này trước đó
```

---

### 7. Security — Thứ tự matchers quan trọng
```java
// Review endpoints phải đặt TRƯỚC rule admin-only cho products
.requestMatchers(HttpMethod.POST, "/products/*/reviews").authenticated()   // USER
.requestMatchers(HttpMethod.DELETE, "/products/*/reviews/*").authenticated() // USER/ADMIN

// Sau đó mới đến rule ADMIN-only cho product CRUD
.requestMatchers(HttpMethod.POST, "/products").hasRole("ADMIN")
```

---

## ⚙️ Cấu hình

### `application.yaml`
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ecommerce_db?useSSL=false&serverTimezone=UTC
    username: ecommerce_user
    password: ecommerce_pass

  jpa:
    hibernate:
      ddl-auto: update    # Tự tạo/update bảng
    show-sql: true

app:
  jwt:
    secret: 404E635266556A586E3272...   # Hex-encoded 256-bit key
    expiration: 86400000                 # 24 giờ (ms)
```

### `docker-compose.yml`
```yaml
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_DATABASE: ecommerce_db
      MYSQL_USER: ecommerce_user
      MYSQL_PASSWORD: ecommerce_pass
    ports:
      - "3306:3306"
```

---

## 🧪 Test nhanh với curl

```bash
# 1. Đăng nhập lấy token
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"user123"}' \
  | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

# 2. Xem sản phẩm
curl http://localhost:8080/products

# 3. Thêm vào giỏ
curl -X POST http://localhost:8080/cart/items \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"productId":1,"quantity":2}'

# 4. Đặt hàng
curl -X POST http://localhost:8080/orders \
  -H "Authorization: Bearer $TOKEN"

# 5. Xem đơn hàng
curl http://localhost:8080/orders/my \
  -H "Authorization: Bearer $TOKEN"
```

---

## 📊 Database Schema (tóm tắt)

```sql
users          → id, name, email, password, role, created_at
categories     → id, name
products       → id, name, description, price, stock, version, category_id, created_at
carts          → id, user_id
cart_items     → id, cart_id, product_id, quantity
orders         → id, user_id, status, created_at
order_items    → id, order_id, product_id, quantity, price
reviews        → id, user_id, product_id, rating, comment, created_at
```

> Hibernate tự động tạo bảng khi `ddl-auto: update`. Không cần chạy script SQL thủ công.

---

## 📄 License

Dự án cá nhân phục vụ mục đích học tập và portfolio.
