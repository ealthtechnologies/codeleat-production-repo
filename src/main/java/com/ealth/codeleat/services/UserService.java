package com.ealth.codeleat.services;

import com.ealth.codeleat.dtos.UserProgressDto;

public interface UserService {
    public UserProgressDto getUserProgress();
    public void updateUserProgress(Integer questionId);
}
