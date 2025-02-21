package fitmeup.repository;

import fitmeup.entity.TrainerPhotoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TrainerPhotoRepository extends JpaRepository<TrainerPhotoEntity, Long> {
    
    // 특정 트레이너의 모든 사진 가져오기
    List<TrainerPhotoEntity> findByTrainer_TrainerId(Long trainerId);
}
