package com.smartepm.service;

public interface EmailService {
    /** Sends a password-reset OTP email. Throws if sending fails (caller decides how to handle). */
    void sendOtpEmail(String toEmail, String otp);
}
