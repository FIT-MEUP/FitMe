package fitmeup.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fitmeup.entity.WorkDataEntity;
import jakarta.transaction.Transactional;

@Repository
public interface WorkDataRepository extends JpaRepository<WorkDataEntity, Long> {
	
	@Query("SELECT w FROM WorkDataEntity w WHERE w.workout.workoutId = :workoutId")
	List<WorkDataEntity> findByWorkoutId(@Param("workoutId") Long workoutId);

	@Query("SELECT w FROM WorkDataEntity w WHERE w.workout.workoutId IN :workoutIds")
	List<WorkDataEntity> findByWorkoutIds(@Param("workoutIds") List<Long> workoutIds);

	 //특정 workoutId에 대한 영상 개수 조회
    @Query("SELECT COUNT(w) FROM WorkDataEntity w WHERE w.workout.workoutId = :workoutId")
    int countByWorkoutId(@Param("workoutId") Long workoutId);





	
}
