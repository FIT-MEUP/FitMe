package fitmeup.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fitmeup.entity.CommentEntity;
import fitmeup.entity.MealEntity;
import fitmeup.entity.WorkEntity;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    // ✅ 특정 운동 게시글의 댓글 조회
    List<CommentEntity> findByWorkout(WorkEntity workout);

    // ✅ 특정 식단 게시글의 댓글 조회
    List<CommentEntity> findByMeal(MealEntity meal);

    // ✅ 특정 날짜의 댓글 조회 (운동 & 식단 포함) → "날짜" 기준으로 조회하도록 변경
    @Query("SELECT c FROM CommentEntity c WHERE c.createdAt BETWEEN :startOfDay AND :endOfDay")
    List<CommentEntity> findByCreatedAtDate(
        @Param("startOfDay") LocalDateTime startOfDay,
        @Param("endOfDay") LocalDateTime endOfDay
    );
    
    @Query("SELECT c FROM CommentEntity c " +
    	       "WHERE c.workout.workoutId = :workoutId " +
    	       "AND c.workout.workoutDate = :date")
    	List<CommentEntity> findByWorkoutAndDate(@Param("workoutId") Long workoutId, @Param("date") LocalDate date);



    // 🔻 [임시 코드] 특정 날짜 + 특정 사용자(`userId`)의 댓글 가져오기
    // ❗ 추후 `userId` 필수 반영 시, findByCreatedAtDate() 대신 이 메서드를 사용해야 함.
    @Query("SELECT c FROM CommentEntity c WHERE DATE(c.createdAt) = :date AND c.user.userId = :userId")
    List<CommentEntity> findByCreatedAtDateAndUserId(LocalDate date, Long userId);
}
