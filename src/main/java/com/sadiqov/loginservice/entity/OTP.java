package com.sadiqov.loginservice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Data
@Table
@FieldDefaults(level = AccessLevel.PRIVATE)

public class OTP {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    int otpRequestCount ;

    LocalDateTime lastOtpRequest;

    LocalDateTime blockTime;

    boolean isOtpBlocked = false;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    User user;

    String otpCode;
}
