package com.ealth.codeleat.controllers;

import com.ealth.codeleat.entities.Question;
import com.ealth.codeleat.loader.QuestionDataLoader;
import com.ealth.codeleat.loader.QuestionJson;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value="/data")
@RequiredArgsConstructor
public class DataLoaderController {
    //fields
    private final QuestionDataLoader questionDataLoader;

    //method to populate db with solutions for questions
    @PostMapping(value="/load")
    public ResponseEntity<String> loadData(@RequestBody QuestionJson questionJson) {
        try {
            questionDataLoader.saveQuestion(questionJson);
            return new ResponseEntity<>("Question loaded in the database successfully!", HttpStatus.OK);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
