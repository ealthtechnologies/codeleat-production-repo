package com.ealth.codeleat.services;

import com.ealth.codeleat.dtos.QuestionDto;
import com.ealth.codeleat.entities.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionService {
    public Page<Question> getAllQuestions(int page, int size);
    public List<QuestionDto> searchByQuestionTitle(String query, int page, int size);
}
