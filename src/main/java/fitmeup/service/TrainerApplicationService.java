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

import fitmeup.dto.TrainerApplicationDTO;
import fitmeup.entity.TrainerApplicationEntity;
import fitmeup.entity.TrainerEntity;
import fitmeup.entity.UserEntity;
import fitmeup.repository.TrainerApplicationRepository;
import fitmeup.repository.TrainerRepository;
import fitmeup.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainerApplicationService {
	private final UserRepository userRepository;
	private final TrainerRepository trainerRepository;
    private final TrainerApplicationRepository trainerApplicationRepository;

    public boolean isAlreadyApplied(String userEmail, Long trainerId) {
        return trainerApplicationRepository.existsByUserUserEmailAndTrainerTrainerId(userEmail, trainerId);
    }


    public List<TrainerApplicationDTO> getApplicationById(Long trainerNum, TrainerApplicationEntity.Status status) {
        Optional<TrainerEntity> trainerEntity = trainerRepository.findByUser_UserId(trainerNum);

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

	public void createApplication(Long userId, Long trainerId) {
		Optional<UserEntity> temp = userRepository.findById(userId);
		Optional<TrainerEntity> temp2 = trainerRepository.findById(trainerId);
		String name = temp.get().getUserName();
	    TrainerApplicationDTO trainerApplicationDTO= new TrainerApplicationDTO();
	    trainerApplicationDTO.setName(name);
	    //trainerApplicationDTO.setStatus("Pending");
	    
	    TrainerApplicationEntity trainerApplicationEntity =
	    		TrainerApplicationEntity.toEntity(trainerApplicationDTO, temp.get(), temp2.get());
	    trainerApplicationRepository.save(trainerApplicationEntity);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	// 박노은 / 0315 : 특정 트레이너가 특정 회원을 승인했는지 확인  
		public boolean isTrainerOfUser(Long trainerId, Long userId) {
		    List<Long> approvedUserIds = getTrainerMembers(trainerId)
		            .stream().map(UserEntity::getUserId).toList();
		    
		    return approvedUserIds.contains(userId);
		}

	    //  박노은 / 0315
	    public List<UserEntity> getTrainerMembers(Long userId) {
	        // trainer 테이블에서 현재 로그인한 사용자의 trainer_id 조회
	        Long trainerId = trainerRepository.findByUser_UserId(userId)
	            .map(TrainerEntity::getTrainerId)
	            .orElseThrow(() -> new RuntimeException("현재 로그인한 사용자는 트레이너가 아닙니다."));

	        // 트레이너 ID를 기반으로 `Approved` 상태인 회원 조회
	        return trainerApplicationRepository.findApprovedUsersByTrainerId(trainerId, TrainerApplicationEntity.Status.Approved);
	    }


}
