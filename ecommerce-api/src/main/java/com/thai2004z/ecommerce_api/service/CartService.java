package com.thai2004z.ecommerce_api.service;

import com.thai2004z.ecommerce_api.dto.request.CartItemRequest;
import com.thai2004z.ecommerce_api.dto.response.CartResponse;
import com.thai2004z.ecommerce_api.entity.Cart;
import com.thai2004z.ecommerce_api.entity.CartItem;
import com.thai2004z.ecommerce_api.entity.Product;
import com.thai2004z.ecommerce_api.entity.User;
import com.thai2004z.ecommerce_api.exception.BusinessException;
import com.thai2004z.ecommerce_api.exception.ResourceNotFoundException;
import com.thai2004z.ecommerce_api.repository.CartItemRepository;
import com.thai2004z.ecommerce_api.repository.CartRepository;
import com.thai2004z.ecommerce_api.repository.ProductRepository;
import com.thai2004z.ecommerce_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private Cart getCartByEmail(String email) {
        User user = getUserByEmail(email);
        return cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + email));
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(String email) {
        Cart cart = getCartByEmail(email);
        return CartResponse.from(cart);
    }

    @Transactional
    public CartResponse addItem(String email, CartItemRequest req) {
        Cart cart = getCartByEmail(email);

        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + req.getProductId()));

        // Nếu sản phẩm đã trong cart, cộng thêm quantity
        Optional<CartItem> existing = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), product.getId());

        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + req.getQuantity());
            cartItemRepository.save(item);
        } else {
            CartItem item = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(req.getQuantity())
                    .build();
            cartItemRepository.save(item);
        }

        // Reload cart để lấy items mới nhất
        cart = getCartByEmail(email);
        return CartResponse.from(cart);
    }

    @Transactional
    public CartResponse updateItem(String email, Long productId, CartItemRequest req) {
        Cart cart = getCartByEmail(email);

        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không có trong giỏ hàng"));

        if (req.getQuantity() <= 0) {
            throw new BusinessException("Số lượng phải ít nhất là 1");
        }

        item.setQuantity(req.getQuantity());
        cartItemRepository.save(item);

        cart = getCartByEmail(email);
        return CartResponse.from(cart);
    }

    @Transactional
    public void removeItem(String email, Long productId) {
        Cart cart = getCartByEmail(email);

        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không có trong giỏ hàng"));

        cartItemRepository.delete(item);
    }

    @Transactional
    public void clearCart(String email) {
        Cart cart = getCartByEmail(email);
        cart.getItems().clear();
        cartRepository.save(cart);
    }
}
