package fitmeup.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import fitmeup.dto.WorkDTO;
import fitmeup.repository.WorkDataRepository;
import fitmeup.service.WorkService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class WorkController {
    
    private final WorkService workService;
    private final WorkDataRepository workDataRepository;

    
    // 화면 요청 (운동 기록 조회 추가)
    @GetMapping({"/work"})
    public String work(@RequestParam(value = "workoutDate", required = false) String workoutDate, Model model) {
        if (workoutDate == null) {
            workoutDate = LocalDate.now().toString(); // 기본값: 오늘 날짜
        }

        // 선택한 날짜의 운동 기록 조회
        List<WorkDTO> workouts = workService.getWorkoutsByDate(LocalDate.parse(workoutDate));

        // 선택한 날짜의 운동 기록에 해당하는 workoutId들만 추출
        List<Long> workoutIds = workouts.stream()
                                        .map(WorkDTO::getWorkoutId)
                                        .collect(Collectors.toList());

        // 선택한 workoutId들에 대한 영상 조회
        Map<Long, String> rawVideoMap = workService.getVideoMapByWorkoutIds(workoutIds);

        // Thymeleaf에서 문제 없이 사용하도록 Key(Long) → String 변환
        Map<String, String> videoMap = new HashMap<>();
        for (Map.Entry<Long, String> entry : rawVideoMap.entrySet()) {
            videoMap.put(String.valueOf(entry.getKey()), entry.getValue()); // Long → String 변환
        }

        System.out.println("🔍 [DEBUG] 컨트롤러에서 videoMap 전달: " + videoMap);

        model.addAttribute("workouts", workouts);
        model.addAttribute("selectedDate", workoutDate);
        model.addAttribute("videoMap", videoMap);

        return "work";
    }
    
 //  [수정됨] 운동 기록을 JSON으로 반환하는 API 추가
    @GetMapping("/workout/data") 
    @ResponseBody
    public List<WorkDTO> getWorkoutData(@RequestParam("workoutDate") String workoutDate) { 
        return workService.getWorkoutsByDate(LocalDate.parse(workoutDate)); 
    }
    
    // 운동 기록 저장 (AJAX 용)
    @PostMapping("/workout")
    public ResponseEntity<WorkDTO> saveWorkout(@RequestBody WorkDTO workDTO) {
        System.out.println("🔥 운동 기록 저장 요청: " + workDTO);

        WorkDTO savedWorkout = workService.saveWorkout(workDTO);
        return ResponseEntity.ok(savedWorkout);
    }
    
    // 운동 기록 수정 
    @PostMapping("/workout/{id}")
    @ResponseBody
    public ResponseEntity<String> updateWorkout(@PathVariable("id") Long id, @RequestBody WorkDTO workDTO) {
        boolean isUpdated = workService.updateWorkout(id, workDTO);
        if (isUpdated) {
            return ResponseEntity.ok("success");
        }
        return ResponseEntity.badRequest().body("failed");
    }
    
	//  특정 운동 기록 조회
	@GetMapping("/workout/{id}")
	@ResponseBody
	public ResponseEntity<WorkDTO> getWorkoutById(@PathVariable("id") Long id) {
	    WorkDTO workout = workService.getWorkoutById(id);
	    if (workout != null) {
	        return ResponseEntity.ok(workout);
	    } else {
	        return ResponseEntity.notFound().build();
	    }
	}
    
 // 삭제 기능 (POST 요청 방식)
    @PostMapping("/workout/delete/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteWorkout(@PathVariable("id") Long id) {
        try {
            workService.deleteWorkout(id);
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("failed");
        }
    }
    
    // 영상 업로드 엔드포인트
    @PostMapping("/workout/videos")
    @ResponseBody
    public ResponseEntity<String> uploadVideo(
            @RequestParam("videoFile") MultipartFile file,
            @RequestParam("workoutId") Long workoutId) {  // 변경된 부분
        try {
            String savedFileName = workService.uploadVideo(file, workoutId);
            return ResponseEntity.ok(savedFileName);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("failed");
        }
    }
    
    //운동 기록에 연결된 동영상 가져오기
    @GetMapping("/workout/video/{workoutId}")
    @ResponseBody
    public ResponseEntity<String> getWorkoutVideo(@PathVariable("workoutId") Long workoutId) {  
        String videoFileName = workService.getWorkoutVideo(workoutId);
        return ResponseEntity.ok(videoFileName);
    }
    
    // 운동 기록에 연결된 동영상 삭제
    @DeleteMapping("/workout/video/{workoutId}")
    @ResponseBody
    public ResponseEntity<String> deleteWorkoutVideo(@PathVariable("workoutId") Long workoutId) {
        boolean isDeleted = workService.deleteWorkoutVideo(workoutId);

        if (isDeleted) {
            return ResponseEntity.ok("✅ 영상이 삭제되었습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("❌ 영상 삭제 실패: 파일이 존재하지 않거나 삭제할 수 없습니다.");
        }
    }

    
    //새로운 동영상 업로드
    @PostMapping("/workout/video")
    @ResponseBody
    public ResponseEntity<String> uploadWorkoutVideo(
            @RequestParam("videoFile") MultipartFile file,
            @RequestParam("workoutId") Long workoutId) {
        try {
            String savedFileName = workService.uploadVideo(file, workoutId);
            return ResponseEntity.ok(savedFileName);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("failed");
        }
    }



    
 //  검색
    @GetMapping("/workout/search")
    @ResponseBody
    public List<WorkDTO> searchWorkouts(@RequestParam("query") String query, 
                                        @RequestParam(value = "hasVideo", required = false, defaultValue = "false") boolean hasVideo) {
        // 1. 검색어(query)가 포함된 운동 기록 가져오기
        List<WorkDTO> workouts = workService.searchWorkoutsByExercise(query);

        // 2. 만약 동영상 체크박스가 활성화됐다면, 해당 운동에 영상이 있는 것만 필터링
        if (hasVideo) {
            workouts = workouts.stream()
                    .filter(workout -> workService.hasVideo(workout.getWorkoutId()))
                    .collect(Collectors.toList());
        }

        return workouts;
    }

 //  GET 요청 추가 (날짜별 조회)
    @GetMapping("/workout/videoMap")
    @ResponseBody
    public Map<Long, String> getVideoMapForWorkouts(@RequestParam("workoutDate") String workoutDate) {
        LocalDate date = LocalDate.parse(workoutDate);
        return workService.getVideoMapByWorkoutDate(date);
    }

    //  POST 요청 (검색된 운동 목록에 대한 영상 조회)
    @PostMapping("/workout/videoMap")
    @ResponseBody
    public Map<Long, String> getVideoMapForWorkouts(@RequestBody List<Long> workoutIds) {
        if (workoutIds == null || workoutIds.isEmpty()) {
            throw new IllegalArgumentException("운동 ID 리스트가 비어 있습니다.");
        }
        return workService.getVideoMapByWorkoutIds(workoutIds);
    }



    
}
