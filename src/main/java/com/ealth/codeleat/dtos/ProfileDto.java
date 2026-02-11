package com.ealth.codeleat.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProfileDto {
    ProfileDto(String firstName, String lastName, String username, String profilePhotoUrl, String bio) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.profilePhotoUrl = profilePhotoUrl;
        this.bio = bio;
    }
    String firstName;
    String lastName;
    String username;
    String profilePhotoUrl;
    String bio;
    String email;
}
