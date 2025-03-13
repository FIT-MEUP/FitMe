package fitmeup.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    List<TrainerApplicationEntity> findByTrainerTrainerId(Long trainerNum);
    
    
    // 승인된 회원만 반환 
    @Query("SELECT t.user.userId FROM TrainerApplicationEntity t WHERE t.trainer.trainerId = :trainerId AND t.status = 'APPROVED'")
    List<Long> findApprovedUserIdsByTrainerId(@Param("trainerId") Long trainerId);


}
