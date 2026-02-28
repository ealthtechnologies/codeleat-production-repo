package com.ealth.codeleat.services;

import com.ealth.codeleat.dtos.CloudinaryUploadResult;
import com.ealth.codeleat.dtos.ProfileDto;
import com.ealth.codeleat.dtos.UserProgressDto;
import com.ealth.codeleat.entities.Question;
import com.ealth.codeleat.entities.User;
import com.ealth.codeleat.entities.UserQuestion;
import com.ealth.codeleat.exceptions.InvalidOperationException;
import com.ealth.codeleat.repositories.QuestionRepository;
import com.ealth.codeleat.repositories.UserQuestionRepository;
import com.ealth.codeleat.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    //fields
    private final UserQuestionRepository userQuestionRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final CloudinaryService cloudinaryService;

    //helper method to get the authenticated user
    public User getCurrentUser() {
        //check if the user is authenticated
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated()) {
            throw new InvalidOperationException("User not logged in!");
        }
        String email = authentication.getName();

        //check if a user with this email id exists or not
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidOperationException("No user found with the given email!"));
    }

    @Override
    public UserProgressDto getUserProgress() {
        User user = getCurrentUser();
        return userQuestionRepository.getUserDifficultySummary(user.getId());
    }

    @Override
    @Transactional
    public void updateUserProgress(Integer questionId) {
        User user = getCurrentUser();

        //check if a question with the given id exists in the db or not
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new InvalidOperationException("No question with given id found"));

        //if the user has already solved the question then just reverse the solved status as the user might have
        //clicked on the tick symbol once again
        Optional<UserQuestion> optionalUserQuestion = userQuestionRepository.findByQuestionIdAndUserId(questionId, user.getId());
        if(optionalUserQuestion.isPresent()) {
            boolean isSolved = optionalUserQuestion.get().isSolved();
            if(optionalUserQuestion.get().isSolved()) {
                optionalUserQuestion.get().setSolvedAt(null);
            } else {
                optionalUserQuestion.get().setSolvedAt(LocalDateTime.now());
            }
            optionalUserQuestion.get().setSolved(!isSolved);
            userQuestionRepository.save(optionalUserQuestion.get());
            return;
        }


        //create a new UserQuestion entity to mark the question as solved for this user
        UserQuestion userQuestion = new UserQuestion();
        userQuestion.setUser(user);
        userQuestion.setQuestion(question);
        userQuestion.setSolvedAt(LocalDateTime.now());
        userQuestion.setSolved(true);

        user.getAttemptedQuestions().add(userQuestion);

        userQuestionRepository.save(userQuestion);
    }

    //Method to get the profile of a user
    public ProfileDto getUserProfile() {
        //Get the currently authenticated user from
        User user = getCurrentUser();

        //Return the profile of the user by fetching it from database
        return userRepository.getUserProfile(user.getId());
    }

    //Method to update user profile
    @Override
    public void updateProfile(String firstName, String lastName, String username, String bio, MultipartFile photo, Boolean removePhoto) {
        //Get the currently authenticated user from Application Context
        User currentUser = getCurrentUser();

        //Check if the user wants to remove his profile photo
        if(removePhoto) {
            cloudinaryService.deleteImage(currentUser.getProfilePhotoPublicId());
            currentUser.setProfilePhotoUrl(null);
            currentUser.setProfilePhotoPublicId(null);
        }

        //Check if the user has uploaded a photo or not and upload it to Cloudinary
        if(photo != null && !photo.isEmpty()) {
            if(currentUser.getProfilePhotoPublicId() != null) {
                cloudinaryService.deleteImage(currentUser.getProfilePhotoPublicId());
            }
            CloudinaryUploadResult cloudinaryUploadResult = cloudinaryService.uploadImage(photo);
            String secureUrl = cloudinaryUploadResult.getUrl();
            String publicId = cloudinaryUploadResult.getPublicId();
            currentUser.setProfilePhotoUrl(secureUrl);
            currentUser.setProfilePhotoPublicId(publicId);
        }

        //Check if the user has given his username or not and update accordingly
        if(username != null && !username.isBlank()) {
            currentUser.setUsername(username);
        }

        //Check if the user has given his first name or not and update accordingly
        if(firstName != null && !firstName.isBlank()) {
            currentUser.setFirstName(firstName);
        }

        //Check if the user has given his last name or not and update accordingly
        if(lastName != null && !lastName.isBlank()) {
            currentUser.setLastName(lastName);
        }

        //Check if the user has given his bio or not and update accordingly
        if(bio != null && !bio.isBlank()) {
            currentUser.setBio(bio);
        }

        userRepository.save(currentUser);
    }

    //Method to get user streak
    public Integer getUserStreak() {
        //Get the currently authenticated user
        User user = getCurrentUser();
        Integer userId = user.getId();

        //Get the questions record of the user from the database
        List<LocalDate> dates = userQuestionRepository.getUserStreak(userId).stream()
                .map(LocalDateTime::toLocalDate)
                .distinct()
                .toList();

        Integer streak = 0;
        LocalDate today = LocalDate.now();

        for (LocalDate date : dates) {
            if (date.equals(today.minusDays(streak))) {
                streak++;
            } else {
                break;
            }
        }

        return streak;
    }
}
