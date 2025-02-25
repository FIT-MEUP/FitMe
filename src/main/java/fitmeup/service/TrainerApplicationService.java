package fitmeup.service;

import org.springframework.stereotype.Service;
import fitmeup.repository.TrainerApplicationRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TrainerApplicationService {

    private final TrainerApplicationRepository trainerApplicationRepository;

    public boolean isAlreadyApplied(String userEmail, Long trainerId) {
        return trainerApplicationRepository.existsByUserUserEmailAndTrainerTrainerId(userEmail, trainerId);
    }
}
