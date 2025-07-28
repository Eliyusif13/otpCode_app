package com.sadiqov.loginservice.util;

import org.springframework.stereotype.Component;

@Component
public class SmsSender {

    public void smsSender(String phone, String message) {
        System.out.println("📩 SMS to " + phone + ": " + message);

    }
}
