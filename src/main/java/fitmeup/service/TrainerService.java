package fitmeup.service;

import fitmeup.entity.TrainerEntity;
import fitmeup.entity.TrainerPhotoEntity;
import fitmeup.repository.TrainerPhotoRepository;
import fitmeup.repository.TrainerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainerService {

    private final TrainerRepository trainerRepository;
    private final TrainerPhotoRepository trainerPhotoRepository;

    // ✅ 모든 트레이너 목록 가져오기 (메인 페이지에서 사용)
    public List<TrainerEntity> getAllTrainers() {
        return trainerRepository.findAll();
    }

    // ✅ 특정 트레이너 정보 가져오기 (상세 페이지에서 사용)
    public TrainerEntity getTrainerById(Long trainerId) {
        return trainerRepository.findById(trainerId)
                .orElseThrow(() -> new IllegalArgumentException("트레이너를 찾을 수 없습니다."));
    }

    // ✅ 특정 트레이너의 모든 사진 가져오기
    public List<TrainerPhotoEntity> getTrainerPhotos(Long trainerId) {
        return trainerPhotoRepository.findByTrainer_TrainerId(trainerId);
    }
}
