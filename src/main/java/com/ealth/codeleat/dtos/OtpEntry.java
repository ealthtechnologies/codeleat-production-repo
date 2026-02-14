package com.ealth.codeleat.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
@NoArgsConstructor
public class OtpEntry {
    Integer userId;
    String otp;
}
