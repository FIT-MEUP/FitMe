package fitmeup.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import fitmeup.entity.HealthDataEntity;

public interface HealthDataRepository extends JpaRepository<HealthDataEntity, Long> {
	
	
  //  List<HealthDataEntity> findByUser_UserId(Long userId);
		List<HealthDataEntity> findByUserId(Long userId, Sort by);
}