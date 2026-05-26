package com.thai2004z.ecommerce_api.dto.response;

import com.thai2004z.ecommerce_api.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class OrderResponse {

    private Long id;
    private String status;
    private List<OrderItemResponse> items;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;

    public static OrderResponse from(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(OrderItemResponse::from)
                .collect(Collectors.toList());

        BigDecimal total = items.stream()
                .map(OrderItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new OrderResponse(
                order.getId(),
                order.getStatus().name(),
                items,
                total,
                order.getCreatedAt()
        );
    }
}
