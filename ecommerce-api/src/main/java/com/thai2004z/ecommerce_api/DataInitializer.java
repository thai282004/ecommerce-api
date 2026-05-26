package com.thai2004z.ecommerce_api;

import com.thai2004z.ecommerce_api.entity.*;
import com.thai2004z.ecommerce_api.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Seed data khi khởi động lần đầu.
 * Tạo: 1 admin, 1 user, 3 categories, 5 products.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            log.info("Database đã có dữ liệu, bỏ qua seed.");
            return;
        }

        log.info("Bắt đầu seed dữ liệu mẫu...");

        // Tạo admin
        User admin = User.builder()
                .name("Admin")
                .email("admin@example.com")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .build();
        userRepository.save(admin);
        log.info("Tạo admin: admin@example.com / admin123");

        // Tạo user thường + cart
        User user = User.builder()
                .name("Test User")
                .email("user@example.com")
                .password(passwordEncoder.encode("user123"))
                .role(Role.USER)
                .build();
        userRepository.save(user);
        cartRepository.save(Cart.builder().user(user).build());
        log.info("Tạo user: user@example.com / user123");

        // Tạo categories
        Category electronics = categoryRepository.save(
                Category.builder().name("Electronics").build());
        Category clothing = categoryRepository.save(
                Category.builder().name("Clothing").build());
        Category books = categoryRepository.save(
                Category.builder().name("Books").build());

        // Tạo products
        productRepository.save(Product.builder()
                .name("iPhone 15")
                .description("Apple iPhone 15, 128GB, chip A16 Bionic")
                .price(new BigDecimal("999.99"))
                .stock(50)
                .category(electronics)
                .build());

        productRepository.save(Product.builder()
                .name("Samsung Galaxy S24")
                .description("Samsung Galaxy S24, 256GB, Snapdragon 8 Gen 3")
                .price(new BigDecimal("799.99"))
                .stock(30)
                .category(electronics)
                .build());

        productRepository.save(Product.builder()
                .name("ASUS ROG Strix G15")
                .description("Laptop Gaming ASUS ROG Strix G15, RTX 4060, 16GB RAM")
                .price(new BigDecimal("1299.99"))
                .stock(15)
                .category(electronics)
                .build());

        productRepository.save(Product.builder()
                .name("Áo thun unisex cotton")
                .description("Áo thun cotton 100%, form rộng, nhiều màu")
                .price(new BigDecimal("19.99"))
                .stock(200)
                .category(clothing)
                .build());

        productRepository.save(Product.builder()
                .name("Clean Code - Robert C. Martin")
                .description("A Handbook of Agile Software Craftsmanship — sách kinh điển cho developer")
                .price(new BigDecimal("39.99"))
                .stock(100)
                .category(books)
                .build());

        log.info("Seed xong: 2 users, 3 categories, 5 products.");
    }
}
