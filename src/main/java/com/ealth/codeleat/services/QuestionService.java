package com.ealth.codeleat.services;

import com.ealth.codeleat.entities.Question;
import org.springframework.data.domain.Page;

public interface QuestionService {
    public Page<Question> getAllQuestions(int page, int size);
}
