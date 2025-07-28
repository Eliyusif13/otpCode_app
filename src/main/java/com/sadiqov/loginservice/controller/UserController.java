package com.sadiqov.loginservice.controller;

import com.sadiqov.loginservice.dto.request.UserRequest;
import com.sadiqov.loginservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {


    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserRequest userRequest) {
        userService.register(userRequest);
        return ResponseEntity.ok("User registered successfully.");

    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody UserRequest req) {
        Map<String, Object> result = userService.login(req.getPhone(), req.getPassword());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", result.get("message"),
                "verifyLink", result.get("verifyLink"),
                "expiresIn", result.get("expiresIn")
        ));
    }

    @GetMapping("/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyOtp(
            @RequestParam String phone,
            @RequestParam String otp) {

        boolean isValid = userService.verifyOtpAndLogin(phone, otp);

        if (isValid) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Uğurla daxil oldunuz!"));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Yanlış OTP kodu və ya vaxtı bitib"
            ));
        }
    }
}