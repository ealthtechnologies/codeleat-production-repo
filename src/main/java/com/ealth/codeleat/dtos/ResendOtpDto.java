package com.ealth.codeleat.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ResendOtpDto {
    String email;
    String verificationId;
}
