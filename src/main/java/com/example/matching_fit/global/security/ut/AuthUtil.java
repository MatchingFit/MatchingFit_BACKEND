package com.example.matching_fit.global.security.ut;

public class AuthUtil {
    public static String generateRandomCode() {
        return String.valueOf((int)(Math.random() * 899999) + 100000); // 6자리 숫자
    }
}