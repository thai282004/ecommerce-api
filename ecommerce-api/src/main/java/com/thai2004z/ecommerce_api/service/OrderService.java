package com.thai2004z.ecommerce_api.service;

import com.thai2004z.ecommerce_api.dto.request.OrderStatusRequest;
import com.thai2004z.ecommerce_api.dto.response.OrderResponse;
import com.thai2004z.ecommerce_api.entity.*;
import com.thai2004z.ecommerce_api.exception.BusinessException;
import com.thai2004z.ecommerce_api.exception.ResourceNotFoundException;
import com.thai2004z.ecommerce_api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    /**
     * Đặt hàng từ cart:
     * 1. Validate stock trước
     * 2. Tạo Order + OrderItems (snapshot giá)
     * 3. Trừ stock
     * 4. Clear cart
     * → @Transactional đảm bảo rollback toàn bộ nếu bất kỳ bước nào fail
     */
    @Transactional
    public OrderResponse placeOrder(String email) {
        User user = getUserByEmail(email);

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new BusinessException("Giỏ hàng trống, không thể đặt hàng");
        }

        // 1. Validate stock TRƯỚC khi làm gì cả
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            if (product.getStock() < item.getQuantity()) {
                throw new BusinessException(
                        "Sản phẩm '" + product.getName() + "' không đủ hàng. Còn lại: " + product.getStock()
                );
            }
        }

        // 2. Tạo Order
        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .build();
        order = orderRepository.save(order);

        // 3. Tạo OrderItems + trừ stock + snapshot giá
        List<CartItem> cartItems = new ArrayList<>(cart.getItems());
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();

            // Snapshot giá tại thời điểm đặt hàng — quan trọng, không lấy sau này
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .price(product.getPrice())
                    .build();
            order.getItems().add(orderItem);

            // Trừ stock — @Version trên Product xử lý optimistic locking
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);
        }

        // 4. Clear cart
        cart.getItems().clear();
        cartRepository.save(cart);

        order = orderRepository.save(order);
        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(String email) {
        User user = getUserByEmail(email);
        return orderRepository.findByUserId(user.getId()).stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResponse getMyOrder(String email, Long orderId) {
        User user = getUserByEmail(email);
        Order order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse cancelOrder(String email, Long orderId) {
        User user = getUserByEmail(email);
        Order order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("Chỉ có thể hủy order ở trạng thái PENDING");
        }

        // Hoàn trả stock khi hủy
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(OrderStatus.CANCELLED);
        return OrderResponse.from(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, OrderStatusRequest req) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        validateStatusTransition(order.getStatus(), req.getStatus());
        order.setStatus(req.getStatus());
        return OrderResponse.from(orderRepository.save(order));
    }

    // Validate luồng status: PENDING → CONFIRMED → SHIPPED → DELIVERED
    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        if (current == OrderStatus.CANCELLED || current == OrderStatus.DELIVERED) {
            throw new BusinessException("Không thể thay đổi status của order đã " + current);
        }

        Map<OrderStatus, OrderStatus> validNext = Map.of(
                OrderStatus.PENDING, OrderStatus.CONFIRMED,
                OrderStatus.CONFIRMED, OrderStatus.SHIPPED,
                OrderStatus.SHIPPED, OrderStatus.DELIVERED
        );

        if (!next.equals(validNext.get(current))) {
            throw new BusinessException(
                    "Chuyển status không hợp lệ: " + current + " → " + next +
                    ". Hợp lệ: " + current + " → " + validNext.get(current)
            );
        }
    }
}
