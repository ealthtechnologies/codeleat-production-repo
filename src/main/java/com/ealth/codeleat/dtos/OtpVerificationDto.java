package com.ealth.codeleat.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class OtpVerificationDto {
    @NotBlank(message = "Verification Id should not be blank")
    String verificationId;
    @NotBlank(message = "Otp should not be blank")
    String enteredOtp;
}
