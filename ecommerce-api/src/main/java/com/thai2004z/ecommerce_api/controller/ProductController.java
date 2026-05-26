package com.thai2004z.ecommerce_api.controller;

import com.thai2004z.ecommerce_api.dto.request.ProductRequest;
import com.thai2004z.ecommerce_api.dto.request.ReviewRequest;
import com.thai2004z.ecommerce_api.dto.response.ProductResponse;
import com.thai2004z.ecommerce_api.dto.response.ReviewResponse;
import com.thai2004z.ecommerce_api.service.ProductService;
import com.thai2004z.ecommerce_api.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "Product", description = "Sản phẩm và reviews")
public class ProductController {

    private final ProductService productService;
    private final ReviewService reviewService;

    // ==================== Product endpoints ====================

    @GetMapping
    @Operation(summary = "Danh sách sản phẩm với pagination + filter (public)")
    public ResponseEntity<Page<ProductResponse>> findAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                productService.findAll(keyword, categoryId, minPrice, maxPrice, page, size)
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết sản phẩm + avg rating (public)")
    public ResponseEntity<ProductResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Tạo sản phẩm (ADMIN)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(req));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật sản phẩm (ADMIN)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ProductResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody ProductRequest req) {
        return ResponseEntity.ok(productService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa sản phẩm (ADMIN)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== Review sub-endpoints ====================

    @GetMapping("/{id}/reviews")
    @Operation(summary = "Xem reviews của sản phẩm (public)")
    public ResponseEntity<List<ReviewResponse>> getReviews(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getReviews(id));
    }

    @PostMapping("/{id}/reviews")
    @Operation(summary = "Viết review — chỉ khi đã mua và DELIVERED",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ReviewResponse> createReview(@PathVariable Long id,
                                                        @Valid @RequestBody ReviewRequest req,
                                                        Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.createReview(id, authentication.getName(), req));
    }

    @DeleteMapping("/{id}/reviews/{reviewId}")
    @Operation(summary = "Xóa review (chủ review hoặc ADMIN)",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteReview(@PathVariable Long id,
                                              @PathVariable Long reviewId,
                                              Authentication authentication) {
        reviewService.deleteReview(id, reviewId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
