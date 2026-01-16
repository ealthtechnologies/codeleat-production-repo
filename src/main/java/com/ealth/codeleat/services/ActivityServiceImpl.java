package com.ealth.codeleat.services;

import com.ealth.codeleat.dtos.DailyActivityDto;
import com.ealth.codeleat.entities.User;
import com.ealth.codeleat.repositories.UserQuestionRepository;
import com.ealth.codeleat.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {
    //fields
    private final UserRepository userRepository;
    private final UserQuestionRepository userQuestionRepository;

    @Override
    public List<DailyActivityDto> getUserActivity(LocalDate start, LocalDate end) {

        //fetch user from SecurityContext (as you already do)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDateTime startDt = start.atStartOfDay();
        LocalDateTime endDt = end.atTime(LocalTime.MAX);

        List<Object[]> rows =
                userQuestionRepository.findDailyActivity(
                        user.getId(), startDt, endDt);

        return rows.stream()
                .map(row -> new DailyActivityDto(
                        ((LocalDate) (row[0])),
                        ((Number) row[1]).longValue()
                ))
                .toList();
    }
}
