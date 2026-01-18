package com.ealth.codeleat.controllers;

import com.ealth.codeleat.dtos.UserProgressDto;
import com.ealth.codeleat.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value="/user")
@RequiredArgsConstructor
public class UserController {
    //fields
    private final UserService userService;

    //method to get user progress
    @GetMapping(value="/get-progress")
    public ResponseEntity<UserProgressDto> getUserProgress() {
        return new ResponseEntity<>(userService.getUserProgress(), HttpStatus.OK);
    }

    //method to update user progress
    @PutMapping(value="/update-progress")
    public ResponseEntity<HttpStatus> updateUserProgress(@RequestParam Integer questionId) {
        userService.updateUserProgress(questionId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
