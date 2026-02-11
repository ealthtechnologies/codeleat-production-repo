package com.ealth.codeleat.services;

import com.ealth.codeleat.dtos.JwtResponseDto;
import com.ealth.codeleat.dtos.OtpVerificationDto;
import com.ealth.codeleat.dtos.UserLoginDto;
import com.ealth.codeleat.dtos.UserSignUpDto;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    public String signUp(UserSignUpDto userSignUpDto);
    public JwtResponseDto login(UserLoginDto userLoginDto, HttpServletResponse response);
    public void verifyOtp(OtpVerificationDto otpVerificationDto);
}
