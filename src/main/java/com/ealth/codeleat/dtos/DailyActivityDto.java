package com.ealth.codeleat.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class DailyActivityDto {
    private LocalDate date;
    private long count;

    public DailyActivityDto(LocalDate date, long count) {
        this.date = date;
        this.count = count;
    }
}
