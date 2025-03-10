package fitmeup.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import fitmeup.entity.TrainerApplicationEntity;

@Repository
public interface TrainerApplicationRepository extends JpaRepository<TrainerApplicationEntity, Long> {
    boolean existsByUserUserEmailAndTrainerTrainerId(String userEmail, Long trainerId);


    //특정 userId에 해당하는 TrainerApplicationEntity에서 연결된 TrainerEntity의 trainerId를 조회하는 것
//  @Query("select ta.trainer.trainerId from TrainerApplicationEntity ta where ta.user.userId = :userId")
//  Long findTrainerIdByUserId(@Param("userId") Long userId);
  //User테이블에 있는 UserId를 찾는 메소드
  Optional<TrainerApplicationEntity> findByUserUserId(Long userId);

}
