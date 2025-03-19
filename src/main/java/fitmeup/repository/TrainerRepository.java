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


    List<TrainerEntity> findByTrainerId(Long trainerId);



    // ✅ 특정 역할(Role)을 가진 트레이너 목록 가져오기
    List<TrainerEntity> findByUser_Role(UserEntity.Role role);
    Optional<TrainerEntity> findById(Long trainerId);    
	
//    List<TrainerEntity> findByTrainerIdAndStatus(Long trainerId, String status);
    // ✅ `trainerId`가 아닌 `userId` 기준으로 트레이너 조회
    Optional<TrainerEntity> findByUser_UserId(Long userId);

    // ✅ 사용자의 이메일로 트레이너 정보를 조회
    Optional<TrainerEntity> findByUser_UserEmail(String email);
    
    // ✅ UserEntity를 기반으로 TrainerEntity 찾기
    Optional<TrainerEntity> findByUser(UserEntity user);



	//List<TrainerEntity> findByUserId(Long userId);
    
//  List<TrainerEntity> findByTrainerIdAndStatus(Long trainerId, String status);
}
