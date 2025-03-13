package fitmeup.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import fitmeup.dto.LoginUserDetails;
import fitmeup.dto.WorkDTO;
import fitmeup.entity.TrainerApplicationEntity;
import fitmeup.entity.TrainerEntity;
import fitmeup.repository.TrainerApplicationRepository;
import fitmeup.repository.TrainerRepository;
import fitmeup.service.TrainerApplicationService;
import fitmeup.service.WorkService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class WorkController {
    
    private final WorkService workService;
    private final TrainerApplicationRepository trainerApplicationRepository;
    private final TrainerRepository trainerRepository;
    
    //로그인한 사용자의 Trainer ID 가져오기
    private Long getTrainerId(Long userId) {
        return workService.getTrainerId(userId);
    }
    
    //특정 Trainer의 승인된 회원 목록 가져오기
    private List<Long> getApprovedUserIds(Long trainerId) {
        return workService.getApprovedUserIds(trainerId);
    }

    
    // 화면 요청 (운동 기록 조회 추가)
    @GetMapping({"/work"})
    public String work(@RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "workoutDate", required = false) String workoutDate,
            @AuthenticationPrincipal LoginUserDetails userDetails,
            Model model) {

        Long loggedInUserId = userDetails.getUserId();
        boolean isTrainer = userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("Trainer"));
        Long trainerId = getTrainerId(loggedInUserId);

        // 기본적으로 로그인한 User 본인의 운동 기록 조회
        if (userId == null) {
            userId = loggedInUserId;
        }

        // 트레이너가 승인되지 않은 회원의 데이터를 보려는 경우 차단
        if (isTrainer && trainerId != null && !getApprovedUserIds(trainerId).contains(userId)) {
            return "trainers";
        }

        // 일반 회원이 다른 회원의 데이터를 보려는 경우 차단
        if (!isTrainer && !loggedInUserId.equals(userId)) {
            return "trainers";
        }

        // 기본 날짜 설정
        if (workoutDate == null) {
            workoutDate = LocalDate.now().toString();
        }

     // 선택한 userId의 운동 기록 조회
        List<WorkDTO> workouts = workService.getWorkoutsByUserAndDate(userId, LocalDate.parse(workoutDate));
        List<Long> workoutIds = workouts.stream().map(WorkDTO::getWorkoutId).collect(Collectors.toList());


     // 선택한 workoutId들에 대한 영상 조회
        Map<Long, String> rawVideoMap = workService.getVideoMapByWorkoutIds(workoutIds);
        // Thymeleaf에서 문제 없이 사용하도록 Key(Long) → String 변환
        Map<String, String> videoMap = new HashMap<>();
        for (Map.Entry<Long, String> entry : rawVideoMap.entrySet()) {
            videoMap.put(String.valueOf(entry.getKey()), entry.getValue()); // Long → String 변환
        }

        model.addAttribute("workouts", workouts);
        model.addAttribute("selectedDate", workoutDate);
        model.addAttribute("videoMap", videoMap);
        model.addAttribute("userId", userId);
        model.addAttribute("isTrainer", isTrainer);
        model.addAttribute("loggedInUserId", loggedInUserId);

        return "work";
    }
    
 ///  운동 기록 조회 (User & Trainer 구분)
    @GetMapping("/workout/data") 
    @ResponseBody
    public List<WorkDTO> getWorkoutData(@RequestParam("workoutDate") String workoutDate, 
            @AuthenticationPrincipal LoginUserDetails userDetails) { 
    	
    	Long loggedInUserId = userDetails.getUserId();
        boolean isTrainer = userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("Trainer"));
        Long trainerId = getTrainerId(loggedInUserId);
        LocalDate date = LocalDate.parse(workoutDate);

        if (!isTrainer) {
            return workService.getWorkoutsByUserAndDate(loggedInUserId, date);
        }

        List<Long> approvedUserIds = trainerId != null ? getApprovedUserIds(trainerId) : List.of();
        return workService.getWorkoutsByTrainerAndDate(approvedUserIds, date);
    }
    
//  특정 운동 기록 조회 (본인 또는 승인된 트레이너만 가능)
	@GetMapping("/workout/{id}")
	@ResponseBody
	public ResponseEntity<WorkDTO> getWorkoutById(@PathVariable("id") Long id, 
            @AuthenticationPrincipal LoginUserDetails userDetails) {
		
		Long loggedInUserId = userDetails.getUserId();
	    boolean isTrainer = userDetails.getAuthorities().stream()
	                          .anyMatch(auth -> auth.getAuthority().equals("Trainer"));

	    WorkDTO workout = workService.getWorkoutById(id);
	    
	    if (workout == null) {
	        return ResponseEntity.notFound().build();
	    }
	    
        Long trainerId = workService.getTrainerId(loggedInUserId);
        List<Long> approvedUserIds = trainerId != null ? workService.getApprovedUserIds(trainerId) : List.of();

        // 본인 또는 승인된 트레이너만 조회 가능
        if (!workout.getUserId().equals(loggedInUserId) &&
            (!isTrainer || !approvedUserIds.contains(workout.getUserId()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(workout);
	}
    
    // 운동 기록 저장 (AJAX 용)  (본인 또는 승인된 트레이너 가능)
    @PostMapping("/workout")
    public ResponseEntity<WorkDTO> saveWorkout(@RequestBody WorkDTO workDTO, 
            @AuthenticationPrincipal LoginUserDetails userDetails) {
    	
    	 Long loggedInUserId = userDetails.getUserId();
         return ResponseEntity.ok(workService.saveWorkout(workDTO, loggedInUserId));
    }
    
	
    
    // 운동 기록 수정 (본인 또는 승인된 트레이너만 가능)
    @PostMapping("/workout/{id}")
    @ResponseBody
    public ResponseEntity<String> updateWorkout(@PathVariable("id") Long id, 
            @RequestBody WorkDTO workDTO, 
            @AuthenticationPrincipal LoginUserDetails userDetails) {
    	
    	Long loggedInUserId = userDetails.getUserId();
        boolean isUpdated = workService.updateWorkout(id, workDTO, loggedInUserId);
        return isUpdated ? ResponseEntity.ok("success") : ResponseEntity.badRequest().body("failed");
    }
    
 // 삭제 기능 (POST 요청 방식) 운동 기록 삭제 (본인 또는 승인된 트레이너만 가능)
    @PostMapping("/workout/delete/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteWorkout(@PathVariable("id") Long id, 
            @AuthenticationPrincipal LoginUserDetails userDetails) {

    	Long loggedInUserId = userDetails.getUserId();
        boolean isDeleted = workService.deleteWorkout(id, loggedInUserId);
        return isDeleted ? ResponseEntity.ok("success") : ResponseEntity.badRequest().body("failed");
    }
    
    // 영상 업로드 (본인 또는 승인된 트레이너만 가능)
    @PostMapping("/workout/upload/video")
    @ResponseBody
    public ResponseEntity<String> uploadVideo(
    		@RequestParam("videoFile") MultipartFile file,
            @RequestParam("workoutId") Long workoutId,
            @AuthenticationPrincipal LoginUserDetails userDetails) {  // 변경된 부분
    	

        Long loggedInUserId = userDetails.getUserId();
        return ResponseEntity.ok(workService.uploadVideo(file, workoutId, loggedInUserId));
     
    }
    
    // 운동 영상 조회 (본인 또는 승인된 트레이너만 가능)
    @GetMapping("/workout/video/{workoutId}")
    @ResponseBody
    public ResponseEntity<String> getWorkoutVideo(@PathVariable("workoutId") Long workoutId,
            									@AuthenticationPrincipal LoginUserDetails userDetails) {     	
 
    	Long loggedInUserId = userDetails.getUserId();
        return ResponseEntity.ok(workService.getWorkoutVideo(workoutId, loggedInUserId));
         
    } 

    // 운동 기록에 연결된 동영상 삭제 (본인만 가능)
    @DeleteMapping("/workout/video/{workoutId}")
    @ResponseBody
    public ResponseEntity<String> deleteWorkoutVideo(@PathVariable("workoutId") Long workoutId,
            										 @AuthenticationPrincipal LoginUserDetails userDetails) {
    	
    	Long loggedInUserId = userDetails.getUserId();
        boolean isDeleted = workService.deleteWorkoutVideo(workoutId, loggedInUserId);
        return isDeleted ? ResponseEntity.ok("영상이 삭제되었습니다.") :
                ResponseEntity.status(HttpStatus.NOT_FOUND).body("영상 삭제 실패: 파일이 존재하지 않거나 삭제할 수 없습니다.");
    
    }
   
 //  검색
    @GetMapping("/workout/search")
    @ResponseBody
    public List<WorkDTO> searchWorkouts(@RequestParam("query") String query, 
                                        @RequestParam(value = "hasVideo", required = false, defaultValue = "false") boolean hasVideo,
                                        @AuthenticationPrincipal LoginUserDetails userDetails) {
        Long loggedInUserId = userDetails.getUserId();
        boolean isTrainer = userDetails.getAuthorities().stream()
                              .anyMatch(auth -> auth.getAuthority().equals("Trainer"));

        List<WorkDTO> workouts = workService.searchWorkoutsByExercise(query);
        Long trainerId = workService.getTrainerId(loggedInUserId);
        List<Long> approvedUserIds = trainerId != null ? workService.getApprovedUserIds(trainerId) : List.of();

        // 본인 또는 승인된 회원의 운동 기록만 필터링
        if (isTrainer) {
            workouts = workouts.stream()
                    .filter(workout -> workout.getUserId().equals(loggedInUserId) || approvedUserIds.contains(workout.getUserId()))
                    .collect(Collectors.toList());
        } else {
            workouts = workouts.stream()
                    .filter(workout -> workout.getUserId().equals(loggedInUserId))
                    .collect(Collectors.toList());
        }

        // 동영상 체크박스가 활성화됐다면, 해당 운동에 영상이 있는 것만 필터링
        if (hasVideo) {
            workouts = workouts.stream()
                    .filter(workout -> workService.hasVideo(workout.getWorkoutId()))
                    .collect(Collectors.toList());
        }

        return workouts;
    }


 //  GET 요청 추가 (날짜별 운동 조회)
    @GetMapping("/workout/videoMap")
    @ResponseBody
    public Map<Long, String> getVideoMapForWorkouts(@RequestParam("workoutDate") String workoutDate,
            										@AuthenticationPrincipal LoginUserDetails userDetails) {
    	   LocalDate date = LocalDate.parse(workoutDate);
    	    Long loggedInUserId = userDetails.getUserId();
    	    boolean isTrainer = userDetails.getAuthorities().stream()
    	                          .anyMatch(auth -> auth.getAuthority().equals("Trainer"));

    	    Long trainerId = workService.getTrainerId(loggedInUserId);
            List<Long> approvedUserIds = trainerId != null ? workService.getApprovedUserIds(trainerId) : List.of();

            if (isTrainer) {
                return workService.getVideoMapByWorkoutIds(workService.getWorkoutsByTrainerAndDate(approvedUserIds, date)
                        .stream().map(WorkDTO::getWorkoutId).collect(Collectors.toList()));
            }

            return workService.getVideoMapByWorkoutIds(workService.getWorkoutsByUserAndDate(loggedInUserId, date)
                    .stream().map(WorkDTO::getWorkoutId).collect(Collectors.toList()));
    }

    //  POST 요청 (검색된 운동 목록에 대한 영상 조회)(트레이너는 승인된 회원의 영상도 조회 가능)
    @PostMapping("/workout/videoMap")
    @ResponseBody
    public Map<Long, String> getVideoMapForWorkouts(@RequestBody List<Long> workoutIds
    											, @AuthenticationPrincipal LoginUserDetails userDetails) {

        if (workoutIds == null || workoutIds.isEmpty()) {
            throw new IllegalArgumentException("운동 ID 리스트가 비어 있습니다.");
        }

        Long loggedInUserId = userDetails.getUserId();
        boolean isTrainer = userDetails.getAuthorities().stream()
                              .anyMatch(auth -> auth.getAuthority().equals("Trainer"));

        Long trainerId = workService.getTrainerId(loggedInUserId);
        List<Long> approvedUserIds = trainerId != null ? workService.getApprovedUserIds(trainerId) : List.of();

        List<WorkDTO> workouts = workService.getWorkoutsByIds(workoutIds);
        if (isTrainer) {
            workouts = workouts.stream()
                    .filter(workout -> workout.getUserId().equals(loggedInUserId) ||
                                       approvedUserIds.contains(workout.getUserId()))
                    .collect(Collectors.toList());
        } else {
            workouts = workouts.stream()
                    .filter(workout -> workout.getUserId().equals(loggedInUserId))
                    .collect(Collectors.toList());
        }

        return workService.getVideoMapByWorkoutIds(
                workouts.stream().map(WorkDTO::getWorkoutId).collect(Collectors.toList()));
        
    }
    
 // 운동 영상 존재 여부 확인 (본인 또는 승인된 트레이너 가능)
    @GetMapping("/workout/hasVideo/{workoutId}")
    @ResponseBody
    public ResponseEntity<Boolean> hasWorkoutVideo(@PathVariable("workoutId") Long workoutId,
                                                   @AuthenticationPrincipal LoginUserDetails userDetails) {
        WorkDTO workout = workService.getWorkoutById(workoutId);
        if (workout == null) {
            return ResponseEntity.notFound().build();
        }

        Long loggedInUserId = userDetails.getUserId();
        boolean isTrainer = userDetails.getAuthorities().stream()
                              .anyMatch(auth -> auth.getAuthority().equals("Trainer"));

        Long trainerId = workService.getTrainerId(loggedInUserId);
        List<Long> approvedUserIds = trainerId != null ? workService.getApprovedUserIds(trainerId) : List.of();

        if (!workout.getUserId().equals(loggedInUserId) &&
            (!isTrainer || !approvedUserIds.contains(workout.getUserId()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(workService.hasVideo(workoutId));
        }

    
}
