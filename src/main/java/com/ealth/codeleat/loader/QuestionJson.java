package com.ealth.codeleat.loader;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class QuestionJson {
    private Integer id;
    private String title;
    private String difficulty;
    private String acceptance;
    private String submissions;
    private String description;
    private List<Map<String, String>> intuition; // title + text
    private Map<String, String> codeSnippets;    // java, python, etc
    private Map<String, Map<String,String>> complexity; // time + space
    private List<Map<String, String>> visualSteps; // desc + image/svg
    private List<Map<String, String>> mistakes;   // title + text
    private List<Map<String, Object>> related;    // id, title, difficulty, tags, link
    private List<Map<String, String>> tips;       // title + text
    private List<String> tags;                    // simple tag list
}
