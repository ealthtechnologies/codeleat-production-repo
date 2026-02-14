package com.ealth.codeleat.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Tokens {
    String accessToken;
    String refreshToken;
}
