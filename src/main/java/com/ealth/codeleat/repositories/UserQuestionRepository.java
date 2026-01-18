package com.ealth.codeleat.repositories;

import com.ealth.codeleat.dtos.DailyActivityDto;
import com.ealth.codeleat.dtos.UserProgressDto;
import com.ealth.codeleat.entities.UserQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserQuestionRepository extends JpaRepository<UserQuestion, Long> {
    @Query(value = """
    SELECT DATE(solved_at) AS day, COUNT(question_id) AS count
    FROM user_question
    WHERE user_id = :userId
    AND solved_at BETWEEN :start AND :end
    GROUP BY DATE(solved_at)
    ORDER BY day""", nativeQuery = true)
    List<Object[]> findDailyActivity(
            @Param("userId") Integer userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
    SELECT new com.ealth.codeleat.dtos.UserProgressDto(
        SUM(CASE WHEN q.difficulty = 'Easy' THEN 1 ELSE 0 END),
        SUM(CASE WHEN q.difficulty = 'Medium' THEN 1 ELSE 0 END),
        SUM(CASE WHEN q.difficulty = 'Hard' THEN 1 ELSE 0 END)
    )
    FROM UserQuestion uq
    JOIN uq.question q
    WHERE uq.user.id = :userId
    """)
    UserProgressDto getUserDifficultySummary(@Param("userId") Integer userId);
}
