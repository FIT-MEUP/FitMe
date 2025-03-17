package fitmeup.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import fitmeup.entity.PTSessionHistoryEntity;




public interface PTSessionHistoryRepository extends JpaRepository<PTSessionHistoryEntity, Long> {
	List<PTSessionHistoryEntity> findByUserUserId(Long userId, Sort by);
}
