package com.thai2004z.ecommerce_api.service;

import com.thai2004z.ecommerce_api.dto.request.ReviewRequest;
import com.thai2004z.ecommerce_api.dto.response.ReviewResponse;
import com.thai2004z.ecommerce_api.entity.*;
import com.thai2004z.ecommerce_api.exception.BusinessException;
import com.thai2004z.ecommerce_api.exception.ResourceNotFoundException;
import com.thai2004z.ecommerce_api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviews(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found: " + productId);
        }
        return reviewRepository.findByProductId(productId).stream()
                .map(ReviewResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReviewResponse createReview(Long productId, String email, ReviewRequest req) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        // Validate: chỉ được review nếu có order DELIVERED chứa sản phẩm này
        boolean hasPurchased = orderRepository.existsByUserIdAndProductIdAndStatus(
                user.getId(), productId, OrderStatus.DELIVERED
        );
        if (!hasPurchased) {
            throw new BusinessException("Bạn chỉ có thể review sản phẩm đã mua và đã được giao hàng");
        }

        // Không cho review 2 lần
        if (reviewRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            throw new BusinessException("Bạn đã review sản phẩm này rồi");
        }

        Review review = Review.builder()
                .user(user)
                .product(product)
                .rating(req.getRating())
                .comment(req.getComment())
                .build();

        return ReviewResponse.from(reviewRepository.save(review));
    }

    @Transactional
    public void deleteReview(Long productId, Long reviewId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        Review review = reviewRepository.findByIdAndProductId(reviewId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found: " + reviewId));

        // Chỉ cho xóa nếu là chủ review hoặc ADMIN
        boolean isOwner = review.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new BusinessException("Bạn không có quyền xóa review này");
        }

        reviewRepository.delete(review);
    }
}
