package com.ealth.codeleat.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class ResendOtpDto {
    String email;
    String verificationId;
}
