package fitmeup.service;


import java.util.Collections;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

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

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainerService {

    private final TrainerRepository trainerRepository;
    private final TrainerPhotoRepository trainerPhotoRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    // ✅ 모든 트레이너 목록 가져오기 (PendingTrainer 제외)
    public List<TrainerEntity> getAllTrainers() {
        return trainerRepository.findByUser_Role(Role.Trainer);
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

    // ✅ 트레이너 ID 가져오기 (userEmail 기반)
    public Long getTrainerIdByUserEmail(String userEmail) {//userEmail= User 테이블의 PK
        log.info("🔍 입력된 이메일: {}", userEmail); // ✅ 입력된 이메일 확인
        Long trainerId = trainerRepository.findTrainerIdByUserEmail(userEmail).orElse(null);
        log.info("🔍 조회된 trainerId: {}", trainerId); // ✅ 조회 결과 확인

        // ✅ trainerId가 null이면 DB에서 UserEntity가 제대로 연관되었는지 확인 필요
        if (trainerId == null) {
            UserEntity user = userRepository.findByUserEmail(userEmail).orElse(null);
            log.info("🔍 UserEntity 조회 결과: {}", user);
            if (user != null) {
                TrainerEntity trainer = trainerRepository.findByUser(user).orElse(null);
                log.info("🔍 TrainerEntity 조회 결과: {}", trainer);
            }
        }
        return trainerId;
    }


    // ✅ 트레이너 정보 저장
    public void saveTrainer(TrainerEntity trainer) {
        trainerRepository.save(trainer);
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
                .isOnline(false)
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

        return trainer.getTrainerId() != null;
    }

    public Long findUserId(Long trainerId) {	// 0312 수정 김준우
    	Optional<TrainerEntity> temp = trainerRepository.findById(trainerId);
    	return temp.get().getUser().getUserId();
    }

    
}
