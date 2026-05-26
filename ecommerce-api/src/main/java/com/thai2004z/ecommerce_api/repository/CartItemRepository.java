package com.thai2004z.ecommerce_api.repository;

import com.thai2004z.ecommerce_api.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // Tìm item trong cart theo productId — dùng để kiểm tra đã có trong giỏ chưa
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    // Xóa toàn bộ item trong cart — dùng khi clear cart hoặc sau khi đặt hàng
    @Modifying
    @Transactional
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId")
    void deleteByCartId(@Param("cartId") Long cartId);
}
