# Automated Integration Test Script for E-Commerce API
# Running business flow: Register -> Login -> View Products -> Add Cart -> Place Order -> Admin Update Status -> User Review Product

$baseUrl = "http://localhost:8080"
$ErrorActionPreference = "Stop"

# Helpers
function Write-Header($text) {
    Write-Host "`n==================================================" -ForegroundColor Cyan
    Write-Host ">>> $text" -ForegroundColor Cyan
    Write-Host "==================================================" -ForegroundColor Cyan
}

function Write-Success($text) {
    Write-Host "[SUCCESS] $text" -ForegroundColor Green
}

function Write-Info($text) {
    Write-Host "[INFO] $text" -ForegroundColor DarkYellow
}

# --------------------------------------------------
# STEP 1: REGISTER NEW CUSTOMER
# --------------------------------------------------
Write-Header "STEP 1: REGISTER NEW CUSTOMER ACCOUNT"
$rand = Get-Random -Minimum 1000 -Maximum 9999
$customerEmail = "buyer_$rand@example.com"
$customerPassword = "password123"

$registerBody = @{
    name = "Customer $rand"
    email = $customerEmail
    password = $customerPassword
} | ConvertTo-Json

$registerRes = Invoke-RestMethod -Uri "$baseUrl/auth/register" -Method Post -Body $registerBody -ContentType "application/json"
Write-Success "Registered successfully: $($registerRes.name) ($($registerRes.email))"

# --------------------------------------------------
# STEP 2: LOGIN NEW CUSTOMER
# --------------------------------------------------
Write-Header "STEP 2: LOGIN NEW CUSTOMER & GET JWT TOKEN"
$loginBody = @{
    email = $customerEmail
    password = $customerPassword
} | ConvertTo-Json

$loginRes = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
$customerToken = $loginRes.token
Write-Success "Login success! Token (excerpt): $($customerToken.Substring(0, 20))..."

# --------------------------------------------------
# STEP 3: GET CUSTOMER PROFILE (AUTH ME)
# --------------------------------------------------
Write-Header "STEP 3: GET CUSTOMER PROFILE (AUTH ME)"
$headers = @{
    Authorization = "Bearer $customerToken"
}
$meRes = Invoke-RestMethod -Uri "$baseUrl/auth/me" -Method Get -Headers $headers
Write-Success "Current user: ID=$($meRes.id), Name=$($meRes.name), Role=$($meRes.role)"

# --------------------------------------------------
# STEP 4: GET CATEGORIES & PRODUCTS (PUBLIC)
# --------------------------------------------------
Write-Header "STEP 4: GET CATEGORIES & PRODUCTS (PUBLIC)"
$categories = Invoke-RestMethod -Uri "$baseUrl/categories" -Method Get
Write-Success "Categories retrieved: $($categories.Count) categories found."
foreach ($cat in $categories) {
    Write-Info " - ID=$($cat.id): $($cat.name)"
}

$productsRes = Invoke-RestMethod -Uri "$baseUrl/products?page=0&size=5" -Method Get
$products = $productsRes.content
Write-Success "Products retrieved: $($products.Count) products found on page 0."
$firstProduct = $products[0]
foreach ($p in $products) {
    Write-Info " - ID=$($p.id): $($p.name) - Price: $($p.price)$ - Stock: $($p.stock)"
}

# --------------------------------------------------
# STEP 5: ADD PRODUCT TO CART
# --------------------------------------------------
Write-Header "STEP 5: ADD PRODUCT TO CART"
Write-Info "Adding first product (ID=$($firstProduct.id): $($firstProduct.name)) with quantity 2 to cart..."

$cartBody = @{
    productId = $firstProduct.id
    quantity = 2
} | ConvertTo-Json

$cartRes = Invoke-RestMethod -Uri "$baseUrl/cart/items" -Method Post -Headers $headers -Body $cartBody -ContentType "application/json"
Write-Success "Cart updated! Total cart price: $($cartRes.totalPrice)$"
foreach ($item in $cartRes.items) {
    Write-Info " - Item: $($item.productName) x $($item.quantity) = $($item.subTotal)$"
}

# --------------------------------------------------
# STEP 6: PLACE ORDER (CHECKOUT)
# --------------------------------------------------
Write-Header "STEP 6: PLACE ORDER (CHECKOUT)"
$orderRes = Invoke-RestMethod -Uri "$baseUrl/orders" -Method Post -Headers $headers
$orderId = $orderRes.id
Write-Success "Order placed successfully! Order ID=$orderId created."
Write-Info " - Initial status: $($orderRes.status)"
Write-Info " - Order total price: $($orderRes.totalPrice)$"

# --------------------------------------------------
# STEP 7: VIEW MY ORDERS HISTORY
# --------------------------------------------------
Write-Header "STEP 7: VIEW MY ORDERS HISTORY"
$myOrders = Invoke-RestMethod -Uri "$baseUrl/orders/my" -Method Get -Headers $headers
Write-Success "Orders history retrieved! Total orders: $($myOrders.Count)"
Write-Info " - Latest order: ID=$($myOrders[0].id), Status=$($myOrders[0].status)"

# --------------------------------------------------
# STEP 8: LOGIN AS ADMIN
# --------------------------------------------------
Write-Header "STEP 8: LOGIN AS ADMIN"
$adminLoginBody = @{
    email = "admin@example.com"
    password = "admin123"
} | ConvertTo-Json

$adminLoginRes = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $adminLoginBody -ContentType "application/json"
$adminToken = $adminLoginRes.token
$adminHeaders = @{
    Authorization = "Bearer $adminToken"
}
Write-Success "Admin login success!"

# --------------------------------------------------
# STEP 9: ADMIN UPDATE ORDER STATUS TO DELIVERED
# --------------------------------------------------
Write-Header "STEP 9: ADMIN UPDATE ORDER STATUS TO DELIVERED"
Write-Info "Updating order ID=$orderId to CONFIRMED..."
$statusConfirmed = @{ status = "CONFIRMED" } | ConvertTo-Json
$confirmRes = Invoke-RestMethod -Uri "$baseUrl/orders/$orderId/status" -Method Put -Headers $adminHeaders -Body $statusConfirmed -ContentType "application/json"

Write-Info "Updating order ID=$orderId to SHIPPED..."
$statusShipped = @{ status = "SHIPPED" } | ConvertTo-Json
$shippedRes = Invoke-RestMethod -Uri "$baseUrl/orders/$orderId/status" -Method Put -Headers $adminHeaders -Body $statusShipped -ContentType "application/json"

Write-Info "Updating order ID=$orderId to DELIVERED..."
$statusDelivered = @{ status = "DELIVERED" } | ConvertTo-Json
$deliveredRes = Invoke-RestMethod -Uri "$baseUrl/orders/$orderId/status" -Method Put -Headers $adminHeaders -Body $statusDelivered -ContentType "application/json"
Write-Success "Order ID=$orderId status updated to: $($deliveredRes.status)"

# --------------------------------------------------
# STEP 10: USER POST REVIEW FOR THE PRODUCT
# --------------------------------------------------
Write-Header "STEP 10: USER POST REVIEW FOR THE PRODUCT"
Write-Info "Posting a 5-star review for product ID $($firstProduct.id)..."
$reviewBody = @{
    rating = 5
    comment = "Excellent quality product! Super fast shipping!"
} | ConvertTo-Json

$reviewRes = Invoke-RestMethod -Uri "$baseUrl/products/$($firstProduct.id)/reviews" -Method Post -Headers $headers -Body $reviewBody -ContentType "application/json"
Write-Success "Review submitted successfully!"
Write-Info " - Review ID: $($reviewRes.id)"
Write-Info " - Reviewer: $($reviewRes.userName)"
Write-Info " - Rating: $($reviewRes.rating) stars"
Write-Info " - Comment: $($reviewRes.comment)"

# --------------------------------------------------
# STEP 11: GET ALL REVIEWS OF PRODUCT (PUBLIC)
# --------------------------------------------------
Write-Header "STEP 11: GET ALL REVIEWS OF PRODUCT (PUBLIC)"
$reviewsList = Invoke-RestMethod -Uri "$baseUrl/products/$($firstProduct.id)/reviews" -Method Get
Write-Success "Reviews retrieved successfully!"
foreach ($r in $reviewsList) {
    Write-Info " - [$($r.rating) stars] - $($r.userName): $($r.comment)"
}

Write-Header "ALL API ENDPOINTS TESTED SUCCESSFULLY AND RUNNING PERFECTLY!"
