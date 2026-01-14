package com.ealth.codeleat.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JwtResponseDto {
    private String token;
    private String tokenType;
    private int expiresIn;
}
