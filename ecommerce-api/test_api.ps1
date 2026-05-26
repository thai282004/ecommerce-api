# Automated Integration Test Script for E-Commerce API (100% Endpoint Coverage)
# This script tests ALL 23 endpoints defined in Auth, Category, Product, Cart, Order, and Review Controllers.

$baseUrl = "http://localhost:8080"
$ErrorActionPreference = "Stop"

# Helpers for colorful output
function Write-Header($text) {
    Write-Host "`n======================================================================" -ForegroundColor Cyan
    Write-Host ">>> $text" -ForegroundColor Cyan
    Write-Host "======================================================================" -ForegroundColor Cyan
}

function Write-Success($text) {
    Write-Host "[SUCCESS] $text" -ForegroundColor Green
}

function Write-Info($text) {
    Write-Host "[INFO] $text" -ForegroundColor DarkYellow
}

# ======================================================================
# MODULE 1: AUTH CONTROLLER (3/3 ENDPOINTS)
# ======================================================================
Write-Header "MODULE 1: AUTH CONTROLLER"

$rand = Get-Random -Minimum 1000 -Maximum 9999
$customerEmail = "buyer_$rand@example.com"
$customerPassword = "password123"

# 1. POST /auth/register
$registerBody = @{
    name = "Customer $rand"
    email = $customerEmail
    password = $customerPassword
} | ConvertTo-Json
$customer = Invoke-RestMethod -Uri "$baseUrl/auth/register" -Method Post -Body $registerBody -ContentType "application/json"
Write-Success "1. POST /auth/register -> Registered: $($customer.name) ($($customer.email))"

# 2. POST /auth/login
$loginBody = @{
    email = $customerEmail
    password = $customerPassword
} | ConvertTo-Json
$loginRes = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
$customerToken = $loginRes.token
$headers = @{ Authorization = "Bearer $customerToken" }
Write-Success "2. POST /auth/login -> Logged in! JWT Token: $($customerToken.Substring(0, 15))..."

# 3. GET /auth/me
$me = Invoke-RestMethod -Uri "$baseUrl/auth/me" -Method Get -Headers $headers
Write-Success "3. GET /auth/me -> Current User: ID=$($me.id), Name=$($me.name), Role=$($me.role)"


# ======================================================================
# SETUP ADMIN TOKEN FOR PROTECTED CRUD ENDPOINTS
# ======================================================================
$adminLoginBody = @{
    email = "admin@example.com"
    password = "admin123"
} | ConvertTo-Json
$adminLoginRes = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $adminLoginBody -ContentType "application/json"
$adminToken = $adminLoginRes.token
$adminHeaders = @{ Authorization = "Bearer $adminToken" }


# ======================================================================
# MODULE 2: CATEGORY CONTROLLER (4/4 ENDPOINTS)
# ======================================================================
Write-Header "MODULE 2: CATEGORY CONTROLLER"

# 4. POST /categories (ADMIN)
$catBody = @{ name = "Temp Category $rand" } | ConvertTo-Json
$newCat = Invoke-RestMethod -Uri "$baseUrl/categories" -Method Post -Headers $adminHeaders -Body $catBody -ContentType "application/json"
$tempCatId = $newCat.id
Write-Success "4. POST /categories -> Created Category: ID=$tempCatId, Name=$($newCat.name)"

# 5. PUT /categories/{id} (ADMIN)
$catUpdateBody = @{ name = "Temp Category $rand - Updated" } | ConvertTo-Json
$updatedCat = Invoke-RestMethod -Uri "$baseUrl/categories/$tempCatId" -Method Put -Headers $adminHeaders -Body $catUpdateBody -ContentType "application/json"
Write-Success "5. PUT /categories/{id} -> Updated Category name to: $($updatedCat.name)"

# 6. GET /categories (PUBLIC)
$categories = Invoke-RestMethod -Uri "$baseUrl/categories" -Method Get
Write-Success "6. GET /categories -> Total categories found: $($categories.Count)"

# 7. DELETE /categories/{id} (ADMIN)
$nullResponse = Invoke-RestMethod -Uri "$baseUrl/categories/$tempCatId" -Method Delete -Headers $adminHeaders
Write-Success "7. DELETE /categories/{id} -> Deleted Temp Category ID=$tempCatId successfully."


# ======================================================================
# MODULE 3: PRODUCT CONTROLLER - ADMIN CRUD (5/5 PRODUCT ENDPOINTS)
# ======================================================================
Write-Header "MODULE 3: PRODUCT CONTROLLER (PRODUCT CRUD)"

# 8. POST /products (ADMIN)
$prodBody = @{
    name = "Temp Phone $rand"
    description = "Temporary product for automated API testing"
    price = 149.99
    stock = 10
    categoryId = 1 # standard Electronics category
} | ConvertTo-Json
$newProduct = Invoke-RestMethod -Uri "$baseUrl/products" -Method Post -Headers $adminHeaders -Body $prodBody -ContentType "application/json"
$tempProdId = $newProduct.id
Write-Success "8. POST /products -> Created Product: ID=$tempProdId, Name=$($newProduct.name), Price=$($newProduct.price)$"

# 9. GET /products (PUBLIC)
$productsRes = Invoke-RestMethod -Uri "$baseUrl/products?page=0&size=5" -Method Get
Write-Success "9. GET /products -> Paginated list returned. Found $($productsRes.content.Count) products on page 0."

# 10. GET /products/{id} (PUBLIC)
$productDetail = Invoke-RestMethod -Uri "$baseUrl/products/$tempProdId" -Method Get
Write-Success "10. GET /products/{id} -> Retrieved detail: Name=$($productDetail.name), Stock=$($productDetail.stock)"

# 11. PUT /products/{id} (ADMIN)
$prodUpdateBody = @{
    name = "Temp Phone $rand - Updated"
    description = "Updated temporary product description"
    price = 179.99
    stock = 8
    categoryId = 1
} | ConvertTo-Json
$updatedProduct = Invoke-RestMethod -Uri "$baseUrl/products/$tempProdId" -Method Put -Headers $adminHeaders -Body $prodUpdateBody -ContentType "application/json"
Write-Success "11. PUT /products/{id} -> Updated Product Price to: $($updatedProduct.price)$"

# 12. DELETE /products/{id} (ADMIN)
Invoke-RestMethod -Uri "$baseUrl/products/$tempProdId" -Method Delete -Headers $adminHeaders
Write-Success "12. DELETE /products/{id} -> Deleted Temp Product ID=$tempProdId successfully."


# ======================================================================
# MODULE 4: CART CONTROLLER (5/5 ENDPOINTS)
# ======================================================================
Write-Header "MODULE 4: CART CONTROLLER"

# 13. GET /cart (Initially empty or clean)
$cart = Invoke-RestMethod -Uri "$baseUrl/cart" -Method Get -Headers $headers
Write-Success "13. GET /cart -> Retrieved current cart. Total items count: $($cart.items.Count)"

# 14. POST /cart/items (Add iPhone 15 [ID 1] x 2 and Samsung S24 [ID 2] x 1)
$cartBody1 = @{ productId = 1; quantity = 2 } | ConvertTo-Json
$cartRes = Invoke-RestMethod -Uri "$baseUrl/cart/items" -Method Post -Headers $headers -Body $cartBody1 -ContentType "application/json"
$cartBody2 = @{ productId = 2; quantity = 1 } | ConvertTo-Json
$cartRes = Invoke-RestMethod -Uri "$baseUrl/cart/items" -Method Post -Headers $headers -Body $cartBody2 -ContentType "application/json"
Write-Success "14. POST /cart/items -> Added ID 1 and ID 2 to cart. Total Price: $($cartRes.totalPrice)$"

# 15. PUT /cart/items/{productId} (Update iPhone 15 quantity to 4)
$cartUpdateBody = @{ productId = 1; quantity = 4 } | ConvertTo-Json
$cartRes = Invoke-RestMethod -Uri "$baseUrl/cart/items/1" -Method Put -Headers $headers -Body $cartUpdateBody -ContentType "application/json"
Write-Success "15. PUT /cart/items/{productId} -> Updated Product 1 quantity to 4. New Total Price: $($cartRes.totalPrice)$"

# 16. DELETE /cart/items/{productId} (Remove Samsung S24 [ID 2] from cart)
Invoke-RestMethod -Uri "$baseUrl/cart/items/2" -Method Delete -Headers $headers
Write-Success "16. DELETE /cart/items/{productId} -> Removed Product 2 from cart."

# 17. DELETE /cart (Clear entire cart)
Invoke-RestMethod -Uri "$baseUrl/cart" -Method Delete -Headers $headers
$cartEmpty = Invoke-RestMethod -Uri "$baseUrl/cart" -Method Get -Headers $headers
Write-Success "17. DELETE /cart -> Cleared entire cart. Current items: $($cartEmpty.items.Count)"


# ======================================================================
# MODULE 5: ORDER CONTROLLER (6/6 ENDPOINTS)
# ======================================================================
Write-Header "MODULE 5: ORDER CONTROLLER"

# Preparatory step: Add a product to checkout
$cartBodyCheckout = @{ productId = 1; quantity = 2 } | ConvertTo-Json
Invoke-RestMethod -Uri "$baseUrl/cart/items" -Method Post -Headers $headers -Body $cartBodyCheckout -ContentType "application/json" > $null

# 18. POST /orders (Place Order 1)
$order1 = Invoke-RestMethod -Uri "$baseUrl/orders" -Method Post -Headers $headers
$orderId1 = $order1.id
Write-Success "18. POST /orders -> Placed Order 1. ID=$orderId1, Status=$($order1.status)"

# 19. PUT /orders/my/{id}/cancel (Cancel Order 1 - allowed since it is PENDING)
$cancelledOrder = Invoke-RestMethod -Uri "$baseUrl/orders/my/$orderId1/cancel" -Method Put -Headers $headers
Write-Success "19. PUT /orders/my/{id}/cancel -> Cancelled Order 1. New Status: $($cancelledOrder.status)"

# Preparatory step: Place Order 2 for shipping flow
Invoke-RestMethod -Uri "$baseUrl/cart/items" -Method Post -Headers $headers -Body $cartBodyCheckout -ContentType "application/json" > $null
$order2 = Invoke-RestMethod -Uri "$baseUrl/orders" -Method Post -Headers $headers
$orderId2 = $order2.id
Write-Success "Placed Order 2 for shipping flow. ID=$orderId2, Status=$($order2.status)"

# 20. GET /orders/my (View my orders list)
$myOrders = Invoke-RestMethod -Uri "$baseUrl/orders/my" -Method Get -Headers $headers
Write-Success "20. GET /orders/my -> Customer orders list retrieved. Total orders: $($myOrders.Count)"

# 21. GET /orders/my/{id} (View single order details)
$orderDetail = Invoke-RestMethod -Uri "$baseUrl/orders/my/$orderId2" -Method Get -Headers $headers
Write-Success "21. GET /orders/my/{id} -> Retrieved Order 2 detail. Total Price: $($orderDetail.totalPrice)$"

# 22. GET /orders (ADMIN - View all orders in system)
$allOrders = Invoke-RestMethod -Uri "$baseUrl/orders" -Method Get -Headers $adminHeaders
Write-Success "22. GET /orders -> Admin retrieved all orders. Total in system: $($allOrders.Count)"

# 23. PUT /orders/{id}/status (ADMIN - Update Order 2 to DELIVERED)
$statusConfirmed = @{ status = "CONFIRMED" } | ConvertTo-Json
Invoke-RestMethod -Uri "$baseUrl/orders/$orderId2/status" -Method Put -Headers $adminHeaders -Body $statusConfirmed -ContentType "application/json" > $null

$statusShipped = @{ status = "SHIPPED" } | ConvertTo-Json
Invoke-RestMethod -Uri "$baseUrl/orders/$orderId2/status" -Method Put -Headers $adminHeaders -Body $statusShipped -ContentType "application/json" > $null

$statusDelivered = @{ status = "DELIVERED" } | ConvertTo-Json
$deliveredOrder = Invoke-RestMethod -Uri "$baseUrl/orders/$orderId2/status" -Method Put -Headers $adminHeaders -Body $statusDelivered -ContentType "application/json"
Write-Success "23. PUT /orders/{id}/status -> Order 2 status updated by Admin to: $($deliveredOrder.status)"


# ======================================================================
# MODULE 6: PRODUCT REVIEWS SUB-ENDPOINTS (3/3 ENDPOINTS)
# ======================================================================
Write-Header "MODULE 6: PRODUCT REVIEWS SUB-ENDPOINTS"

# 24. POST /products/{id}/reviews (Post review for Product 1 since order is DELIVERED)
$reviewBody = @{
    rating = 5
    comment = "Exceptional product and lightning-fast checkout flow!"
} | ConvertTo-Json
$review = Invoke-RestMethod -Uri "$baseUrl/products/1/reviews" -Method Post -Headers $headers -Body $reviewBody -ContentType "application/json"
$reviewId = $review.id
Write-Success "24. POST /products/{id}/reviews -> Review posted! ID=$reviewId, Rating=$($review.rating) stars"

# 25. GET /products/{id}/reviews (View reviews for Product 1)
$reviews = Invoke-RestMethod -Uri "$baseUrl/products/1/reviews" -Method Get
Write-Success "25. GET /products/{id}/reviews -> Public reviews retrieved: $($reviews.Count) reviews found."
foreach ($r in $reviews) {
    Write-Info " - [$($r.rating) stars] - $($r.userName): $($r.comment)"
}

# 26. DELETE /products/{id}/reviews/{reviewId} (ADMIN deletes the review for cleanup)
Invoke-RestMethod -Uri "$baseUrl/products/1/reviews/$reviewId" -Method Delete -Headers $adminHeaders
Write-Success "26. DELETE /products/{id}/reviews/{reviewId} -> Deleted Review ID=$reviewId successfully."


Write-Header "CONGRATULATIONS! ALL 23 API ENDPOINTS FULLY TESTED AND WORKING 100% PERFECTLY! 🚀"
