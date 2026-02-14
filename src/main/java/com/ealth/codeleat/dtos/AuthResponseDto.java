package com.ealth.codeleat.dtos;

import com.ealth.codeleat.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AuthResponseDto {
    public AuthResponseDto(AccountStatus accountStatus, String message) {
        this.status = accountStatus;
        this.message = message;
    }
    AccountStatus status;
    String verificationId;
    String message;
}
