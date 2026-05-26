package com.thai2004z.ecommerce_api.dto.response;

import com.thai2004z.ecommerce_api.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String categoryName;
    private Double avgRating;
    private LocalDateTime createdAt;

    // Dùng cho danh sách — không có avgRating
    public static ProductResponse from(Product p) {
        return new ProductResponse(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                p.getStock(),
                p.getCategory() != null ? p.getCategory().getName() : null,
                null,
                p.getCreatedAt()
        );
    }

    // Dùng cho chi tiết — có avgRating
    public static ProductResponse from(Product p, Double avgRating) {
        return new ProductResponse(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                p.getStock(),
                p.getCategory() != null ? p.getCategory().getName() : null,
                avgRating,
                p.getCreatedAt()
        );
    }
}
