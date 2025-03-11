package fitmeup.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import fitmeup.entity.TrainerScheduleEntity;



public interface TrainerScheduleRepository extends JpaRepository<TrainerScheduleEntity, Integer> {
	List<TrainerScheduleEntity> findByTrainerTrainerId(Long trainerId);
}
