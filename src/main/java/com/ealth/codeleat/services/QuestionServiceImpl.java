package com.ealth.codeleat.services;

import com.ealth.codeleat.dtos.QuestionDto;
import com.ealth.codeleat.entities.Question;
import com.ealth.codeleat.entities.Tag;
import com.ealth.codeleat.repositories.QuestionRepository;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {
    //fields
    private final QuestionRepository questionRepository;
    private final RateLimitService rateLimitService;

    //method to get all questions for the question selection page with pagination
    @Override
    public Page<Question> getAllQuestions(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        log.info("Paginated list of questions fetched from db with {} page number and {} page size", page, size);
        return questionRepository.findAll(pageable);
    }

    //method to search a question by title with autocomplete
    @Override
    public List<QuestionDto> searchByQuestionTitle(String query, int page, int size) {
        //check if the user exceeded the rate limit
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        boolean allowed = rateLimitService.tryConsume(email);
        if (!allowed) {
            System.out.println("Rate Limited");
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many requests.");
        }
        System.out.println("Forwarded");

        query = query == null ? "" : query;
        Pageable pageable = PageRequest.of(page, size);
        List<Question> questions = questionRepository.searchByQuestionTitle(query, pageable);

        return questions.stream()
                .map(q -> new QuestionDto(
                        q.getId(),
                        q.getTitle(),
                        q.getDifficulty(),
                        q.getTags().stream().map(Tag::getName).toList()
                ))
                .toList();
    }
}
