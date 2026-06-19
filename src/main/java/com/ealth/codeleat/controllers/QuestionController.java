package com.ealth.codeleat.controllers;

import com.ealth.codeleat.dtos.ProblemListDto;
import com.ealth.codeleat.dtos.QuestionDto;
import com.ealth.codeleat.dtos.SolutionDto;
import com.ealth.codeleat.entities.Question;
import com.ealth.codeleat.services.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value="/question")
public class QuestionController {
    //fields
    private final QuestionService questionService;

    //method to get all questions for question selection
    @GetMapping(value="/get")
    public ResponseEntity<Page<ProblemListDto>> getAllQuestions(@RequestParam int page, @RequestParam int size) {
        log.info("Request received for fetching question for question selection page with {} page number and {} page size", page, size);
        return new ResponseEntity<>(questionService.getAllQuestions(page, size), HttpStatus.OK);
    }

    //method to search questions by their title
    @GetMapping("/search")
    public ResponseEntity<Page<ProblemListDto>> searchProblems(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ProblemListDto> results = questionService.searchByQuestionTitle(query, difficulty, tags, page, size);
        return ResponseEntity.ok(results);
    }

    //method to get the solution data for a question by its id
    @GetMapping("/{id}")
    public SolutionDto getQuestion(@PathVariable Integer id) {
        return questionService.getSolutionByQuestionId(id);
    }
}
