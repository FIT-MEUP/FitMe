package fitmeup.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fitmeup.entity.TrainerApplicationEntity;
import fitmeup.entity.UserEntity;

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


    // 일반 사용자가 상담 신청한 내역이 존재하는지 확인
    boolean existsByUserUserEmail(String userEmail);
    
    // 특정 사용자가 특정 트레이너에 대해 신청한 내역 조회
    Optional<TrainerApplicationEntity> findByUserUserEmailAndTrainerTrainerId(String userEmail, Long trainerId);
    
    // 특정 사용자가 특정 트레이너에 대해 신청한 내역 삭제 (신청 취소 기능 구현 시)
    void deleteByUserUserEmailAndTrainerTrainerId(String userEmail, Long trainerId);
    
    // ✅ userId 기반 상담 신청 여부 확인
    boolean existsByUserUserIdAndTrainerTrainerId(Long userId, Long trainerId);

    // ✅ userId 기반 특정 사용자가 특정 트레이너에 대해 신청한 내역 조회
    Optional<TrainerApplicationEntity> findByUserUserIdAndTrainerTrainerId(Long userId, Long trainerId);

    // ✅ userId 기반 일반 사용자의 상담 신청 여부 확인
    boolean existsByUserUserId(Long userId);

    // ✅ userId 기반 특정 사용자가 특정 트레이너에 대해 신청한 내역 삭제
    void deleteByUserUserIdAndTrainerTrainerId(Long userId, Long trainerId);

    // ✅ 특정 상태(Pending)인 상담 신청 여부 확인
    boolean existsByUserUserIdAndTrainerTrainerIdAndStatus(Long userId, Long trainerId, TrainerApplicationEntity.Status status);

    // ✅ userId 기반 특정 상태 목록에 해당하는 상담 신청 여부 확인
    boolean existsByUserUserIdAndStatusIn(Long userId, List<TrainerApplicationEntity.Status> statuses);

    // 박노은 / 0315
    @Query("SELECT ta.user FROM TrainerApplicationEntity ta WHERE ta.trainer.trainerId = :trainerId AND ta.status = :status")
    List<UserEntity> findApprovedUsersByTrainerId(@Param("trainerId") Long trainerId, @Param("status") TrainerApplicationEntity.Status status);

    // 박노은 / 0315
    @Query("SELECT ta.trainer.trainerId FROM TrainerApplicationEntity ta WHERE ta.user.userId = :userId")
    Optional<Long> findTrainerIdByUserId(@Param("userId") Long userId);
}
