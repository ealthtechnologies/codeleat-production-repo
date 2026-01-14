package com.ealth.codeleat.controllers;

import com.ealth.codeleat.dtos.JwtResponseDto;
import com.ealth.codeleat.dtos.UserLoginDto;
import com.ealth.codeleat.dtos.UserSignUpDto;
import com.ealth.codeleat.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(value="/auth")
public class AuthController {
    //fields
    private final AuthService authService;

    //method for user Sign Up
    @PostMapping("/sign-up")
    public ResponseEntity<HttpStatus> signUp(@Valid @RequestBody UserSignUpDto userSignUpDto) {
        authService.signUp(userSignUpDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    //method for user Login
    @PostMapping(value="/login")
    public ResponseEntity<JwtResponseDto> login(@Valid @RequestBody UserLoginDto userLoginDto) {
        return new ResponseEntity<>(authService.login(userLoginDto), HttpStatus.OK);
    }
}
