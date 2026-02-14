package com.ealth.codeleat.controllers;

import com.ealth.codeleat.dtos.ProfileDto;
import com.ealth.codeleat.dtos.UserProgressDto;
import com.ealth.codeleat.entities.User;
import com.ealth.codeleat.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping(value="/user")
@RequiredArgsConstructor
public class UserController {
    //fields
    private final UserService userService;

    //method to get user progress
    @GetMapping(value = "/get-progress")
    public ResponseEntity<UserProgressDto> getUserProgress() {
        return new ResponseEntity<>(userService.getUserProgress(), HttpStatus.OK);
    }

    //method to update user progress
    @PutMapping(value = "/update-progress")
    public ResponseEntity<HttpStatus> updateUserProgress(@RequestParam Integer questionId) {
        userService.updateUserProgress(questionId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    //Endpoint to get user profile
    @GetMapping(value = "/get-profile")
    public ResponseEntity<ProfileDto> getUserProfile() {
        return new ResponseEntity<>(userService.getUserProfile(), HttpStatus.OK);
    }

    //Endpoint to update profile for user (bio, skills, profile photo and username)
    @PutMapping(value = "/update-profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<User> completeProfile(
            @RequestPart(value = "profilePhoto", required = false) MultipartFile photo,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "bio", required = false) String bio,
            @RequestParam(value = "firstName", required = false) String firstName,
            @RequestParam(value = "lastName", required = false) String lastName
    ) {
        userService.updateProfile(firstName, lastName, username, bio, photo);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    //Endpoint to get user streak
    @GetMapping(value="/get-streak")
    public ResponseEntity<Integer> forgotPassword() {
        return new ResponseEntity<>(userService.getUserStreak(), HttpStatus.OK);
    }
}
