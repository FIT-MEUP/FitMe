package fitmeup.service;

import fitmeup.dto.PTSessionHistoryDTO;
import fitmeup.dto.TrainerApplicationDTO;
import fitmeup.entity.PTSessionHistoryEntity;
import fitmeup.entity.TrainerApplicationEntity;
import fitmeup.entity.TrainerEntity;
import fitmeup.entity.UserEntity;
import fitmeup.repository.PTSessionHistoryRepository;
import fitmeup.repository.TrainerApplicationRepository;
import fitmeup.repository.TrainerRepository;
import fitmeup.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainerApplicationService {

  private final UserRepository userRepository;
  private final TrainerRepository trainerRepository;
  private final TrainerApplicationRepository trainerApplicationRepository;
  private final PTSessionHistoryRepository ptSessionHistoryRepository;

  public boolean isAlreadyApplied(String userEmail, Long trainerId) {
    return trainerApplicationRepository.existsByUserUserEmailAndTrainerTrainerId(userEmail, trainerId);
  }


    // ✅ 새로운 userId 기반 상담 신청 여부 확인 메서드 추가(Pending 상태인 경우만 true 반환)
    public boolean isAlreadyAppliedByUserId(Long userId, Long trainerId) {
        return trainerApplicationRepository.existsByUserUserIdAndTrainerTrainerIdAndStatus(userId, trainerId, TrainerApplicationEntity.Status.Pending);
    }


  public List<TrainerApplicationDTO> getApplicationById(Long userNum, TrainerApplicationEntity.Status status) {
    Optional<TrainerEntity> trainerEntity = trainerRepository.findByUser(userRepository.findById(userNum).get());
    log.info("==========userId:{}", trainerEntity.get().getTrainerId());
    List<TrainerApplicationEntity> entityList = trainerApplicationRepository.findByTrainerTrainerId(trainerEntity.get().getTrainerId());


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
      
      PTSessionHistoryDTO dto = new PTSessionHistoryDTO();
      dto.setUserId(application.getUser().getUserId());
      dto.setChangeType(PTSessionHistoryEntity.ChangeType.Added.name());
      dto.setChangeAmount(0L);
      dto.setReason("새로운 PT계약 생성");
	  ptSessionHistoryRepository.save(PTSessionHistoryEntity.toEntity(dto));

    } else {
      throw new RuntimeException("Application not found with ID: " + applicationId);
    }
  }

    public String selectOne(Long applicationId) {
        Optional<TrainerApplicationEntity> applicationOptional = trainerApplicationRepository.findById(applicationId);
        return applicationOptional.map(application -> application.getUser().getUserName())
                .orElseThrow(() -> new RuntimeException("Application not found with ID: " + applicationId));
    }

 


  public void createApplication(Long userId, Long trainerId) {
        // 🔍 이미 신청한 기록이 있는지 확인
        if (trainerApplicationRepository.existsByUserUserIdAndTrainerTrainerId(userId, trainerId)) {
            log.warn("⚠️ 이미 상담 신청한 사용자입니다. userId={}, trainerId={}", userId, trainerId);
            throw new RuntimeException("이미 상담 신청한 내역이 있습니다.");
        }

        Optional<UserEntity> temp = userRepository.findById(userId);
        Optional<TrainerEntity> temp2 = trainerRepository.findById(trainerId);

        if (temp.isEmpty() || temp2.isEmpty()) {
            throw new RuntimeException("User or Trainer not found");
        }

        UserEntity userEntity = temp.get();
        String name = userEntity.getUserName();

        if (name == null || name.isEmpty()) {
            name = "이름 없음"; // 기본값 설정
            log.warn("⚠️ userId={}의 userName이 NULL입니다. 기본값 '이름 없음'으로 설정", userId);
        }

        TrainerApplicationDTO trainerApplicationDTO = new TrainerApplicationDTO();
        trainerApplicationDTO.setName(name);
        trainerApplicationDTO.setStatus("Pending"); // ✅ 기본값 설정

        TrainerApplicationEntity trainerApplicationEntity =
                TrainerApplicationEntity.toEntity(trainerApplicationDTO, userEntity, temp2.get());

        trainerApplicationRepository.save(trainerApplicationEntity);
        log.info("✅ 상담 신청 DB 저장 완료: userId={}, trainerId={}, name={}, status={}",
                 userId, trainerId, name, trainerApplicationEntity.getStatus());
    }
    
    // ✅ 신청 취소 상태 업데이트
    public void saveApplication(TrainerApplicationEntity application) {
        trainerApplicationRepository.save(application);
    }

    @Transactional
    public boolean cancelApplication(Long userId, Long trainerId) {
        Optional<TrainerApplicationEntity> applicationOptional =
                trainerApplicationRepository.findByUserUserIdAndTrainerTrainerId(userId, trainerId);

        if (applicationOptional.isPresent()) {
            TrainerApplicationEntity application = applicationOptional.get();
            if (application.getStatus() == TrainerApplicationEntity.Status.Pending) {
                application.setStatus(TrainerApplicationEntity.Status.Rejected);
                trainerApplicationRepository.save(application);
                log.info("✅ 상담 신청 취소됨: userId={}, trainerId={}, 새로운 상태={}", userId, trainerId, application.getStatus());
                return true;
            } else {
                log.warn("⚠️ 상담 신청이 이미 승인되었거나 거절된 상태: userId={}, trainerId={}, 현재 상태={}", 
                         userId, trainerId, application.getStatus());
                return false;
            }
        } else {
            log.warn("❌ 취소할 상담 신청을 찾을 수 없음: userId={}, trainerId={}", userId, trainerId);
            return false;
        }


		
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


    
    
    // 🔍 특정 사용자와 트레이너 간의 상담 신청 가져오기 (Pending, Rejected 모두 포함)
    public Optional<TrainerApplicationEntity> getApplicationByUserIdAndTrainerId(Long userId, Long trainerId) {
        return trainerApplicationRepository.findByUserUserIdAndTrainerTrainerId(userId, trainerId);
    }


  
  public Long findApplicationIdByUserId(Long userId) {
    List<TrainerApplicationEntity> temp = trainerApplicationRepository.findByUserUserId(userId);
    return	temp.get(0).getApplicationId();
	  
	 
//	  return trainerApplicationRepository.findByUserUserId(userId).get().getApplicationId();
  }
}
