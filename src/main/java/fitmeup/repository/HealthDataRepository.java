package fitmeup.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import fitmeup.entity.HealthDataEntity;

public interface HealthDataRepository extends JpaRepository<HealthDataEntity, Long> {
	
	 // ✅ 특정 사용자의 모든 건강 데이터를 조회하는 메서드 추가
  //  List<HealthDataEntity> findByUser_UserId(Long userId);
		List<HealthDataEntity> findByUserId(Long userId,  Sort by);
}