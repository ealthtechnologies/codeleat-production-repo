package com.ealth.codeleat.services;

import com.ealth.codeleat.dtos.ProblemListDto;
import com.ealth.codeleat.dtos.QuestionDto;
import com.ealth.codeleat.dtos.SolutionDto;
import com.ealth.codeleat.entities.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionService {
    public Page<ProblemListDto> getAllQuestions(int page, int size);
    public Page<ProblemListDto> searchByQuestionTitle(String query, String difficulty, List<String> tags, int page, int size);
    public SolutionDto getSolutionByQuestionId(Integer id);
}
