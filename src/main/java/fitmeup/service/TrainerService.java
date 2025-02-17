package fitmeup.service;

import java.util.List;
import java.util.logging.Logger;
import org.springframework.stereotype.Service;
import fitmeup.entity.TrainerEntity;
import fitmeup.repository.TrainerRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TrainerService {
    private final TrainerRepository trainerRepository;
    private static final Logger logger = Logger.getLogger(TrainerService.class.getName());

    public List<TrainerEntity> getAllTrainers() {
        List<TrainerEntity> trainers = trainerRepository.findAll();
        logger.info("트레이너 목록 조회됨: " + trainers.size() + "명"); // 🔥 로그 추가
        trainers.forEach(trainer -> logger.info(trainer.toString())); // 개별 트레이너 정보 출력
        return trainers;
    }
}
