package com.ealth.codeleat.services;

import com.ealth.codeleat.dtos.UserProgressDto;
import com.ealth.codeleat.entities.Question;
import com.ealth.codeleat.entities.User;
import com.ealth.codeleat.entities.UserQuestion;
import com.ealth.codeleat.exceptions.InvalidOperationException;
import com.ealth.codeleat.repositories.QuestionRepository;
import com.ealth.codeleat.repositories.UserQuestionRepository;
import com.ealth.codeleat.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    //fields
    private final UserQuestionRepository userQuestionRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;

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
}
