package com.ealth.codeleat.repositories;

import com.ealth.codeleat.dtos.ProblemListDto;
import com.ealth.codeleat.entities.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer> {
    @Query("SELECT q FROM Question q LEFT JOIN FETCH q.tags")
    Page<Question> findAllWithTags(Pageable pageable);


    @EntityGraph(attributePaths = {"tags"})
    @Query("""
    SELECT DISTINCT q FROM Question q
    JOIN q.tags t
    WHERE (:query IS NULL OR LOWER(q.title) LIKE LOWER(CONCAT('%', :query, '%')))
      AND (:difficulty IS NULL OR q.difficulty = :difficulty)
      AND (:tags IS NULL OR t.name IN :tags)
""")
    List<Question> searchByQuestionTitle(
            @Param("query") String query,
            @Param("difficulty") String difficulty,
            @Param("tags") List<String> tags,
            Pageable pageable
    );

    @Query("""
        SELECT q 
        FROM Question q
        LEFT JOIN FETCH q.tags
        WHERE q.id = :id
    """)
    Optional<Question> findByIdWithTags(@Param("id") Integer id);

    @Query("""
        SELECT DISTINCT q FROM Question q 
        LEFT JOIN q.userAttempts ua 
        LEFT JOIN q.tags t
        WHERE (:query IS NULL OR LOWER(q.title) LIKE LOWER(CONCAT('%', :query, '%')))
        AND (:difficulty IS NULL OR LOWER(q.difficulty) = LOWER(:difficulty))
        AND (:tags IS NULL OR t.name IN :tags)
        AND (ua.user.id = :userId OR ua IS NULL)
        """)
    Page<Question> findAllWithFilters(
            @Param("query") String query,
            @Param("difficulty") String difficulty,
            @Param("tags") List<String> tags,
            @Param("userId") Integer userId,
            Pageable pageable
    );

    @Query("""
    SELECT DISTINCT q FROM Question q 
    LEFT JOIN FETCH q.userAttempts ua 
    WHERE ua.user.id = :userId OR ua IS NULL
    """)
    Page<Question> findAllWithUserStatus(@Param("userId") Integer userId, Pageable pageable);

    @Query("SELECT DISTINCT q FROM Question q LEFT JOIN FETCH q.tags WHERE q.id IN :ids")
    List<Question> findByIdInWithTags(@Param("ids") List<Integer> ids);

    @Query("""
        SELECT DISTINCT q FROM Question q
        LEFT JOIN FETCH q.userAttempts ua
        WHERE q.id IN :ids AND (ua.user.id = :userId OR ua IS NULL)
        """)
    List<Question> findByIdInWithUserAttempts(
            @Param("ids") List<Integer> ids,
            @Param("userId") Integer userId
    );
}
