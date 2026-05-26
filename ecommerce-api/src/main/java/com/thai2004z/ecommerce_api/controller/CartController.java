package com.thai2004z.ecommerce_api.controller;

import com.thai2004z.ecommerce_api.dto.request.CartItemRequest;
import com.thai2004z.ecommerce_api.dto.response.CartResponse;
import com.thai2004z.ecommerce_api.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Quản lý giỏ hàng")
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Xem cart hiện tại")
    public ResponseEntity<CartResponse> getCart(Authentication authentication) {
        return ResponseEntity.ok(cartService.getCart(authentication.getName()));
    }

    @PostMapping("/items")
    @Operation(summary = "Thêm sản phẩm vào cart")
    public ResponseEntity<CartResponse> addItem(@Valid @RequestBody CartItemRequest req,
                                                 Authentication authentication) {
        return ResponseEntity.ok(cartService.addItem(authentication.getName(), req));
    }

    @PutMapping("/items/{productId}")
    @Operation(summary = "Cập nhật số lượng sản phẩm trong cart")
    public ResponseEntity<CartResponse> updateItem(@PathVariable Long productId,
                                                    @Valid @RequestBody CartItemRequest req,
                                                    Authentication authentication) {
        return ResponseEntity.ok(cartService.updateItem(authentication.getName(), productId, req));
    }

    @DeleteMapping("/items/{productId}")
    @Operation(summary = "Xóa một sản phẩm khỏi cart")
    public ResponseEntity<Void> removeItem(@PathVariable Long productId,
                                            Authentication authentication) {
        cartService.removeItem(authentication.getName(), productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @Operation(summary = "Xóa toàn bộ cart")
    public ResponseEntity<Void> clearCart(Authentication authentication) {
        cartService.clearCart(authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
