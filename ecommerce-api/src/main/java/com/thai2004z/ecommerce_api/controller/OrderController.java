package com.thai2004z.ecommerce_api.controller;

import com.thai2004z.ecommerce_api.dto.request.OrderStatusRequest;
import com.thai2004z.ecommerce_api.dto.response.OrderResponse;
import com.thai2004z.ecommerce_api.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Order", description = "Quản lý đơn hàng")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    // ==================== User endpoints ====================

    @PostMapping
    @Operation(summary = "Đặt hàng từ cart — trừ stock, clear cart")
    public ResponseEntity<OrderResponse> placeOrder(Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.placeOrder(authentication.getName()));
    }

    @GetMapping("/my")
    @Operation(summary = "Xem lịch sử đơn hàng của mình")
    public ResponseEntity<List<OrderResponse>> getMyOrders(Authentication authentication) {
        return ResponseEntity.ok(orderService.getMyOrders(authentication.getName()));
    }

    @GetMapping("/my/{id}")
    @Operation(summary = "Xem chi tiết một đơn hàng của mình")
    public ResponseEntity<OrderResponse> getMyOrder(@PathVariable Long id,
                                                     Authentication authentication) {
        return ResponseEntity.ok(orderService.getMyOrder(authentication.getName(), id));
    }

    @PutMapping("/my/{id}/cancel")
    @Operation(summary = "Hủy đơn hàng — chỉ khi status PENDING")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long id,
                                                      Authentication authentication) {
        return ResponseEntity.ok(orderService.cancelOrder(authentication.getName(), id));
    }

    // ==================== Admin endpoints ====================

    @GetMapping
    @Operation(summary = "Xem tất cả đơn hàng (ADMIN)")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Cập nhật status đơn hàng (ADMIN): PENDING→CONFIRMED→SHIPPED→DELIVERED")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable Long id,
                                                       @Valid @RequestBody OrderStatusRequest req) {
        return ResponseEntity.ok(orderService.updateStatus(id, req));
    }
}
