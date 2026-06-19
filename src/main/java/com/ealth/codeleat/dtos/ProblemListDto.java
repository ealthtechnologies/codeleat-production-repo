package com.ealth.codeleat.dtos;

import com.ealth.codeleat.entities.Tag;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class ProblemListDto {
    private Integer id;
    private String title;
    private String difficulty;
    private Set<Tag> topic;
    private boolean solved;

    // Constructor for JPQL (must match query below)
    public ProblemListDto(Integer id, String title,
                          String difficulty, Set<Tag> topic, boolean solved) {
        this.id = id;
        this.title = title;
        this.difficulty = difficulty;
        this.topic = topic;
        this.solved = solved;
    }
}