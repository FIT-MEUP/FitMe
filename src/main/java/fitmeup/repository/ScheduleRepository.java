package fitmeup.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import fitmeup.entity.ScheduleEntity;

public interface ScheduleRepository extends JpaRepository<ScheduleEntity, Integer> {
	List<ScheduleEntity> findByTrainerTrainerId(Long trainerId);
	
	   // 사용자(userId)로 예약을 조회하는 메소드 추가
    List<ScheduleEntity> findByUserUserId(Long userId);
}
