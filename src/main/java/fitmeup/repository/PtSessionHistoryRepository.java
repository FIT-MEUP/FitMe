package fitmeup.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import fitmeup.entity.PtSessionHistoryEntity;



public interface PtSessionHistoryRepository extends JpaRepository<PtSessionHistoryRepository, Long> {
	List<PtSessionHistoryEntity> findByUserUserId(Long userId, Sort by);
}
