package fitmeup.service;

import fitmeup.dto.UserDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import fitmeup.dto.ApproveRequestDTO;
import fitmeup.dto.TrainerApplicationDTO;
import fitmeup.entity.TrainerApplicationEntity;
import fitmeup.entity.TrainerApplicationEntity.Status;
import fitmeup.repository.TrainerApplicationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainerApplicationService {

    private final TrainerApplicationRepository trainerApplicationRepository;

    public boolean isAlreadyApplied(String userEmail, Long trainerId) {
        return trainerApplicationRepository.existsByUserUserEmailAndTrainerTrainerId(userEmail, trainerId);
    }

    public List<TrainerApplicationDTO> getApplicationById(Long trainerNum, TrainerApplicationEntity.Status status) {
        List<TrainerApplicationEntity> entityList = trainerApplicationRepository.findByTrainerTrainerId(trainerNum);
        List<TrainerApplicationDTO> dtoList = new ArrayList<>();

        entityList.forEach(entity -> {
            if (entity.getStatus() == status) {
                dtoList.add(TrainerApplicationDTO.toDTO(entity));
            }
        });

        return dtoList;
    }

    @Transactional
    public void updateApplicationStatus(Long applicationId, TrainerApplicationEntity.Status status) {
        Optional<TrainerApplicationEntity> applicationOptional = trainerApplicationRepository.findById(applicationId);

        if (applicationOptional.isPresent()) {
            TrainerApplicationEntity application = applicationOptional.get();
            application.setStatus(status);
            trainerApplicationRepository.save(application);
        } else {
            throw new RuntimeException("Application not found with ID: " + applicationId);
        }
    }

    public String selectOne(Long applicationId) {
        Optional<TrainerApplicationEntity> applicationOptional = trainerApplicationRepository.findById(applicationId);

        if (applicationOptional.isPresent()) {
            TrainerApplicationEntity application = applicationOptional.get();
        	return application.getUser().getUserName();
        } else {
            throw new RuntimeException("Application not found with ID: " + applicationId);
        }
	}

}
