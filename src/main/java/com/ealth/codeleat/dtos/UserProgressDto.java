package com.ealth.codeleat.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserProgressDto {
    Long easySolved;
    Long mediumSolved;
    Long hardSolved;
    Long totalSolved;

    public UserProgressDto(Long easySolved, Long mediumSolved, Long hardSolved) {
        this.easySolved = easySolved;
        this.mediumSolved = mediumSolved;
        this.hardSolved = hardSolved;
        this.totalSolved = easySolved + mediumSolved + hardSolved;
    }
}
