package com.ealth.codeleat.repositories;

import com.ealth.codeleat.entities.Question;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer> {
    @EntityGraph(attributePaths = {"tags"})
    @Query("SELECT DISTINCT q FROM Question q WHERE q.title LIKE %:query%")
    public List<Question> searchByQuestionTitle(@Param("query") String query, Pageable pageable);
}
