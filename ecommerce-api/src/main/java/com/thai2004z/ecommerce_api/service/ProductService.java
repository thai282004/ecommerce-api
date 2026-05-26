package com.thai2004z.ecommerce_api.service;

import com.thai2004z.ecommerce_api.dto.request.ProductRequest;
import com.thai2004z.ecommerce_api.dto.response.ProductResponse;
import com.thai2004z.ecommerce_api.entity.Category;
import com.thai2004z.ecommerce_api.entity.Product;
import com.thai2004z.ecommerce_api.exception.ResourceNotFoundException;
import com.thai2004z.ecommerce_api.repository.CategoryRepository;
import com.thai2004z.ecommerce_api.repository.ProductRepository;
import com.thai2004z.ecommerce_api.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;

    // @Transactional(readOnly=true) giữ session mở — tránh LazyInitializationException
    @Transactional(readOnly = true)
    public Page<ProductResponse> findAll(String keyword, Long categoryId,
                                         BigDecimal minPrice, BigDecimal maxPrice,
                                         int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findWithFilters(keyword, categoryId, minPrice, maxPrice, pageable)
                .map(ProductResponse::from);
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        Product product = productRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        Double avgRating = reviewRepository.findAverageRatingByProductId(id);
        return ProductResponse.from(product, avgRating);
    }

    public ProductResponse create(ProductRequest req) {
        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + req.getCategoryId()));

        Product product = Product.builder()
                .name(req.getName())
                .description(req.getDescription())
                .price(req.getPrice())
                .stock(req.getStock())
                .category(category)
                .build();
        return ProductResponse.from(productRepository.save(product));
    }

    public ProductResponse update(Long id, ProductRequest req) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));

        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + req.getCategoryId()));

        product.setName(req.getName());
        product.setDescription(req.getDescription());
        product.setPrice(req.getPrice());
        product.setStock(req.getStock());
        product.setCategory(category);

        return ProductResponse.from(productRepository.save(product));
    }

    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found: " + id);
        }
        productRepository.deleteById(id);
    }
}
