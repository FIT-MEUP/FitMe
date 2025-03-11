package fitmeup.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fitmeup.entity.TrainerEntity;
import fitmeup.entity.UserEntity;

@Repository
public interface TrainerRepository extends JpaRepository<TrainerEntity, Long> {

    List<TrainerEntity> findByTrainerId(Long trainerId);
    Optional<TrainerEntity> findByUser(UserEntity user);
	List<TrainerEntity> findByUser_Role(UserEntity.Role role);
//    List<TrainerEntity> findByTrainerIdAndStatus(Long trainerId, String status);


}
