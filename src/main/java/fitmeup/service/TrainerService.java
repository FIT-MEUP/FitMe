package fitmeup.service;

import fitmeup.dto.TrainerDTO;
import fitmeup.entity.TrainerEntity;
import fitmeup.entity.TrainerPhotoEntity;
import fitmeup.entity.UserEntity;

import fitmeup.entity.UserEntity.Role;
import fitmeup.repository.TrainerPhotoRepository;
import fitmeup.repository.TrainerRepository;
import fitmeup.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainerService {

    private final TrainerRepository trainerRepository;
    private final TrainerPhotoRepository trainerPhotoRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;


    // ✅ 모든 트레이너 목록 가져오기
    public List<TrainerEntity> getAllTrainers() {
    	// 만약 PendingTrainer 는 제외한 Trainer만 나오게 하고 싶다면 아래 주석을 풀자. 
    	return trainerRepository.findByUser_Role(UserEntity.Role.Trainer);
//        return trainerRepository.findAll();
    }

    // ✅ 특정 트레이너 정보 가져오기
    public TrainerEntity getTrainerById(Long trainerId) {
        return trainerRepository.findById(trainerId)
                .orElseThrow(() -> new IllegalArgumentException("트레이너를 찾을 수 없습니다."));
    }

    // ✅ 특정 트레이너의 모든 사진 가져오기
    public List<TrainerPhotoEntity> getTrainerPhotos(Long trainerId) {
        return trainerPhotoRepository.findByTrainer_TrainerId(trainerId);
    }
  
    // ✅ 이메일로 사용자 찾기
    public UserEntity getUserByEmail(String userEmail) {
        return userRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일의 사용자를 찾을 수 없습니다."));

    }

    
    @Transactional
    public boolean joinProc(TrainerDTO trainerDTO) {
        // 1. 중복 가입 방지를 위해 이메일, 연락처 체크 (예외 발생 X, false 반환)
        if (userRepository.findByUserEmail(trainerDTO.getUserEmail()).isPresent()) {
            log.error("이미 등록된 이메일: {}", trainerDTO.getUserEmail());
            return false;
        }
        if (userRepository.findByUserContact(trainerDTO.getUserContact()).isPresent()) {
            log.error("이미 등록된 전화번호: {}", trainerDTO.getUserContact());
            return false; // ❌ 예외 발생 X → false 반환
        }

        // 2. 비밀번호 암호화
        String encryptedPassword = bCryptPasswordEncoder.encode(trainerDTO.getPassword());

        // 3. UserEntity 생성: 트레이너도 User 테이블에 먼저 등록 (role: Trainer)
        UserEntity user = UserEntity.builder()
                .userName(trainerDTO.getUserName())
                .userEmail(trainerDTO.getUserEmail())
                .userGender(UserEntity.Gender.valueOf(trainerDTO.getUserGender()))
                .userBirthdate(trainerDTO.getUserBirthdate())
                .userContact(trainerDTO.getUserContact())
                .password(encryptedPassword)
                .role(Role.PendingTrainer) // 승인 대기 상태로 저장
                .isOnline(false)  // 기본값
                .build();

        // 4. UserEntity 저장
        user = userRepository.save(user);

        // 5. TrainerEntity 생성: 추가 정보 저장 (User와 연관관계 설정)
        TrainerEntity trainer = TrainerEntity.builder()
                .user(user)
                .specialization(trainerDTO.getSpecialization())
                .experience(trainerDTO.getExperience())
                .fee(trainerDTO.getFee())
                .shortIntro(trainerDTO.getShortIntro()) // ✅ 추가
                .bio(trainerDTO.getBio())
                .build();

        trainer = trainerRepository.save(trainer);

        // 6. 저장 결과 확인
        return trainer.getTrainerId() != null;


    }
}
