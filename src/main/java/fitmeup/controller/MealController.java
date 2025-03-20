package fitmeup.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import fitmeup.dto.FoodDTO;
import fitmeup.dto.LoginUserDetails;
import fitmeup.dto.MealDTO;
import fitmeup.entity.UserEntity;
import fitmeup.service.MealService;
import fitmeup.service.TrainerApplicationService;

@Controller
public class MealController {

	@Autowired
    private MealService mealService;
	@Autowired
    private TrainerApplicationService trainerApplicationService;
	@Autowired
    public MealController(MealService mealService, TrainerApplicationService trainerApplicationService) {
        this.mealService = mealService;
        this.trainerApplicationService = trainerApplicationService;
    }
	
    @Value("${upload.meal.path}") // 수정됨: 음식 게시판 업로드 경로로 변경
    private String uploadDir;

	 // 특정 회원의 특정 날짜 식단 조회 (mealDate를 기준으로 조회)
    @GetMapping("/meals")
    public String getMealsPage(
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "mealDate", required = false) String mealDate,
            Model model,
            @AuthenticationPrincipal LoginUserDetails loginUser) {

        List<MealDTO> meals = Collections.emptyList(); // ✅ 기본값: 빈 리스트

        if (mealDate == null) {
            mealDate = LocalDate.now().toString();
        }

        Long loggedInUserId = loginUser.getUserId();
        String role = loginUser.getRoles();

        if (userId == null) {
            userId = loggedInUserId; // 회원은 본인 식단만 조회
        }
        
        // 트레이너인 경우, 승인된 회원 목록 조회
        List<UserEntity> trainerMembers = "Trainer".equals(role)
                ? trainerApplicationService.getTrainerMembers(loggedInUserId)
                : Collections.emptyList();

        meals = mealService.getMealsByUserAndDate(userId, LocalDate.parse(mealDate), loggedInUserId, role);

        model.addAttribute("meals", meals);
        model.addAttribute("selectedDate", mealDate);
        model.addAttribute("role", role);
        model.addAttribute("userId", userId);
        model.addAttribute("trainerMembers", trainerMembers);

        return "meals";
    }

    // 새로운 식단 추가 (FullCalendar 적용)
    @PostMapping("/meals")
    public String saveMeal(
            @RequestParam(name = "userId", required = false) Long userId, // FullCalendar 적용: userId 유지
            @RequestParam(name = "mealDate") String mealDate,
            @RequestParam(name = "mealType", required = false) String mealType,
            @RequestParam(name = "totalCalories" , required = false, defaultValue = "0") Double totalCalories,
            @RequestParam(name = "totalCarbs", required = false, defaultValue = "0") Double totalCarbs,
            @RequestParam(name = "totalProtein", required = false, defaultValue = "0") Double totalProtein,
            @RequestParam(name = "totalFat", required = false, defaultValue = "0") Double totalFat,
            @RequestParam(name = "mealFoodName", required = false) String mealFoodName,
            @RequestParam(name = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal LoginUserDetails loginUser) {
    	
        Long loggedInUserId = loginUser.getUserId();
        String role = loginUser.getRoles();

        // mealDate가 null이거나 비어있다면, URL에서 전달된 값을 사용 (자동으로 오늘 날짜로 설정하지 않음)
        if (mealDate == null || mealDate.trim().isEmpty()) {
            mealDate = LocalDate.now().toString(); 
        }

        MealDTO mealDTO = new MealDTO();
        mealDTO.setUserId(userId);
        mealDTO.setMealDate(LocalDate.parse(mealDate)); // FullCalendar에서 선택된 날짜 적용
        mealDTO.setMealType(mealType); 
        mealDTO.setTotalCalories(totalCalories);
        mealDTO.setTotalCarbs(totalCarbs);
        mealDTO.setTotalProtein(totalProtein);
        mealDTO.setTotalFat(totalFat);
        
        if (mealFoodName != null && !mealFoodName.trim().isEmpty()) {
            FoodDTO foodDTO = new FoodDTO();
            foodDTO.setFoodName(mealFoodName);
            foodDTO.setCalories(totalCalories);
            foodDTO.setCarbs(totalCarbs);
            foodDTO.setProtein(totalProtein);
            foodDTO.setFat(totalFat);

            mealDTO.setFoodList(Collections.singletonList(foodDTO));  // ✅ 음식 추가
        }
        
     //  파일 저장 로직 추가
        if (file != null && !file.isEmpty()) {
            try {
                String originalFileName = file.getOriginalFilename();
                String savedFileName = UUID.randomUUID() + "_" + originalFileName;
                File destinationFile = new File(Paths.get(uploadDir, savedFileName).toString());
                file.transferTo(destinationFile);
                mealDTO.setSavedFileName(savedFileName);
                mealDTO.setOriginalFileName(originalFileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        mealService.saveMeal(mealDTO, loggedInUserId, role);
        return "redirect:/meals?mealDate=" + mealDate; // ✅ FullCalendar에서 선택한 날짜로 이동
    }


    // 특정 식단 삭제 (FullCalendar 적용)
    @PostMapping("/meals/delete")
    public String deleteMeal(
            @RequestParam(name = "mealId") Long mealId,
            @RequestParam(name = "mealDate", required = false) String mealDate,
            @AuthenticationPrincipal LoginUserDetails loginUser) { 

        mealService.deleteMeal(mealId, loginUser.getUserId(), loginUser.getRoles());

        if (mealDate != null) {
            return "redirect:/meals?mealDate=" + mealDate; // 선택한 날짜 유지
        }
        return "redirect:/meals"; // 기본 화면으로 이동
    }
    
 // 특정 식단 수정 (수정 페이지 혹은 모달에서 호출)
    @PostMapping("/meals/update")
    public String updateMeal(
    		@RequestParam(name = "mealId") Long mealId,
            @RequestParam(name = "mealDate") String mealDate,
            @RequestParam(name = "mealType") String mealType,
            @RequestParam(name = "totalCalories") Double totalCalories,
            @RequestParam(name = "totalCarbs") Double totalCarbs,
            @RequestParam(name = "totalProtein") Double totalProtein,
            @RequestParam(name = "totalFat") Double totalFat,
            @RequestParam(name = "foodList", required = false) List<Long> foodIds,
            @RequestParam(name = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal LoginUserDetails loginUser) {

        mealService.updateMeal(mealId, mealType, totalCalories, totalCarbs, totalProtein, totalFat, foodIds, file,
                loginUser.getUserId(), loginUser.getRoles());
        
                return "redirect:/meals?mealDate=" + mealDate;
    }
    
    // 특정 식단 상세 조회 
    @GetMapping("/meals/{mealId}")
    public ResponseEntity<MealDTO> getMealById(@PathVariable("mealId") Long mealId,
    		 @AuthenticationPrincipal LoginUserDetails loginUser) {  

    	MealDTO meal = mealService.getMealById(mealId, loginUser.getUserId(), loginUser.getRoles());

        if (meal == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(meal);
    }

    
    
    // 파일 업로드 
    @PostMapping("/{id}/upload")
    public ResponseEntity<?> uploadMealImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(name = "userId", required = false) Long userId,
            @AuthenticationPrincipal LoginUserDetails loginUser) {

        try {
            mealService.updateMealImage(id, file, loginUser.getUserId(), loginUser.getRoles());
            return ResponseEntity.ok("파일 업로드 성공");
        } catch (RuntimeException e) {  
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        
    }
    
    // 식단 기록 있는 날 점 표시 
    @GetMapping("/meals/highlight-dates")
    @ResponseBody
    public List<String> getMealDatesForCalendar(
            @RequestParam("userId") Long userId,
            @RequestParam("year") int year,
            @RequestParam("month") int month,
            @AuthenticationPrincipal LoginUserDetails loginUser) {
        
        Long loginUserId = loginUser.getUserId();
        String role = loginUser.getRoles();
        
        return mealService.getMealDatesForMonth(userId, year, month, loginUserId, role);
    }



}
