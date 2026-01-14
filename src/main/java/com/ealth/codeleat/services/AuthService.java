package com.ealth.codeleat.services;

import com.ealth.codeleat.dtos.JwtResponseDto;
import com.ealth.codeleat.dtos.UserLoginDto;
import com.ealth.codeleat.dtos.UserSignUpDto;

public interface AuthService {
    public void signUp(UserSignUpDto userSignUpDto);
    public JwtResponseDto login(UserLoginDto userLoginDto);
}
