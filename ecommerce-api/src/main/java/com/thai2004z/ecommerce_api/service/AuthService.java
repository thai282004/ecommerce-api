package com.thai2004z.ecommerce_api.service;

import com.thai2004z.ecommerce_api.dto.request.LoginRequest;
import com.thai2004z.ecommerce_api.dto.request.RegisterRequest;
import com.thai2004z.ecommerce_api.dto.response.AuthResponse;
import com.thai2004z.ecommerce_api.dto.response.UserResponse;
import com.thai2004z.ecommerce_api.entity.Cart;
import com.thai2004z.ecommerce_api.entity.Role;
import com.thai2004z.ecommerce_api.entity.User;
import com.thai2004z.ecommerce_api.exception.BusinessException;
import com.thai2004z.ecommerce_api.exception.ResourceNotFoundException;
import com.thai2004z.ecommerce_api.repository.CartRepository;
import com.thai2004z.ecommerce_api.repository.UserRepository;
import com.thai2004z.ecommerce_api.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.thai2004z.ecommerce_api.security.JwtUtil;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    public UserResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BusinessException("Email đã được sử dụng: " + req.getEmail());
        }

        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(Role.USER)
                .build();
        userRepository.save(user);

        // Tạo cart cho user mới
        Cart cart = Cart.builder().user(user).build();
        cartRepository.save(cart);

        return UserResponse.from(user);
    }

    public AuthResponse login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );
        UserDetails userDetails = userDetailsService.loadUserByUsername(req.getEmail());
        String token = jwtUtil.generateToken(userDetails);
        return new AuthResponse(token);
    }

    public UserResponse getMe(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        return UserResponse.from(user);
    }
}
