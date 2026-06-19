package com.ealth.codeleat.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.Set;

@Data
@AllArgsConstructor
@Getter
public class SolutionDto {
    private Integer id;
    private String title;
    private String difficulty;
    private String acceptance;
    private String submissions;
    private String description;
    private String intuition;       // JSON stored as String
    private String codeSnippets;    // JSON stored as String
    private String complexity;      // JSON stored as String
    private String visualSteps;     // JSON stored as String
    private String mistakes;        // JSON stored as String
    private String related;         // JSON stored as String
    private String tips;            // JSON stored as String
    private Set<String> tags;       // Just tag names
}

