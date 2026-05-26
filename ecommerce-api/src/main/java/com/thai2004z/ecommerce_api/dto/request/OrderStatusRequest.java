package com.thai2004z.ecommerce_api.dto.request;

import com.thai2004z.ecommerce_api.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderStatusRequest {

    @NotNull(message = "Status không được để trống")
    private OrderStatus status;
}
