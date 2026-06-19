package com.ealth.codeleat.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QuestionDto {
    private Integer id;
    private String title;
    private String difficulty;
    private List<String> tags;

    //constructor
    public QuestionDto(Integer id, String title, String difficulty, List<String> tags) {
        this.id = id;
        this.title = title;
        this.difficulty = difficulty;
        this.tags = tags;
    }
}
