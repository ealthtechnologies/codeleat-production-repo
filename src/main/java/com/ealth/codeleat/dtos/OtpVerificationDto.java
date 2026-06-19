package com.ealth.codeleat.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OtpVerificationDto {
    @NotBlank(message = "Verification Id should not be blank")
    String verificationId;
    @NotBlank(message = "Otp should not be blank")
    String enteredOtp;
}
