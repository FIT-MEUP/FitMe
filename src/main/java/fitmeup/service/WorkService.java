package fitmeup.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import fitmeup.dto.WorkDTO;
import fitmeup.entity.TrainerApplicationEntity;
import fitmeup.entity.TrainerEntity;
import fitmeup.entity.UserEntity;
import fitmeup.entity.WorkDataEntity;
import fitmeup.entity.WorkEntity;
import fitmeup.repository.TrainerApplicationRepository;
import fitmeup.repository.TrainerRepository;
import fitmeup.repository.UserRepository;
import fitmeup.repository.WorkDataRepository;
import fitmeup.repository.WorkRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class WorkService {
	
	private final WorkRepository workRepository;
	private final UserRepository userRepository;
    private final WorkDataRepository workDataRepository;
    private final TrainerRepository trainerRepository;
    private final TrainerApplicationRepository trainerApplicationRepository;

    @Value("${upload.video.path}")
    private String uploadDir; // 운동 게시판 영상 업로드 경로
    

   // 로그인한 사용자의 Trainer ID 가져오기
    public Long getTrainerId(Long userId) {
        return trainerRepository.findByUser_UserId(userId)
                .map(TrainerEntity::getTrainerId)
                .orElse(null);
    }

    // 특정 Trainer의 승인된 회원 목록 가져오기
    public List<Long> getApprovedUserIds(Long trainerId) {
        return trainerApplicationRepository.findByTrainerTrainerId(trainerId).stream()
                .filter(app -> app.getStatus() == TrainerApplicationEntity.Status.Approved)
                .map(app -> app.getUser().getUserId())
                .collect(Collectors.toList());
    }
    
    /**
     * 📌 운동 기록 접근 권한 체크
     * - 일반 회원: 본인 기록만 접근 가능
     * - 트레이너: 승인된 회원의 기록만 접근 가능
     */
    public boolean hasAccessToWorkout(Long targetUserId, Long loggedInUserId, boolean isTrainer) {
        if (targetUserId.equals(loggedInUserId)) {
            return true; // 본인이라면 OK
        }
        if (!isTrainer) {
            return false; // 일반 회원이 다른 회원 데이터 접근 불가
        }
        
        Long trainerId = getTrainerId(loggedInUserId);
        List<Long> approvedUserIds = trainerId != null ? getApprovedUserIds(trainerId) : List.of();
        
        return approvedUserIds.contains(targetUserId);
    }

    
    

   // 특정 Trainer의 승인된 회원들의 특정 날짜 운동 기록 조회

    public List<WorkDTO> getWorkoutsByTrainerAndDate(List<Long> approvedUserIds, LocalDate workoutDate) {
        List<WorkEntity> workouts = workRepository.findByUserUserIdInAndWorkoutDate(approvedUserIds, workoutDate);
        return workouts.stream().map(WorkDTO::fromEntity).collect(Collectors.toList());
    }

    //특정 workoutId 목록을 기반으로 운동 기록 조회 (검색 기능에 사용)
    public List<WorkDTO> getWorkoutsByIds(List<Long> workoutIds) {
        List<WorkEntity> workouts = workRepository.findByWorkoutIdIn(workoutIds);
        return workouts.stream().map(WorkDTO::fromEntity).collect(Collectors.toList());
    }

    //  특정 운동 기록 조회
    public WorkDTO getWorkoutById(Long id) {
        Optional<WorkEntity> workout = workRepository.findById(id);
        return workout.map(WorkDTO::fromEntity).orElse(null);
    }
    
//	 특정 User의 특정 날짜 운동 기록 조회
    public List<WorkDTO> getWorkoutsByUserAndDate(Long userId, LocalDate workoutDate) {
        List<WorkEntity> workouts = workRepository.findByUserUserIdAndWorkoutDate(userId, workoutDate);
        return workouts.stream().map(WorkDTO::fromEntity).collect(Collectors.toList());
    }


// 운동 기록 저장 (로그인한 사용자 정보 반영)
    public WorkDTO saveWorkout(WorkDTO workDTO, Long loggedInUserId) {
    	
    	 boolean isTrainer = userRepository.findById(loggedInUserId)
                 .map(user -> user.getRole().equals("Trainer"))
                 .orElse(false);

         if (!hasAccessToWorkout(workDTO.getUserId(), loggedInUserId, isTrainer)) {
             throw new RuntimeException("🚨 운동 기록을 저장할 권한이 없습니다! 🚨");
         }

         UserEntity user = userRepository.findById(workDTO.getUserId())
                 .orElseThrow(() -> new RuntimeException("User not found with ID: " + workDTO.getUserId()));

         WorkEntity workEntity = WorkEntity.builder()
                 .part(workDTO.getPart())
                 .exercise(workDTO.getExercise())
                 .sets(workDTO.getSets())
                 .reps(workDTO.getReps())
                 .weight(workDTO.getWeight())
                 .workoutDate(workDTO.getWorkoutDate())
                 .user(user)
                 .build();

         workRepository.save(workEntity);
         return WorkDTO.fromEntity(workEntity);
    }

	
	// 운동 기록 수정 
	@Transactional
	public boolean updateWorkout(Long workoutId, WorkDTO updatedWorkout, Long loggedInUserId) {
		
		 WorkEntity workout = workRepository.findById(workoutId)
		            .orElseThrow(() -> new RuntimeException("운동 기록을 찾을 수 없습니다."));

		    // 🔥 사용자 역할 확인 (Trainer or User)
		    UserEntity.Role userRole = userRepository.findById(loggedInUserId)
		            .map(UserEntity::getRole)
		            .orElse(UserEntity.Role.User);

		    boolean isTrainer = userRole == UserEntity.Role.Trainer;
		    Long trainerId = getTrainerId(loggedInUserId);
		    List<Long> approvedUserIds = trainerId != null ? getApprovedUserIds(trainerId) : new ArrayList<>();

		    // 🔥 디버깅 로그 추가
		    System.out.println("🔥 workout userId: " + workout.getUser().getUserId());
		    System.out.println("🔥 loggedInUserId: " + loggedInUserId);
		    System.out.println("🔥 isTrainer: " + isTrainer);
		    System.out.println("🔥 trainerId: " + trainerId);
		    System.out.println("🔥 approvedUserIds: " + approvedUserIds);

		    // 🚨 권한 체크: 본인 or 승인된 트레이너만 수정 가능
		    if (!workout.getUser().getUserId().equals(loggedInUserId) &&
		        (!isTrainer || !approvedUserIds.contains(workout.getUser().getUserId()))) {
		        throw new RuntimeException("🚨 운동 기록을 수정할 권한이 없습니다! 🚨");
		    }

		    // ✅ DTO 데이터를 엔티티에 반영
		    workout.updateFromDTO(updatedWorkout);
		    workRepository.save(workout);

		    return true;  // ✅ 수정 성공 시 true 반환
	}
	// 삭제 
	@Transactional
	public boolean deleteWorkout(Long id, Long loggedInUserId) {
		WorkEntity workout = workRepository.findById(id)
	            .orElseThrow(() -> new RuntimeException("운동 기록을 찾을 수 없습니다."));

	    UserEntity.Role userRole = userRepository.findById(loggedInUserId)
	            .map(UserEntity::getRole)
	            .orElse(UserEntity.Role.User);

	    boolean isTrainer = userRole == UserEntity.Role.Trainer;
	    Long trainerId = getTrainerId(loggedInUserId);
	    List<Long> approvedUserIds = trainerId != null ? getApprovedUserIds(trainerId) : new ArrayList<>();

	    // 🔥 디버깅 로그 추가
	    System.out.println("🔥 workout userId: " + workout.getUser().getUserId());
	    System.out.println("🔥 loggedInUserId: " + loggedInUserId);
	    System.out.println("🔥 isTrainer: " + isTrainer);
	    System.out.println("🔥 trainerId: " + trainerId);
	    System.out.println("🔥 approvedUserIds: " + approvedUserIds);

	    // 🚨 권한 체크: 본인 or 승인된 트레이너만 삭제 가능
	    if (!workout.getUser().getUserId().equals(loggedInUserId) &&
	        (!isTrainer || !approvedUserIds.contains(workout.getUser().getUserId()))) {
	        throw new RuntimeException("🚨 삭제할 권한이 없습니다! 🚨");
	    }

	    workRepository.delete(workout);
	    return true;  // ✅ 삭제 성공 시 true 반환
	}
	
	 // 영상 업로드 기능: 파일 저장 후 WorkDataEntity에 기록
    @Transactional
    public String uploadVideo(MultipartFile file, Long workoutId, Long loggedInUserId) {
        String originalFileName = file.getOriginalFilename();
        String savedFileName = System.currentTimeMillis() + "_" + originalFileName;
        Path targetPath = Paths.get(uploadDir).resolve(savedFileName);
        
        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save video file", e);
        }
        
     // 운동 기록과 연결
        WorkEntity workout = workRepository.findById(workoutId)
                .orElseThrow(() -> new RuntimeException("Workout not found"));
        
        boolean isTrainer = userRepository.findById(loggedInUserId)
                .map(user -> {
                    System.out.println("🔥 로그인한 사용자 role: " + user.getRole());  // role 확인
                    return user.getRole().equals("Trainer");
                })
                .orElse(false);

        Long trainerId = getTrainerId(loggedInUserId);

        List<Long> approvedUserIds = trainerId != null ? getApprovedUserIds(trainerId) : List.of();
        
        System.out.println("🔥 workout userId: " + workout.getUser().getUserId());
        System.out.println("🔥 loggedInUserId: " + loggedInUserId);
        System.out.println("🔥 isTrainer: " + isTrainer);
        System.out.println("🔥 trainerId: " + trainerId);
        System.out.println("🔥 approvedUserIds: " + approvedUserIds);

        // 일반 회원은 자기 workout만 업로드 가능
        if (!isTrainer && !workout.getUser().getUserId().equals(loggedInUserId)) {
            throw new RuntimeException("🚨 일반 회원은 본인 workout만 업로드할 수 있습니다! 🚨");
        }

        // 트레이너는 승인된 회원의 workout만 업로드 가능
        if (isTrainer && !approvedUserIds.contains(workout.getUser().getUserId())) {
            throw new RuntimeException("🚨 트레이너는 승인된 회원의 workout만 업로드할 수 있습니다! 🚨");
        }

        WorkDataEntity videoEntity = WorkDataEntity.builder()
                .originalFileName(originalFileName)
                .savedFileName(savedFileName)
                .workout(workout)  // workout과 연결
                .build();

        workDataRepository.save(videoEntity);
        
        workDataRepository.flush();  //  강제 DB 반영
            
        return savedFileName;
    }
    
    //동영상 재업로드 (특정 workoutId의 영상 조회)
    @Transactional
    public String getWorkoutVideo(Long workoutId, Long loggedInUserId) {
    	WorkDTO workout = getWorkoutById(workoutId);

        if (workout == null) {
            throw new RuntimeException("해당 운동 기록을 찾을 수 없습니다.");
        }

        boolean isTrainer = userRepository.findById(loggedInUserId)
                .map(user -> user.getRole().equals("Trainer"))
                .orElse(false);

        Long trainerId = trainerRepository.findByUser_UserId(loggedInUserId)
                .map(TrainerEntity::getTrainerId)
                .orElse(null);

        List<Long> approvedUserIds = trainerId != null ? getApprovedUserIds(trainerId) : List.of();

        // 본인 또는 승인된 트레이너만 조회 가능
        if (!workout.getUserId().equals(loggedInUserId) &&
            (!isTrainer || !approvedUserIds.contains(workout.getUserId()))) {
            throw new RuntimeException("영상을 조회할 권한이 없습니다.");
        }

        List<WorkDataEntity> videos = workDataRepository.findByWorkoutId(workoutId);
        return videos.isEmpty() ? null : videos.get(0).getSavedFileName();
    }
    
    // 특정 workoutId에 영상이 있는지 확인하는 메서드 추가
    public boolean hasVideo(Long workoutId) {
        return workDataRepository.countByWorkoutId(workoutId) > 0;
    }
    
// 동영상 삭제 
    @Transactional
    public boolean deleteWorkoutVideo(Long workoutId, Long loggedInUserId) {
        List<WorkDataEntity> videos = workDataRepository.findByWorkoutId(workoutId);

        if (videos.isEmpty()) {
            return false;
        }

        WorkEntity workout = workRepository.findById(workoutId)
                .orElseThrow(() -> new RuntimeException("Workout not found"));

        boolean isTrainer = userRepository.findById(loggedInUserId)
                .map(user -> user.getRole().equals("Trainer"))
                .orElse(false);

        Long trainerId = trainerRepository.findByUser_UserId(loggedInUserId)
                .map(TrainerEntity::getTrainerId)
                .orElse(null);

        List<Long> approvedUserIds = trainerId != null ? getApprovedUserIds(trainerId) : List.of();

        // 본인 또는 승인된 트레이너만 삭제 가능
        if (!workout.getUser().getUserId().equals(loggedInUserId) &&
            (!isTrainer || !approvedUserIds.contains(workout.getUser().getUserId()))) {
            throw new RuntimeException("삭제할 권한이 없습니다.");
        }

        boolean isDeleted = false;
        for (WorkDataEntity video : videos) {
            Path filePath = Paths.get(uploadDir, video.getSavedFileName());

            try {
                Files.deleteIfExists(filePath);
                isDeleted = true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            workDataRepository.delete(video);
        }
        return isDeleted;
    }
    
    // 영상 정보 Map 반환: workoutId -> savedFileName (있다면 첫 번째 영상 파일의 이름)@Transactional
       @Transactional
       public Map<Long, String> getVideoMapByWorkoutDate(LocalDate workoutDate) {
           List<WorkEntity> workouts = workRepository.findWorkoutsByDate(workoutDate);
           Map<Long, String> videoMap = new HashMap<>();

           for (WorkEntity workout : workouts) {
               workDataRepository.flush(); // 강제 최신화

               List<WorkDataEntity> videos = workDataRepository.findByWorkoutId(workout.getWorkoutId());



               if (!videos.isEmpty()) {
                   String savedFileName = videos.get(0).getSavedFileName();
    
                   videoMap.put(workout.getWorkoutId(), savedFileName);
               } 
           }

           return videoMap;
       }
       
       // workoutId 목록으로 영상 가져오기 
       @Transactional
       public Map<Long, String> getVideoMapByWorkoutIds(List<Long> workoutIds) {
           Map<Long, String> videoMap = new HashMap<>();

           for (Long workoutId : workoutIds) {
               List<WorkDataEntity> videos = workDataRepository.findByWorkoutId(workoutId);

               if (!videos.isEmpty()) {
                   String savedFileName = videos.get(0).getSavedFileName();
               
                   videoMap.put(workoutId, savedFileName);
               }
           }
           return videoMap;
       }
       
       public List<WorkDTO> searchWorkoutsByExercise(String exercise) {
           List<WorkEntity> workouts = workRepository.findByExerciseContainingIgnoreCase(exercise);
           return workouts.stream().map(WorkDTO::fromEntity).collect(Collectors.toList());
       }
      
}