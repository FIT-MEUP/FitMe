package fitmeup.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fitmeup.entity.TrainerEntity;
import fitmeup.entity.UserEntity;

@Repository
public interface TrainerRepository extends JpaRepository<TrainerEntity, Long> {


    // ✅ 특정 역할(Role)을 가진 트레이너 목록 가져오기
    List<TrainerEntity> findByUser_Role(UserEntity.Role role);
  　List<TrainerEntity> findByTrainerId(Long trainerId);
    
	
//    List<TrainerEntity> findByTrainerIdAndStatus(Long trainerId, String status);
    // ✅ `trainerId`가 아닌 `userId` 기준으로 트레이너 조회
    Optional<TrainerEntity> findByUser_UserId(Long userId);


    // ✅ 이메일 기반으로 트레이너 ID 조회 (불필요한 UserEntity 조회 방지)
    @Query("SELECT t.trainerId FROM TrainerEntity t WHERE t.user.userEmail = :email")
    Optional<Long> findTrainerIdByUserEmail(@Param("email") String email);
    
    // ✅ UserEntity를 기반으로 TrainerEntity 찾기
    Optional<TrainerEntity> findByUser(UserEntity user);
    
//  List<TrainerEntity> findByTrainerIdAndStatus(Long trainerId, String status);
}
