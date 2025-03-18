package fitmeup.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Sort;

import fitmeup.entity.AnnouncementEntity;

@Repository
public interface AnnouncementRepository extends JpaRepository<AnnouncementEntity, Long> {

	List<AnnouncementEntity> findByUserUserId(Long userId, Sort by);

	
//    List<AnnouncementEntity> findByUser(UserEntity userEntity, Sort by);
}
