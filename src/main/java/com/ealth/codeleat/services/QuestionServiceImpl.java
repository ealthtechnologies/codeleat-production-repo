package com.ealth.codeleat.services;

import com.ealth.codeleat.entities.Question;
import com.ealth.codeleat.repositories.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {
    //fields
    private final QuestionRepository questionRepository;

    //method to get all questions for the question selection page with pagination
    @Override
    public Page<Question> getAllQuestions(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        log.info("Paginated list of questions fetched from db with {} page number and {} page size", page, size);
        return questionRepository.findAll(pageable);
    }
}
