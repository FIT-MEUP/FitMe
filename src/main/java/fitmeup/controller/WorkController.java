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
import fitmeup.entity.UserEntity;
import fitmeup.repository.TrainerApplicationRepository;
import fitmeup.repository.TrainerRepository;
import fitmeup.service.MealService;
import fitmeup.service.TrainerApplicationService;
import fitmeup.service.WorkService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class WorkController {
    
    private final WorkService workService;
    private final TrainerApplicationRepository trainerApplicationRepository;
    private final TrainerRepository trainerRepository;
    private final TrainerApplicationService trainerApplicationService;
        
   // // 화면 요청 (운동 기록 조회 추가)
    @GetMapping({"/work"})
    public String work(@RequestParam(value = "workoutDate", required = false) String workoutDate, 
            @RequestParam(value = "userId", required = false) Long userId,
            Model model, @AuthenticationPrincipal LoginUserDetails loginUser) {

    	if (workoutDate == null) {
            workoutDate = LocalDate.now().toString();
        }

        String role = loginUser.getRoles();
        Long loggedInUserId = loginUser.getUserId();

        // 트레이너가 특정 회원의 운동 게시판을 보려면 userId 포함
        if (userId == null || (!loggedInUserId.equals(userId) && !"Trainer".equals(role))) {
            userId = loggedInUserId; // 회원이라면 본인 userId 사용
        }

        // 트레이너인 경우, 본인이 관리하는 회원 목록 가져오기
        List<UserEntity> trainerMembers = "Trainer".equals(role) 
                ? trainerApplicationService.getTrainerMembers(loggedInUserId) 
                : List.of(); // 일반 사용자는 빈 리스트        
        
        // 운동 기록 조회
        List<WorkDTO> workouts = workService.getUserWorkoutsByDate(userId, LocalDate.parse(workoutDate), loggedInUserId, role);

        // 해당 회원의 운동 기록에 대한 영상 조회
        List<Long> workoutIds = workouts.stream().map(WorkDTO::getWorkoutId).collect(Collectors.toList());
        Map<Long, String> rawVideoMap = workService.getVideoMapByWorkoutIds(workoutIds);

        Map<String, String> videoMap = new HashMap<>();
        for (Map.Entry<Long, String> entry : rawVideoMap.entrySet()) {
            videoMap.put(String.valueOf(entry.getKey()), entry.getValue());
        }

        // Model에 데이터 추가 (Thymeleaf에서 사용 가능하도록)
        model.addAttribute("workouts", workouts);
        model.addAttribute("selectedDate", workoutDate);
        model.addAttribute("videoMap", videoMap);
        model.addAttribute("role", role); // ✅ 사용자 역할 (User or Trainer)
        model.addAttribute("userId", userId); // ✅ 현재 보고 있는 운동 게시판의 회원 ID
        model.addAttribute("trainerMembers", trainerMembers); // ✅ 트레이너 전용: 관리하는 회원 리스트

        return "work";
    }
    
    
 ////  운동 기록 조회 (User & Trainer 구분)
    @GetMapping("/workout/data") 
    @ResponseBody
    public List<WorkDTO> getWorkoutData(@RequestParam("workoutDate") String workoutDate, 
              @RequestParam(value = "userId", required = false) Long userId,
              @AuthenticationPrincipal LoginUserDetails userDetails) { 
    	
        // 로그인한 사용자 정보 확인
        Long loggedInUserId = userDetails.getUserId();
        boolean isTrainer = userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("Trainer"));
        LocalDate date = LocalDate.parse(workoutDate);

        // userId가 없는 경우 본인 ID로 설정
        if (userId == null) {
            userId = loggedInUserId; 
        }

        // Trainer가 아닌 경우 본인의 운동 데이터만 조회
        if (!isTrainer) {
            List<WorkDTO> workouts = workService.getWorkoutsByUserAndDate(loggedInUserId, date);
            return workouts;
        }

        // Trainer인 경우 승인된 회원 목록 가져오기
        List<Long> approvedUserIds = trainerApplicationService.getTrainerMembers(loggedInUserId)
            .stream().map(UserEntity::getUserId).collect(Collectors.toList());

        // Trainer가 관리하는 회원들의 운동 기록 조회
        List<WorkDTO> workouts = workService.getWorkoutsByTrainerAndDate(approvedUserIds, date);

        return workouts;
    }

    
//  특정 운동 기록 조회 (본인 또는 승인된 트레이너만 가능) // 
	@GetMapping("/workout/{id}")
	@ResponseBody
	public ResponseEntity<WorkDTO> getWorkoutById(@PathVariable("id") Long id, 
	        									  @AuthenticationPrincipal LoginUserDetails loginUser) {
		
		 Long loggedInUserId = loginUser.getUserId();
		    String role = loginUser.getRoles();
		    WorkDTO workout = workService.getWorkoutById(id);
		    
		    if (workout == null) {
		        return ResponseEntity.notFound().build();
		    }

		    // 본인 또는 승인된 트레이너만 조회 가능
		    if (!workout.getUserId().equals(loggedInUserId) &&
		        (!"Trainer".equals(role) || !trainerApplicationService.isTrainerOfUser(loggedInUserId, workout.getUserId()))) {
		        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		    }

		    return ResponseEntity.ok(workout);
	}
    
	
   //// 운동 기록 저장 (AJAX 용)  (본인 또는 승인된 트레이너 가능)
    @PostMapping("/workout")
    public ResponseEntity<WorkDTO> saveWorkout(@RequestBody WorkDTO workDTO, @AuthenticationPrincipal LoginUserDetails loginUser) {
    	
        WorkDTO savedWorkout = workService.saveWorkout(workDTO, loginUser.getUserId(), loginUser.getRoles());
        return ResponseEntity.ok(savedWorkout);
    }

    
   //// 운동 기록 수정 (본인 또는 승인된 트레이너만 가능)
    @PostMapping("/workout/{id}")
    @ResponseBody
    public ResponseEntity<String> updateWorkout(@PathVariable("id") Long id, 
                                                @RequestBody WorkDTO workDTO, 
                                                @AuthenticationPrincipal LoginUserDetails loginUser) {
        Long loggedInUserId = loginUser.getUserId();
        String role = loginUser.getRoles();

        boolean isUpdated = workService.updateWorkout(id, workDTO, loggedInUserId, role);
        
        return isUpdated 
            ? ResponseEntity.ok("✅ 운동 기록 수정 완료") 
            : ResponseEntity.status(HttpStatus.FORBIDDEN).body("❌ 운동 기록을 수정할 권한이 없습니다.");
    }

    
 //// 삭제 기능 (POST 요청 방식) 운동 기록 삭제 (본인 또는 승인된 트레이너만 가능)
    @PostMapping("/workout/delete/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteWorkout(@PathVariable("id") Long id, @AuthenticationPrincipal LoginUserDetails loginUser) {

    	boolean isDeleted = workService.deleteWorkout(id, loginUser.getUserId(), loginUser.getRoles());
        return isDeleted ? ResponseEntity.ok("✅ 운동 기록이 삭제되었습니다.") : ResponseEntity.status(HttpStatus.FORBIDDEN).body("❌ 삭제 권한이 없습니다.");
        
    }
    
  //  // 영상 업로드 (본인 또는 승인된 트레이너만 가능)
    @PostMapping("/workout/upload/video")
    @ResponseBody
    public ResponseEntity<String> uploadVideo(
    		@RequestParam("videoFile") MultipartFile file,
            @RequestParam("workoutId") Long workoutId,
            @AuthenticationPrincipal LoginUserDetails loginUser) {  // 변경된 부분
    	
        String savedFileName = workService.uploadVideo(file, workoutId, loginUser.getUserId(), loginUser.getRoles());
        return ResponseEntity.ok(savedFileName);
     
    }
    
    // 운동 영상 조회 (본인 또는 승인된 트레이너만 가능) // 
    @GetMapping("/workout/video/{workoutId}")
    @ResponseBody
    public ResponseEntity<String> getWorkoutVideo(@PathVariable("workoutId") Long workoutId, 
            									  @AuthenticationPrincipal LoginUserDetails loginUser) {     	
    	
    	Long loggedInUserId = loginUser.getUserId();
        String role = loginUser.getRoles();

        String video = workService.getWorkoutVideo(workoutId, loggedInUserId, role);
        
        if (video == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("영상이 존재하지 않습니다.");
        }

        return ResponseEntity.ok(video);
         
    } 

   // // 운동 기록에 연결된 동영상 삭제 (본인만 가능)
    @DeleteMapping("/workout/video/{workoutId}")
    @ResponseBody
    public ResponseEntity<String> deleteWorkoutVideo(@PathVariable("workoutId") Long workoutId, 
            @AuthenticationPrincipal LoginUserDetails loginUser) {
    	
    	  boolean isDeleted = workService.deleteWorkoutVideo(workoutId, loginUser.getUserId(), loginUser.getRoles());
          return isDeleted ? ResponseEntity.ok("✅ 영상이 삭제되었습니다.") : ResponseEntity.status(HttpStatus.NOT_FOUND).body("❌ 영상 삭제 실패");
    
    }
   
 //  검색  // 
    @GetMapping("/workout/search")
    @ResponseBody
    public List<WorkDTO> searchWorkouts(@RequestParam("query") String query, 
                                        @RequestParam(value = "hasVideo", required = false, defaultValue = "false") boolean hasVideo,
                                        @RequestParam(value = "userId", required = false) Long userId,
                                        @AuthenticationPrincipal LoginUserDetails loginUser) {

        Long loggedInUserId = loginUser.getUserId();
        String role = loginUser.getRoles();
        
        if (userId == null) {
            userId = loggedInUserId;
        }


        List<WorkDTO> workouts = workService.searchWorkoutsByUserAndExercise(userId, query);

        // 트레이너의 승인된 회원 목록 가져오기
        List<Long> approvedUserIds = "Trainer".equals(role)
            ? trainerApplicationService.getTrainerMembers(loggedInUserId).stream()
                .map(UserEntity::getUserId)
                .collect(Collectors.toList())
            : List.of();

        // 본인 또는 승인된 회원의 운동 기록만 필터링
        workouts = workouts.stream()
                .filter(workout -> workout.getUserId().equals(loggedInUserId) ||
                                   ("Trainer".equals(role) && approvedUserIds.contains(workout.getUserId())))
                .collect(Collectors.toList());

        // 동영상 체크박스가 활성화됐다면, 해당 운동에 영상이 있는 것만 필터링
        if (hasVideo) {
            workouts = workouts.stream()
                    .filter(workout -> workService.hasVideo(workout.getWorkoutId()))
                    .collect(Collectors.toList());
        }
        return workouts;
    }


 //  GET 요청 추가 (날짜별 운동 조회) //
    @GetMapping("/workout/videoMap")
    @ResponseBody
    public Map<Long, String> getVideoMapForWorkouts(@RequestParam("workoutDate") String workoutDate,
            										@AuthenticationPrincipal LoginUserDetails loginUser) {
    	LocalDate date = LocalDate.parse(workoutDate);
        Long loggedInUserId = loginUser.getUserId();
        String role = loginUser.getRoles();

        // 트레이너의 승인된 회원 목록 가져오기
        List<Long> approvedUserIds = "Trainer".equals(role)
            ? trainerApplicationService.getTrainerMembers(loggedInUserId).stream()
                .map(UserEntity::getUserId)
                .collect(Collectors.toList())
            : List.of();

        List<WorkDTO> workouts = "Trainer".equals(role)
            ? workService.getWorkoutsByTrainerAndDate(approvedUserIds, date)
            : workService.getWorkoutsByUserAndDate(loggedInUserId, date);

        return workService.getVideoMapByWorkoutIds(
                workouts.stream().map(WorkDTO::getWorkoutId).collect(Collectors.toList()));
    }

    //  POST 요청 (검색된 운동 목록에 대한 영상 조회)(트레이너는 승인된 회원의 영상도 조회 가능) // 
    @PostMapping("/workout/videoMap")
    @ResponseBody
    public Map<Long, String> getVideoMapForWorkouts(@RequestBody List<Long> workoutIds,
            										@AuthenticationPrincipal LoginUserDetails loginUser) {

    	if (workoutIds == null || workoutIds.isEmpty()) {
            throw new IllegalArgumentException("운동 ID 리스트가 비어 있습니다.");
        }

        Long loggedInUserId = loginUser.getUserId();
        String role = loginUser.getRoles();

        // 트레이너의 승인된 회원 목록 가져오기
        List<Long> approvedUserIds = "Trainer".equals(role)
            ? trainerApplicationService.getTrainerMembers(loggedInUserId).stream()
                .map(UserEntity::getUserId)
                .collect(Collectors.toList())
            : List.of();

        // 본인 또는 승인된 회원의 운동 기록만 필터링
        List<WorkDTO> workouts = workService.getWorkoutsByIds(workoutIds).stream()
                .filter(workout -> workout.getUserId().equals(loggedInUserId) ||
                                   ("Trainer".equals(role) && approvedUserIds.contains(workout.getUserId())))
                .collect(Collectors.toList());

        return workService.getVideoMapByWorkoutIds(
                workouts.stream().map(WorkDTO::getWorkoutId).collect(Collectors.toList()));
        
    }
    
 // 운동 영상 존재 여부 확인 (본인 또는 승인된 트레이너 가능) //
    @GetMapping("/workout/hasVideo/{workoutId}")
    @ResponseBody
    public ResponseEntity<Boolean> hasWorkoutVideo(@PathVariable("id") Long workoutId, 
            									   @AuthenticationPrincipal LoginUserDetails loginUser) {

    	WorkDTO workout = workService.getWorkoutById(workoutId);

        if (workout == null) {
            return ResponseEntity.notFound().build();
        }

        Long loggedInUserId = loginUser.getUserId();
        String role = loginUser.getRoles();

        // 본인 또는 승인된 트레이너만 영상 존재 여부 확인 가능
        if (!workout.getUserId().equals(loggedInUserId) &&
            (!"Trainer".equals(role) || !trainerApplicationService.isTrainerOfUser(loggedInUserId, workout.getUserId()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(workService.hasVideo(workoutId));
    }
    
    @GetMapping("/workout/highlight-dates")
    @ResponseBody
    public List<String> getWorkoutDatesForCalendar(
            @RequestParam("userId") Long userId,
            @RequestParam("year") int year,
            @RequestParam("month") int month,
            @AuthenticationPrincipal LoginUserDetails loginUser) {

        Long loginUserId = loginUser.getUserId();
        String role = loginUser.getRoles();

        System.out.println("🔥 userId: " + userId + ", year: " + year + ", month: " + month); 
        return workService.getWorkoutDatesForMonth(userId, year, month, loginUserId, role);
    }
    
  
        }

    

