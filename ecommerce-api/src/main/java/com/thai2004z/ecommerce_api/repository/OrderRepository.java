package com.thai2004z.ecommerce_api.repository;

import com.thai2004z.ecommerce_api.entity.Order;
import com.thai2004z.ecommerce_api.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);

    Optional<Order> findByIdAndUserId(Long id, Long userId);

    // Kiểm tra user đã mua sản phẩm và đã được giao hàng chưa — để validate review
    @Query("SELECT COUNT(o) > 0 FROM Order o JOIN o.items i " +
           "WHERE o.user.id = :userId AND i.product.id = :productId AND o.status = :status")
    boolean existsByUserIdAndProductIdAndStatus(
            @Param("userId") Long userId,
            @Param("productId") Long productId,
            @Param("status") OrderStatus status);
}
