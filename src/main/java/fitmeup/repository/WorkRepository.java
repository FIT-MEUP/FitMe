package fitmeup.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fitmeup.entity.WorkEntity;

@Repository
public interface WorkRepository extends JpaRepository<WorkEntity, Long> {
    List<WorkEntity> findByUserUserIdAndWorkoutDate(Long userId, LocalDate workoutDate);

	 //  특정 날짜의 운동 기록 조회
    @Query("SELECT w FROM WorkEntity w WHERE w.workoutDate = :workoutDate")
    List<WorkEntity> findWorkoutsByDate(@Param("workoutDate") LocalDate workoutDate);

    //검색 기능 
    @Query("SELECT w FROM WorkEntity w WHERE LOWER(w.exercise) LIKE LOWER(CONCAT('%', :exercise, '%'))")
    List<WorkEntity> findByExerciseContainingIgnoreCase(@Param("exercise") String exercise);


	
}
