package com.ealth.codeleat.services;

import com.ealth.codeleat.dtos.ProfileDto;
import com.ealth.codeleat.dtos.UserProgressDto;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    public UserProgressDto getUserProgress();
    public void updateUserProgress(Integer questionId);
    public ProfileDto getUserProfile();
    public void updateProfile(String firstName, String lastName, String username, String bio, MultipartFile photo);
    public Integer getUserStreak();
}
