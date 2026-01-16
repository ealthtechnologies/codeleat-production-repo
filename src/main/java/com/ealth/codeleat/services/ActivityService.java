package com.ealth.codeleat.services;

import com.ealth.codeleat.dtos.DailyActivityDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ActivityService {
    public List<DailyActivityDto> getUserActivity(LocalDate start, LocalDate end);
}
