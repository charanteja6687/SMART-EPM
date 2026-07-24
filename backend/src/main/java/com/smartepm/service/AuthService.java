package com.smartepm.service;

import com.smartepm.dto.request.ForgotPasswordRequest;
import com.smartepm.dto.request.LoginRequest;
import com.smartepm.dto.request.RegisterRequest;
import com.smartepm.dto.request.ResetPasswordRequest;
import com.smartepm.dto.request.VerifyOtpRequest;
import com.smartepm.dto.response.JwtResponse;

public interface AuthService {
    JwtResponse login(LoginRequest request);
    JwtResponse register(RegisterRequest request);

    /** Generates a 6-digit OTP, valid for 5 minutes, and emails it to the given address. */
    void forgotPassword(ForgotPasswordRequest request);

    /** Checks the OTP is correct and not expired, without consuming it (used for the frontend's step-2 check). */
    void verifyOtp(VerifyOtpRequest request);

    /** Re-validates the OTP and, if valid, updates the password and clears the OTP. */
    void resetPassword(ResetPasswordRequest request);
}
