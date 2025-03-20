package fitmeup.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fitmeup.entity.UserEntity;
import fitmeup.entity.WorkEntity;

@Repository
public interface WorkRepository extends JpaRepository<WorkEntity, Long> {
	
	//특정 User의 특정 날짜 운동 기록 조회
	@Query("SELECT w FROM WorkEntity w WHERE w.user.userId = :userId AND w.workoutDate = :workoutDate")
	List<WorkEntity> findByUserUserIdAndWorkoutDate(
	    @Param("userId") Long userId,
	    @Param("workoutDate") LocalDate workoutDate
	);

	 //  특정 날짜의 모든 운동 기록 조회
    @Query("SELECT w FROM WorkEntity w WHERE w.workoutDate = :workoutDate")
    List<WorkEntity> findWorkoutsByDate(@Param("workoutDate") LocalDate workoutDate);
    
    // Trainer가 승인된 회원들의 특정 날짜 운동 기록 조회
    @Query("SELECT w FROM WorkEntity w WHERE (:userIds IS NULL OR w.user.userId IN :userIds) AND w.workoutDate = :workoutDate")
    List<WorkEntity> findByUserUserIdInAndWorkoutDate(@Param("userIds") List<Long> userIds, 
                                                      @Param("workoutDate") LocalDate workoutDate);

    //특정 운동 목록 (workoutIds) 조회 (검색 기능에 활용)
    @Query("SELECT w FROM WorkEntity w WHERE w.workoutId IN :workoutIds")
    List<WorkEntity> findByWorkoutIdIn(@Param("workoutIds") List<Long> workoutIds);


    //검색 기능 (운동 이름을 포함하는 기록 조회)
    @Query("SELECT w FROM WorkEntity w WHERE LOWER(w.exercise) LIKE LOWER(CONCAT('%', :exercise, '%'))")
    List<WorkEntity> findByExerciseContainingIgnoreCase(@Param("exercise") String exercise);
    
 // 특정 회원(userId)의 운동을 검색 (운동명 기준)
    @Query("SELECT w FROM WorkEntity w WHERE w.user.userId = :userId AND LOWER(w.exercise) LIKE LOWER(CONCAT('%', :exercise, '%'))")
    List<WorkEntity> findByUserUserIdAndExerciseContainingIgnoreCase(@Param("userId") Long userId, @Param("exercise") String exercise);

    // dot 
    @Query("SELECT DISTINCT w.workoutDate FROM WorkEntity w WHERE w.user = :user AND YEAR(w.workoutDate) = :year AND MONTH(w.workoutDate) = :month")
    List<LocalDate> findWorkoutDatesByUserAndMonth(@Param("user") UserEntity user, @Param("year") int year, @Param("month") int month);

}

