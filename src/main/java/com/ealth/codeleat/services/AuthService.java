package com.ealth.codeleat.services;

import com.ealth.codeleat.dtos.JwtResponseDto;
import com.ealth.codeleat.dtos.OtpVerificationDto;
import com.ealth.codeleat.dtos.UserLoginDto;
import com.ealth.codeleat.dtos.UserSignUpDto;

public interface AuthService {
    public String signUp(UserSignUpDto userSignUpDto);
    public JwtResponseDto login(UserLoginDto userLoginDto);
    public void verifyOtp(OtpVerificationDto otpVerificationDto);
}
