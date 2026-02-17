package com.ealth.codeleat.controllers;

import com.ealth.codeleat.dtos.*;
import com.ealth.codeleat.entities.RefreshToken;
import com.ealth.codeleat.entities.User;
import com.ealth.codeleat.repositories.RefreshTokenRepository;
import com.ealth.codeleat.repositories.UserRepository;
import com.ealth.codeleat.security.JwtService;
import com.ealth.codeleat.services.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value="/auth")
public class AuthController {
    //Fields
    private final AuthService authService;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    //Method for user Sign Up
    @PostMapping(value="/sign-up")
    public ResponseEntity<AuthResponseDto> signUp(@Valid @RequestBody UserSignUpDto userSignUpDto) {
        AuthResponseDto authResponseDto = authService.signUp(userSignUpDto);
        return new ResponseEntity<>(authResponseDto, HttpStatus.CREATED);
    }

    //Method for user Login
    @PostMapping(value="/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody UserLoginDto userLoginDto, HttpServletResponse response) {
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

    //Endpoint to refresh token
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {

        //Extract refresh token from HttpOnly cookie
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("REFRESH_TOKEN".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if(refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No refresh token found");
        }

        //Validate token
        if(!jwtService.isRefreshToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token type");
        }

        //Find token in DB
        RefreshToken dbToken = refreshTokenRepository.findByToken(refreshToken)
                .orElse(null);

        if(dbToken == null || dbToken.getExpiryDate().isBefore(Instant.now())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token expired or invalid");
        }

        //Load user
        User user = dbToken.getUser();

        String newAccessToken = jwtService.generateToken(new HashMap<>(), user.getEmail());

        //Set cookies
        response.addHeader("Set-Cookie",
                String.format("ACCESS_TOKEN=%s; Max-Age=%d; Path=/; HttpOnly; Secure; SameSite=None",
                        newAccessToken, 15*60));

        return ResponseEntity.ok("Token refreshed successfully");
    }

    //Endpoint for Forgot Password and Password Reset
    @PostMapping(value="/forgot-password")
    public ResponseEntity<HttpStatus> sendEmailWithForgotPasswordLink(@RequestBody ForgotPasswordDto forgotPasswordDto) {
        authService.forgotPassword(forgotPasswordDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    //Endpoint for Resetting Password
    @PostMapping(value="/reset-password")
    public ResponseEntity<HttpStatus> resetPassword(@RequestBody ResetPasswordDto resetPasswordDto) {
        authService.resetPassword(resetPasswordDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    //Endpoint to receive request for Resending the OTP
    @PostMapping(value="/resend-otp")
    public ResponseEntity<AuthResponseDto> resendOtp(@RequestBody ResendOtpDto resendOtpDto) {
        return new ResponseEntity<>(authService.resendOtp(resendOtpDto), HttpStatus.OK);
    }

    //Endpoint to set cookies after o-auth success
    @GetMapping(value="/set-cookies")
    public ResponseEntity<HttpStatus> setCookies(@RequestParam String verificationId, HttpServletResponse response) {
        authService.setCookies(verificationId, response);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
