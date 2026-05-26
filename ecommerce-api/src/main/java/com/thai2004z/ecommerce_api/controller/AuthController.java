package com.thai2004z.ecommerce_api.controller;

import com.thai2004z.ecommerce_api.dto.request.LoginRequest;
import com.thai2004z.ecommerce_api.dto.request.RegisterRequest;
import com.thai2004z.ecommerce_api.dto.response.AuthResponse;
import com.thai2004z.ecommerce_api.dto.response.UserResponse;
import com.thai2004z.ecommerce_api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Đăng ký / Đăng nhập / Xem thông tin bản thân")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Đăng ký tài khoản mới")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(req));
    }

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập — trả về JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @GetMapping("/me")
    @Operation(summary = "Xem thông tin tài khoản của mình (cần token)")
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        return ResponseEntity.ok(authService.getMe(authentication.getName()));
    }
}
