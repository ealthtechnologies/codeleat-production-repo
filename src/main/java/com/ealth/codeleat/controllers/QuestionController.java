package com.ealth.codeleat.controllers;

import com.ealth.codeleat.entities.Question;
import com.ealth.codeleat.services.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value="/question")
public class QuestionController {
    //fields
    private final QuestionService questionService;

    //method to get all questions for question selection
    @GetMapping(value="get")
    public ResponseEntity<Page<Question>> getAllQuestions(@RequestParam int page, @RequestParam int size) {
        log.info("Request received for fetching question for question selection page with {} page number and {} page size", page, size);
        return new ResponseEntity<>(questionService.getAllQuestions(page, size), HttpStatus.OK);
    }
}
