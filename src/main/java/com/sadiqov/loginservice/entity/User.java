package com.sadiqov.loginservice.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Table
@Entity
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String phone;

    int failedPasswordAttempts = 0;
    LocalDateTime accountLockedUntil;
    boolean isAccountLocked = false;

    String password;
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    OTP otp;
}