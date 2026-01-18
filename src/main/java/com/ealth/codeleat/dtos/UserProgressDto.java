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
        this.easySolved = easySolved != null ? easySolved : 0L;
        this.mediumSolved = mediumSolved != null ? mediumSolved : 0L;
        this.hardSolved = hardSolved != null ? hardSolved : 0L;
        this.totalSolved = this.easySolved + this.mediumSolved + this.hardSolved;
    }
}
