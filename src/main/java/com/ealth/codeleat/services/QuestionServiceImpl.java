package com.ealth.codeleat.services;

import com.ealth.codeleat.dtos.ProblemListDto;
import com.ealth.codeleat.dtos.QuestionDto;
import com.ealth.codeleat.dtos.SolutionDto;
import com.ealth.codeleat.entities.Question;
import com.ealth.codeleat.entities.Tag;
import com.ealth.codeleat.entities.User;
import com.ealth.codeleat.entities.UserQuestion;
import com.ealth.codeleat.exceptions.InvalidOperationException;
import com.ealth.codeleat.repositories.QuestionRepository;
import com.ealth.codeleat.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {
    private final UserRepository userRepository;
    //fields
    private final QuestionRepository questionRepository;
    private final RateLimitService rateLimitService;

    //method to get all questions for the question selection page with pagination
    @Override
    public Page<ProblemListDto> getAllQuestions(int page, int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidOperationException("No user found with the given email!"));

        Pageable pageable = PageRequest.of(page, size);

        // First query: Get paginated questions with user attempts
        Page<Question> questionPage = questionRepository.findAllWithUserStatus(user.getId(), pageable);

        log.info("Paginated list of questions fetched from db with {} page number and {} page size", page, size);

        // Extract question IDs from the current page
        List<Integer> questionIds = questionPage.getContent().stream()
                .map(Question::getId)
                .toList();

        // Second query: Batch fetch tags for all questions on this page
        List<Question> questionsWithTags = questionRepository.findByIdInWithTags(questionIds);

        // Create a map for quick lookup: questionId -> Question (with tags)
        Map<Integer, Question> questionMap = questionsWithTags.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        // Map to DTO
        return questionPage.map(q -> mapToProblemListDto(questionMap.get(q.getId()), user.getId()));
    }

    //helper method to convert the Question Entity to Problem List Dto
    private ProblemListDto mapToProblemListDto(Question q, Integer userId) {
        Set<Tag> tags = q.getTags().stream()
                .map(tag -> new Tag(tag.getId(), tag.getName()))
                .collect(Collectors.toSet());

        // Find the solved status for this user from userAttempts
        boolean solved = q.getUserAttempts().stream()
                .filter(ua -> ua.getUser().getId().equals(userId))
                .findFirst()
                .map(UserQuestion::isSolved)
                .orElse(false);

        return new ProblemListDto(
                q.getId(),
                q.getTitle(),
                q.getDifficulty(),
                tags,
                solved
        );
    }

    //method to search a question by title with autocomplete
    @Override
    public Page<ProblemListDto> searchByQuestionTitle(String query, String difficulty, List<String> tags, int page, int size) {
        // Check if the user exceeded the rate limit
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidOperationException("No user with given email id exists!"));

        boolean allowed = rateLimitService.tryConsume(email);
        if (!allowed) {
            log.warn("Rate limit exceeded for user: {}", email);
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many requests.");
        }

        int userId = user.getId();
        Pageable pageable = PageRequest.of(page, size);

        // Query 1: Get paginated questions matching filters (no eager loading)
        Page<Question> questionPage = questionRepository.findAllWithFilters(
                query, difficulty, tags, userId, pageable
        );

        log.info("Searching questions - query: '{}', difficulty: '{}', tags: {}, page: {}, size: {}, found: {}",
                query, difficulty, tags, page, size, questionPage.getTotalElements());

        // Extract question IDs from the current page
        List<Integer> questionIds = questionPage.getContent().stream()
                .map(Question::getId)
                .toList();

        // Early return if no questions found
        if (questionIds.isEmpty()) {
            log.info("No questions found matching the criteria");
            return Page.empty(pageable);
        }

        // Query 2: Batch fetch tags - Hibernate merges into session cache
        questionRepository.findByIdInWithTags(questionIds);
        log.debug("Batch fetched tags for {} questions", questionIds.size());

        // Query 3: Batch fetch user attempts - Hibernate merges into session cache
        questionRepository.findByIdInWithUserAttempts(questionIds, userId);
        log.debug("Batch fetched user attempts for {} questions", questionIds.size());

        // Map to DTO - everything is now loaded in the Question entities
        return questionPage.map(q -> mapToProblemListDto(q, userId));
    }

    //method to get the solution of a question
    @Override
    public SolutionDto getSolutionByQuestionId(Integer id) {
        Question q = questionRepository.findByIdWithTags(id)
                .orElseThrow(() -> new NoSuchElementException("Question not found with id: " + id));

        return new SolutionDto(
                q.getId(),
                q.getTitle(),
                q.getDifficulty(),
                q.getAcceptance(),
                q.getSubmissions(),
                q.getDescription(),
                q.getIntuition(),
                q.getCodeSnippets(),
                q.getComplexity(),
                q.getVisualSteps(),
                q.getMistakes(),
                q.getRelated(),
                q.getTips(),
                q.getTags().stream().map(t -> t.getName()).collect(Collectors.toSet())
        );
    }
}
