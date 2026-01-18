package com.ealth.codeleat.loader;

import com.ealth.codeleat.entities.Question;
import com.ealth.codeleat.entities.Tag;
import com.ealth.codeleat.repositories.QuestionRepository;
import com.ealth.codeleat.repositories.TagRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class QuestionDataLoader {

    private final QuestionRepository questionRepository;
    private final TagRepository tagRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void saveQuestion(QuestionJson qj) throws JsonProcessingException {
        Question question = new Question();
        question.setTitle(qj.getTitle());
        question.setDifficulty(qj.getDifficulty());
        question.setAcceptance(qj.getAcceptance());
        question.setSubmissions(qj.getSubmissions());
        question.setDescription(qj.getDescription());

        // Serialize JSON fields as String
        question.setIntuition(objectMapper.writeValueAsString(qj.getIntuition()));
        question.setCodeSnippets(objectMapper.writeValueAsString(qj.getCodeSnippets()));
        question.setComplexity(objectMapper.writeValueAsString(qj.getComplexity()));
        question.setVisualSteps(qj.getVisualSteps() == null ? null : objectMapper.writeValueAsString(qj.getVisualSteps()));
        question.setMistakes(objectMapper.writeValueAsString(qj.getMistakes()));
        question.setRelated(objectMapper.writeValueAsString(qj.getRelated()));
        question.setTips(objectMapper.writeValueAsString(qj.getTips()));

        // Handle tags
        Set<Tag> tagSet = new HashSet<>();
        for (String tagName : qj.getTags()) {
            Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> tagRepository.save(new Tag(null, tagName)));
            tagSet.add(tag);
        }
        question.setTags(tagSet);

        questionRepository.save(question);
    }
}

