package com.example.ridesharing.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtils {

    private static BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // Method to hash the password
    public static String hashPassword(String plainPassword) {
        return encoder.encode(plainPassword);
    }

    // Method to check if the provided password matches the hashed password
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return encoder.matches(plainPassword, hashedPassword);
    }
}
