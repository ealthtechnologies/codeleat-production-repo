package com.ealth.codeleat.services;

import com.ealth.codeleat.dtos.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    public AuthResponseDto signUp(UserSignUpDto userSignUpDto);
    public AuthResponseDto login(UserLoginDto userLoginDto, HttpServletResponse response);
    public void verifyOtp(OtpVerificationDto otpVerificationDto);
    public void forgotPassword(ForgotPasswordDto forgotPasswordDto);
    public void resetPassword(ResetPasswordDto resetPasswordDto);
    public AuthResponseDto resendOtp(ResendOtpDto resendOtpDto);
    public void setCookies(String verificationId, HttpServletResponse response);
    public void logout(HttpServletRequest request, HttpServletResponse response);
}
