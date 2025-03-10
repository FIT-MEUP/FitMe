package fitmeup.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fitmeup.entity.TrainerEntity;
import fitmeup.entity.UserEntity;

@Repository
public interface TrainerRepository extends JpaRepository<TrainerEntity, Long> {
	List<TrainerEntity> findByUser_Role(UserEntity.Role role);
}
