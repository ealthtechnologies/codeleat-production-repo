package com.ealth.codeleat.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class OtpEmailEvent {
    private final String email;       // Recipient email
    private final String otp;         // OTP code
    private final String eventType;   // e.g., "SIGNUP", "PASSWORD_RESET"

}

