package com.ealth.codeleat.controllers;

import com.ealth.codeleat.dtos.JwtResponseDto;
import com.ealth.codeleat.dtos.OtpVerificationDto;
import com.ealth.codeleat.dtos.UserLoginDto;
import com.ealth.codeleat.dtos.UserSignUpDto;
import com.ealth.codeleat.services.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value="/auth")
public class AuthController {
    //fields
    private final AuthService authService;

    //method for user Sign Up
    @PostMapping("/sign-up")
    public ResponseEntity<String> signUp(@Valid @RequestBody UserSignUpDto userSignUpDto) {
        return new ResponseEntity<>(authService.signUp(userSignUpDto), HttpStatus.CREATED);
    }

    //method for user Login
    @PostMapping(value="/login")
    public ResponseEntity<JwtResponseDto> login(@Valid @RequestBody UserLoginDto userLoginDto, HttpServletResponse response) {
        return new ResponseEntity<>(authService.login(userLoginDto, response), HttpStatus.OK);
    }

    //method for the frontend to fetch the jwt from the cookie
    @GetMapping(value="/get-token")
    public ResponseEntity<JwtResponseDto> fetchJwt(@CookieValue(name = "TEMP_JWT", required = false) String jwtToken,
                                                   HttpServletResponse response
    ) {
        //If cookie missing that means user not authenticated
        if (jwtToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        //Delete the temporary cookie immediately
        Cookie deleteCookie = new Cookie("TEMP_JWT", null);
        deleteCookie.setPath("/auth/get-token");
        deleteCookie.setHttpOnly(true);
        deleteCookie.setMaxAge(0);
        response.addCookie(deleteCookie);

        return new ResponseEntity<>(new JwtResponseDto(jwtToken, "Bearer", 604800000 / 1000), HttpStatus.OK);
    }

    //Method to verify otp
    @PostMapping(value="/verify-otp")
    public ResponseEntity<HttpStatus> verifyOtp(@Valid @RequestBody OtpVerificationDto otpVerificationDto) {
        authService.verifyOtp(otpVerificationDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
