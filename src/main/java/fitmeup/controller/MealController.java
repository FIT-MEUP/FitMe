package fitmeup.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import fitmeup.dto.FoodDTO;
import fitmeup.dto.MealDTO;
import fitmeup.service.MealService;

@Controller
public class MealController {

	@Autowired
    private MealService mealService;
	
    @Value("${upload.meal.path}") // 수정됨: 음식 게시판 업로드 경로로 변경
    private String uploadDir;

	 // 특정 회원의 특정 날짜 식단 조회 (mealDate를 기준으로 조회)
    @GetMapping("/meals")
    public String getMealsPage(
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "mealDate", required = false) String mealDate,
            Model model) {

        List<MealDTO> meals = Collections.emptyList(); // ✅ 기본값: 빈 리스트

        // mealDate가 없거나 잘못된 형식이면 오늘 날짜로 설정
        LocalDate selectedDate;
        if (mealDate == null || mealDate.isEmpty()) {
            selectedDate = LocalDate.now();
        } else {
            try {
                selectedDate = LocalDate.parse(mealDate);
            } catch (DateTimeParseException e) {
                selectedDate = LocalDate.now(); // ✅ 예외 발생 시 기본값: 오늘 날짜
            }
        }

        // ✅ 특정 userId가 있으면 해당 유저의 식단을 가져오고, 없으면 기본값(userId=1L) 사용
        if (userId == null) {
            userId = 1L; // 🔥 로그인 기능 추가 전까지 임시 userId 사용
        }

        meals = mealService.getMealsByUserAndDate(userId, selectedDate);

        model.addAttribute("meals", meals);
        model.addAttribute("selectedDate", selectedDate.toString()); // ✅ 선택한 날짜 유지
        model.addAttribute("userId", userId);

        return "meals"; // Thymeleaf에서 meals.html을 렌더링
    }

    // 새로운 식단 추가 (FullCalendar 적용)
    @PostMapping("/meals")
    public String saveMeal(
            @RequestParam(name = "userId", required = false) Long userId, // FullCalendar 적용: userId 유지
            @RequestParam(name = "mealDate") String mealDate,
            @RequestParam(name = "totalCalories" , required = false, defaultValue = "0") Double totalCalories,
            @RequestParam(name = "totalCarbs", required = false, defaultValue = "0") Double totalCarbs,
            @RequestParam(name = "totalProtein", required = false, defaultValue = "0") Double totalProtein,
            @RequestParam(name = "totalFat", required = false, defaultValue = "0") Double totalFat,
            @RequestParam(name = "mealFoodName", required = false) String mealFoodName,
            @RequestParam(name = "file", required = false) MultipartFile file) {
    	
        // userId가 null이면 기본값 설정 (로그인 기능이 없는 동안)
        if (userId == null) {
            userId = 1L; // 예제 기본값 (로그인 기능 구현 후 변경 필요)
        }
        
        // mealDate가 null이거나 비어있다면, URL에서 전달된 값을 사용 (자동으로 오늘 날짜로 설정하지 않음)
        if (mealDate == null || mealDate.trim().isEmpty()) {
            System.out.println("🚨 mealDate가 전달되지 않음, 기본값을 오늘로 설정");
            mealDate = LocalDate.now().toString(); 
        }

        MealDTO mealDTO = new MealDTO();
        mealDTO.setUserId(userId);
        mealDTO.setMealDate(LocalDate.parse(mealDate)); // 🔥 FullCalendar에서 선택된 날짜 적용
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

        mealService.saveMeal(mealDTO);
        return "redirect:/meals?mealDate=" + mealDate; // ✅ FullCalendar에서 선택한 날짜로 이동
    }


    // 특정 식단 삭제 (FullCalendar 적용)
    @PostMapping("/meals/delete")
    public String deleteMeal(
            @RequestParam(name = "mealId") Long mealId,
            @RequestParam(name = "mealDate", required = false) String mealDate) { // 🔥 FullCalendar 적용: mealDate 유지

        mealService.deleteMeal(mealId);

        if (mealDate != null) {
            return "redirect:/meals?mealDate=" + mealDate; // ✅ 선택한 날짜 유지
        }
        return "redirect:/meals"; // ✅ 기본 화면으로 이동
    }
    
 // 특정 식단 수정 (수정 페이지 혹은 모달에서 호출)
    @PostMapping("/meals/update")
    public String updateMeal(
            @RequestParam(name = "mealId") Long mealId,
            @RequestParam(name = "totalCalories") double totalCalories,
            @RequestParam(name = "totalCarbs") double totalCarbs,
            @RequestParam(name = "totalProtein") double totalProtein,
            @RequestParam(name = "totalFat") double totalFat,
            @RequestParam(name = "mealDate") String mealDate) {

        mealService.updateMeal(mealId, totalCalories, totalCarbs, totalProtein, totalFat);
        return "redirect:/meals?mealDate=" + mealDate; // ✅ 수정 후 해당 날짜 페이지로 리디렉트
    }
    
    
    // 파일 업로드 
    @PostMapping("/{id}/upload")
    public ResponseEntity<?> uploadMealImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(name = "userId", required = false) Long userId) {

        // 회원 시스템이 없으므로 임시로 userId 설정 (로그인 기능 적용 후 변경)
        if (userId == null) {
            userId = 1L; // 회원 기능 추가 전까지 임시 사용자 ID 사용
        }

        try {
            // 파일 유효성 검사 (확장자 체크)
            String originalFileName = file.getOriginalFilename();
            if (originalFileName == null || (!originalFileName.endsWith(".jpg") && !originalFileName.endsWith(".png"))) {
                return ResponseEntity.badRequest().body("지원하는 파일 형식은 JPG, PNG만 가능합니다.");
            }

            // 고유한 파일명 생성 (UUID 사용)
            String savedFileName = UUID.randomUUID() + "_" + originalFileName;

            // 파일 저장 (c:/uploadPath/ 에 저장)
            File destinationFile = new File(Paths.get(uploadDir, savedFileName).toString());
            file.transferTo(destinationFile);

            // DB에 파일 정보 저장 (MealService 사용)
            mealService.updateMealImage(id, savedFileName, originalFileName);

            return ResponseEntity.ok("파일 업로드 성공: " + savedFileName);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("파일 업로드 실패: " + e.getMessage());
        }
    }

}
