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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import fitmeup.dto.WorkDTO;
import fitmeup.entity.UserEntity;
import fitmeup.entity.WorkDataEntity;
import fitmeup.entity.WorkEntity;
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

    @Value("${upload.video.path}")
    private String uploadDir; // 운동 게시판 영상 업로드 경로
	
	// 운동 기록 저장 (회원 정보 없이 저장)
    public WorkDTO saveWorkout(WorkDTO workDTO) {

        UserEntity dummyUser = userRepository.findById(1L).orElse(null); // 🔥 유저 정보 설정

        WorkEntity workEntity = WorkEntity.builder()
                .part(workDTO.getPart())
                .exercise(workDTO.getExercise())
                .sets(workDTO.getSets())
                .reps(workDTO.getReps())
                .weight(workDTO.getWeight())
                .workoutDate(workDTO.getWorkoutDate())  // ✅ LocalDate 변환 불필요
                .user(dummyUser)
                .build();

        workRepository.save(workEntity);
        return WorkDTO.fromEntity(workEntity);
    }

	
	//  특정 날짜의 운동 기록 조회
	public List<WorkDTO> getWorkoutsByDate(LocalDate workoutDate) {
		
	    List<WorkEntity> workouts = workRepository.findWorkoutsByDate(workoutDate); 
	    
	    return workouts.stream()
	            .map(WorkDTO::fromEntity)
	            .collect(Collectors.toList());
}
	
	// 특정 운동 기록 조회 
	public WorkDTO getWorkoutById(Long id) {
	    Optional<WorkEntity> workout = workRepository.findById(id);
	    return workout.map(WorkDTO::fromEntity).orElse(null);
	}

	
	// 운동 기록 수정 
	@Transactional
	public boolean updateWorkout(Long id, WorkDTO workDTO) {
	    // 1️. DB에서 해당 workoutId를 가진 운동 기록 찾기
	    Optional<WorkEntity> optionalWorkout = workRepository.findById(id);

	    // 2️. 만약 해당 ID의 운동 기록이 없으면 false 반환 (수정 실패)
	    if (optionalWorkout.isEmpty()) {
	        return false;
	    }
	    // 3️. 기존 엔티티 가져오기
	    WorkEntity workout = optionalWorkout.get();

	    // 4️. 새로운 값으로 업데이트 (부분적으로 수정 가능)
	    workout.setPart(workDTO.getPart());
	    workout.setExercise(workDTO.getExercise());
	    workout.setSets(workDTO.getSets());
	    workout.setReps(workDTO.getReps());
	    workout.setWeight(workDTO.getWeight());
	    workout.setWorkoutDate(workDTO.getWorkoutDate());

	    // 5️. 변경 사항 저장
	    workRepository.save(workout);
	    
	    return true; // 수정 성공
	}

	// 삭제 
	@Transactional
	public void deleteWorkout(Long id) {
		workRepository.deleteById(id);		
	}
	
	// 영상 업로드 기능: 파일 저장 후 WorkDataEntity에 기록
	 // 영상 업로드 기능: 파일 저장 후 WorkDataEntity에 기록
    @Transactional
    public String uploadVideo(MultipartFile file, Long workoutId) {
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

        WorkDataEntity videoEntity = WorkDataEntity.builder()
                .originalFileName(originalFileName)
                .savedFileName(savedFileName)
                .workout(workout)  // workout과 연결
                .build();

        workDataRepository.save(videoEntity);
        
        workDataRepository.flush();  //  강제 DB 반영
            
        return savedFileName;
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

    // 특정 workoutId에 영상이 있는지 확인하는 메서드 추가
    public boolean hasVideo(Long workoutId) {
        return workDataRepository.countByWorkoutId(workoutId) > 0;
    }
    
    // 동영상 삭제 
    @Transactional
    public boolean deleteWorkoutVideo(Long workoutId) {
        List<WorkDataEntity> videos = workDataRepository.findByWorkoutId(workoutId);

        if (videos.isEmpty()) {
            System.out.println("❌ [ERROR] 영상 데이터가 존재하지 않음: workoutId=" + workoutId);
            return false;
        }

        boolean isDeleted = false;
        for (WorkDataEntity video : videos) {
            Path filePath = Paths.get(uploadDir, video.getSavedFileName());

            if (!Files.exists(filePath)) {
                System.out.println("⚠ [WARN] 파일이 존재하지 않음: " + filePath);
            } else {
                try {
                    Files.delete(filePath);
                    System.out.println("✅ 파일 삭제 성공: " + filePath);
                    isDeleted = true;
                } catch (IOException e) {
                    System.out.println("❌ [ERROR] 파일 삭제 실패: " + filePath);
                    e.printStackTrace();
                    return false;
                }
            }
            workDataRepository.delete(video);
        }
        return isDeleted;
    }



    //동영상 재업로드 
    @Transactional
    public String getWorkoutVideo(Long workoutId) {
        List<WorkDataEntity> videos = workDataRepository.findByWorkoutId(workoutId);
        return videos.isEmpty() ? null : videos.get(0).getSavedFileName();
    }

   
}