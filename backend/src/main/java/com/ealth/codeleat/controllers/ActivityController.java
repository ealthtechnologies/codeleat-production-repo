package com.ealth.codeleat.controllers;

import com.ealth.codeleat.dtos.DailyActivityDto;
import com.ealth.codeleat.services.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/activity")
public class ActivityController {
    //fields
    private final ActivityService activityService;

    @GetMapping("/daily")
    public List<DailyActivityDto> getDailyActivity(
            @RequestParam LocalDate start,
            @RequestParam LocalDate end) {

        return activityService.getUserActivity(start, end);
    }
}
