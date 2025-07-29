package com.sadiqov.loginservice.service;

import com.sadiqov.loginservice.dto.request.UserRequest;
import com.sadiqov.loginservice.entity.OTP;
import com.sadiqov.loginservice.entity.User;
import com.sadiqov.loginservice.repo.UserRepository;
import com.sadiqov.loginservice.util.SmsSender;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final SmsSender smsSender;
    private final PasswordEncoder config;


    public void register(UserRequest request) {

        if (request.getPhone().length() != 10) {
            throw new RuntimeException("Telefon nomresi 10 reqemden ibaret olmalidir ❗");
        } else if (userRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new RuntimeException("Bu telefon nömrəsi artıq qeydiyyatdan keçib");
        }


        User user = new User();
        user.setPhone(request.getPhone());
        user.setPassword(config.encode(request.getPassword()));
        userRepository.save(user);
    }


    public Map<String, Object> login(String phoneNumber, String password) {
        User user = userRepository.findByPhone(phoneNumber)
                .orElseThrow(() -> new RuntimeException("İstifadəçi tapılmadı"));

        if (user.getOtp() != null &&
                user.getOtp().isOtpBlocked() &&
                user.getOtp().getBlockTime().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Hesabınız müvəqqəti bloklanıb. Zəhmət olmasa " +
                    user.getOtp().getBlockTime().plusMinutes(5).until(LocalDateTime.now(), ChronoUnit.MINUTES) +
                    " dəqiqə sonra yenidən cəhd edin");
        }

        if (user.isAccountLocked()) {
            if (user.getAccountLockedUntil().isAfter(LocalDateTime.now())) {
                long secs = ChronoUnit.SECONDS.between(LocalDateTime.now(), user.getAccountLockedUntil());
                throw new RuntimeException("Hesab bloklanıb. " + "\r"+secs + " saniyə sonra yenidən cəhd edin");

            }                resetFailedAttempts(user);

        }

        if (!config.matches(password, user.getPassword())) {
                handleFailedPasswordAttempt(user);
                throw new RuntimeException("Yanlış şifrə. Qalan cəhd sayı: " + (3 - user.getFailedPasswordAttempts()));
            }

            String generatedOtp = generateOtp();
            smsSender.smsSender(phoneNumber, "Sizin təsdiq kodunuz: " + generatedOtp);

            OTP otp = user.getOtp() != null ? user.getOtp() : new OTP();

            otp.setOtpCode(generatedOtp);
            otp.setOtpRequestCount(0);
            otp.setLastOtpRequest(LocalDateTime.now());
            otp.setUser(user);
            user.setOtp(otp);
            userRepository.save(user);

            String verifyLink = "http://localhost:8081/api/user/verify-otp?phone=" + phoneNumber + "&otp=" + generatedOtp;

            return Map.of(
                    "message", "OTP kodunuz telefon nömrənizə göndərildi",
                    "verifyLink", verifyLink,
                    "expiresIn", "5 dəqiqə"
            );

        }

        public boolean verifyOtpAndLogin (String phoneNumber, String otpCode){
            User user = userRepository.findByPhone(phoneNumber)
                    .orElseThrow(() -> new RuntimeException("İstifadəçi tapılmadı"));

            if (user.getOtp() == null) {
                throw new RuntimeException("OTP yaradılmayıb");
            }

            if (user.getOtp().isOtpBlocked() &&
                    user.getOtp().getBlockTime().isAfter(LocalDateTime.now())) {
                throw new RuntimeException("OTP bloklanıb. Zəhmət olmasa bir qədər sonra yenidən cəhd edin");
            }

            if (user.getOtp().getLastOtpRequest().plusMinutes(5).isBefore(LocalDateTime.now())) {
                throw new RuntimeException("OTP-nin vaxtı bitib");
            }

            if (!user.getOtp().getOtpCode().equals(otpCode)) {
                handleFailedLoginAttempt(user);
                return false;
            }

            user.getOtp().setOtpRequestCount(0);
            user.getOtp().setOtpBlocked(false);
            userRepository.save(user);

            return true;
        }
        private void handleFailedLoginAttempt (User user){
            OTP otp = user.getOtp();
            otp.setOtpRequestCount(otp.getOtpRequestCount() + 1);

            if (otp.getOtpRequestCount() >= 3) {
                otp.setBlockTime(LocalDateTime.now().plusSeconds(30));
                otp.setOtpBlocked(true);
            }

            userRepository.save(user);
        }

        private String generateOtp () {
            return String.valueOf((int) (Math.random() * 900000) + 100000);
        }

        private void handleFailedPasswordAttempt (User user){
            user.setFailedPasswordAttempts(user.getFailedPasswordAttempts() + 1);

            if (user.getFailedPasswordAttempts() >= 3) {
                user.setAccountLockedUntil(LocalDateTime.now().plusSeconds(30));
                user.setAccountLocked(true);
            }

            userRepository.save(user);
        }

        private void resetFailedAttempts (User user){
            user.setFailedPasswordAttempts(0);
            user.setAccountLocked(false);
            user.setAccountLockedUntil(null);
            userRepository.save(user);
        }
    }
