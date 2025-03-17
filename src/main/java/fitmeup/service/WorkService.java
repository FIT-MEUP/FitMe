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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import fitmeup.dto.WorkDTO;
import fitmeup.entity.TrainerApplicationEntity;
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

@Service
@RequiredArgsConstructor
public class WorkService {
	
	private final WorkRepository workRepository;
	private final UserRepository userRepository;
    private final WorkDataRepository workDataRepository;
    private final TrainerRepository trainerRepository;
    private final TrainerApplicationRepository trainerApplicationRepository;
    private final TrainerApplicationService trainerApplicationService;

    @Value("${upload.video.path}")
    private String uploadDir; // 운동 게시판 영상 업로드 경로
    
   // // 트레이너가 특정 회원의 승인된 트레이너인지 확인하는 메서드
    private boolean isTrainerOfUser(Long trainerId, Long memberId) {
        return trainerApplicationRepository.existsByUserUserIdAndTrainerTrainerIdAndStatus(
            memberId, trainerId, TrainerApplicationEntity.Status.Approved
        );
    }
    
    
   // 특정 Trainer의 승인된 회원들의 특정 날짜 운동 기록 조회

    public List<WorkDTO> getWorkoutsByTrainerAndDate(List<Long> approvedUserIds, LocalDate workoutDate) {
        List<WorkEntity> workouts = workRepository.findByUserUserIdInAndWorkoutDate(approvedUserIds, workoutDate);
        return workouts.stream().map(WorkDTO::fromEntity).collect(Collectors.toList());
    }

    ////특정 workoutId 목록을 기반으로 운동 기록 조회 (검색 기능에 사용)
    public List<WorkDTO> getWorkoutsByIds(List<Long> workoutIds) {
        List<WorkEntity> workouts = workRepository.findByWorkoutIdIn(workoutIds);
        return workouts.stream().map(WorkDTO::fromEntity).collect(Collectors.toList());
    }

    ////  특정 운동 기록 조회
    	public WorkDTO getWorkoutById(Long id) {
    	    return workRepository.findById(id)
    	            .map(WorkDTO::fromEntity)
    	            .orElseThrow(() -> new RuntimeException("운동 기록을 찾을 수 없습니다."));
    	}
    
//	 특정 User의 특정 날짜 운동 기록 조회
    	public List<WorkDTO> getWorkoutsByUserAndDate(Long userId, LocalDate workoutDate) {

    	    List<WorkEntity> workouts = workRepository.findByUserUserIdAndWorkoutDate(userId, workoutDate);
    	    return workouts.stream().map(WorkDTO::fromEntity).toList();
    	}
  
////운동 기록 조회 (회원은 본인만, 트레이너는 승인된 회원의 운동 기록만 조회 가능)
   public List<WorkDTO> getUserWorkoutsByDate(Long userId, LocalDate workoutDate, Long loginUserId, String role) {
	   
	   //  userId가 null이면 현재 로그인한 사용자 ID로 설정 (새로운 변수로 할당)
	    final Long targetUserId = (userId == null) ? loginUserId : userId;	    

	    // 일반 사용자는 본인의 기록만 조회 가능
	    if (!"Trainer".equals(role) && !targetUserId.equals(loginUserId)) {
	        throw new RuntimeException("본인의 운동 기록만 조회할 수 있습니다.");
	    }

	 // 트레이너인 경우 승인된 회원만 조회 가능
	    if ("Trainer".equals(role) && !targetUserId.equals(loginUserId)) {
	        List<UserEntity> trainerMembers = trainerApplicationService.getTrainerMembers(loginUserId);
	        List<Long> approvedUserIds = trainerMembers.stream().map(UserEntity::getUserId).toList();

	        if (!approvedUserIds.contains(targetUserId)) {
	            throw new RuntimeException("🚨 이 회원의 운동 기록을 조회할 권한이 없습니다!");
	        }
	    }
	    return workRepository.findByUserUserIdAndWorkoutDate(targetUserId, workoutDate)
	            .stream().map(WorkDTO::fromEntity)
	            .toList();
   }
   
//// 운동 기록 저장 (로그인한 사용자 정보 반영)
    public WorkDTO saveWorkout(WorkDTO workDTO, Long loginUserId, String role) {
    	
    	if ("Trainer".equals(role)) {
    	    if (!trainerApplicationService.isTrainerOfUser(loginUserId, workDTO.getUserId())) {
    	        throw new RuntimeException("이 회원의 운동 기록을 추가할 권한이 없습니다.");
    	    }
        } else if (!workDTO.getUserId().equals(loginUserId)) {
            throw new RuntimeException("본인의 운동 기록만 추가할 수 있습니다.");
        }

        UserEntity user = userRepository.findById(workDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        WorkEntity workEntity = WorkEntity.builder()
                .user(user)
                .part(workDTO.getPart())
                .exercise(workDTO.getExercise())
                .sets(workDTO.getSets())
                .reps(workDTO.getReps())
                .weight(workDTO.getWeight())
                .workoutDate(workDTO.getWorkoutDate())
                .build();

        workRepository.save(workEntity);
        return WorkDTO.fromEntity(workEntity);
    }
    
	//// 운동 기록 수정 
    @Transactional
    public boolean updateWorkout(Long id, WorkDTO workDTO, Long loginUserId, String role) {
        Optional<WorkEntity> optionalWorkout = workRepository.findById(id);
        if (optionalWorkout.isEmpty()) {
            return false;
        }

        WorkEntity workout = optionalWorkout.get();
        Long targetUserId = workout.getUser().getUserId(); // 수정 대상 userId

        // 일반 사용자는 본인의 운동 기록만 수정 가능
        if (!"Trainer".equals(role) && !targetUserId.equals(loginUserId)) {
            System.out.println("🚨 [WorkService] 본인의 운동 기록만 수정 가능!");
            throw new RuntimeException("본인의 운동 기록만 수정할 수 있습니다.");
        }

        // 트레이너인 경우 승인된 회원 목록을 가져와 수정 권한 체크
        if ("Trainer".equals(role) && !targetUserId.equals(loginUserId)) {
            // 트레이너 승인된 회원 목록을 가져오는 방식 통일
            List<Long> approvedUserIds = trainerApplicationService.getTrainerMembers(loginUserId)
                    .stream().map(UserEntity::getUserId).toList();
           
            if (!approvedUserIds.contains(targetUserId)) {
                System.out.println("🚨 [WorkService] 트레이너가 승인한 회원이 아님! 수정 불가");
                throw new RuntimeException("이 회원의 운동 기록을 수정할 권한이 없습니다.");
            }
        }

        // 운동 기록 수정
        workout.setPart(workDTO.getPart());
        workout.setExercise(workDTO.getExercise());
        workout.setSets(workDTO.getSets());
        workout.setReps(workDTO.getReps());
        workout.setWeight(workDTO.getWeight());
        workout.setWorkoutDate(workDTO.getWorkoutDate());

        workRepository.save(workout);
        return true;
    }
	
	//// 삭제 
	@Transactional
	public boolean deleteWorkout(Long workoutId, Long loginUserId, String role) {
		Optional<WorkEntity> workoutOpt = workRepository.findById(workoutId);
        if (workoutOpt.isEmpty()) {
            return false;
        }

        WorkEntity workout = workoutOpt.get();

        if ("Trainer".equals(role)) {
            if (!trainerApplicationService.isTrainerOfUser(loginUserId, workout.getUser().getUserId())) {
                throw new RuntimeException("이 회원의 운동 기록을 삭제할 권한이 없습니다.");
            }
        } else if (!workout.getUser().getUserId().equals(loginUserId)) {
            throw new RuntimeException("본인의 운동 기록만 삭제할 수 있습니다.");
        }

        workRepository.delete(workout);
        return true;
	}
	
	//// 영상 업로드 기능: 파일 저장 후 WorkDataEntity에 기록
    @Transactional
    public String uploadVideo(MultipartFile file, Long workoutId, Long loggedInUserId, String role) {
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

        // 트레이너 또는 본인 여부 확인
        if ("Trainer".equals(role)) {
            if (!trainerApplicationService.isTrainerOfUser(loggedInUserId, workout.getUser().getUserId())) {
                throw new RuntimeException("🚨 트레이너는 승인된 회원의 workout만 업로드할 수 있습니다! 🚨");
            }
        } else if (!workout.getUser().getUserId().equals(loggedInUserId)) {
            throw new RuntimeException("🚨 일반 회원은 본인 workout만 업로드할 수 있습니다! 🚨");
        }

        WorkDataEntity videoEntity = WorkDataEntity.builder()
                .originalFileName(originalFileName)
                .savedFileName(savedFileName)
                .workout(workout)
                .build();

        workDataRepository.save(videoEntity);
        workDataRepository.flush();

        return savedFileName;
    }
    
    ////동영상 재업로드 (특정 workoutId의 영상 조회)
    @Transactional
    public String getWorkoutVideo(Long workoutId, Long loggedInUserId, String role) {
        WorkDTO workout = getWorkoutById(workoutId);

        if (workout == null) {
            throw new RuntimeException("해당 운동 기록을 찾을 수 없습니다.");
        }

        boolean hasPermission = "Trainer".equals(role)
                ? trainerApplicationService.isTrainerOfUser(loggedInUserId, workout.getUserId()) 
                : workout.getUserId().equals(loggedInUserId);

        if (!hasPermission) {
            System.out.println("🚨 [WorkService] 운동 영상 조회 권한 없음!");
            throw new RuntimeException("영상을 조회할 권한이 없습니다.");
        }

        List<WorkDataEntity> videos = workDataRepository.findByWorkoutId(workoutId);

        return videos.isEmpty() ? null : videos.get(0).getSavedFileName();
    }

    
//// 동영상 삭제 
    @Transactional
    public boolean deleteWorkoutVideo(Long workoutId, Long loggedInUserId, String role) {
        List<WorkDataEntity> videos = workDataRepository.findByWorkoutId(workoutId);

        if (videos.isEmpty()) {
            System.out.println("❌ [WorkService] 삭제할 영상이 존재하지 않습니다.");
            return false;
        }

        WorkEntity workout = workRepository.findById(workoutId)
                .orElseThrow(() -> new RuntimeException("운동 기록을 찾을 수 없습니다."));

        boolean hasPermission = "Trainer".equals(role)
                ? trainerApplicationService.isTrainerOfUser(loggedInUserId, workout.getUser().getUserId()) 
                : workout.getUser().getUserId().equals(loggedInUserId);


        if (!hasPermission) {
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

    
   //// 영상 정보 Map 반환: workoutId -> savedFileName (있다면 첫 번째 영상 파일의 이름)@Transactional
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
       
      //// workoutId 목록으로 영상 가져오기 
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
       

    // 특정 회원의 운동 검색 (운동명 기준)
       public List<WorkDTO> searchWorkoutsByUserAndExercise(Long userId, String exercise) {
           List<WorkEntity> workouts = workRepository.findByUserUserIdAndExerciseContainingIgnoreCase(userId, exercise);
           return workouts.stream().map(WorkDTO::fromEntity).collect(Collectors.toList());
       }

       
       // 특정 workoutId에 영상이 있는지 확인하는 메서드 추가
       public boolean hasVideo(Long workoutId) {
           return workDataRepository.countByWorkoutId(workoutId) > 0;
       }
       
       public List<String> getWorkoutDatesForMonth(Long userId, int year, int month, Long loginUserId, String role) {
    	    if (userId == null) {
    	        userId = loginUserId;
    	    }

    	    UserEntity user = userRepository.findById(userId)
    	            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

    	    if (!"Trainer".equals(role) && !Objects.equals(loginUserId, userId)) {
    	        throw new RuntimeException("본인의 운동 기록만 조회할 수 있습니다.");
    	    }

    	    if ("Trainer".equals(role) && !Objects.equals(loginUserId, userId)) {
    	        List<Long> approvedUserIds = trainerApplicationService.getTrainerMembers(loginUserId)
    	                .stream().map(UserEntity::getUserId).toList();
    	        if (!approvedUserIds.contains(userId)) {
    	            throw new RuntimeException("🚨 이 회원의 운동 기록을 조회할 권한이 없습니다!");
    	        }
    	    }

    	    List<LocalDate> dates = workRepository.findWorkoutDatesByUserAndMonth(user, year, month);

    	    return dates.stream().map(LocalDate::toString).toList();
    	}

      
}