package fitmeup.controller;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import fitmeup.dto.MealDTO;
import fitmeup.service.MealService;

@Controller
public class MealController {

	@Autowired
    private MealService mealService;

	 // ✅ 특정 회원의 특정 날짜 식단 조회 (mealDate를 기준으로 조회)
    @GetMapping({ "", "/", "/meals" })
    public String getMealsPage(
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "mealDate", required = false) String mealDate,
            Model model) {

        List<MealDTO> meals = Collections.emptyList(); // ✅ 기본값: 빈 리스트

        // ✅ mealDate가 없거나 잘못된 형식이면 오늘 날짜로 설정
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

        return "meals"; // ✅ Thymeleaf에서 meals.html을 렌더링
    }

    // ✅ 새로운 식단 추가 (FullCalendar 적용)
    @PostMapping("/meals")
    public String saveMeal(
            @RequestParam(name = "userId", required = false) Long userId, // 🔥 FullCalendar 적용: userId 유지
            @RequestParam(name = "mealDate") String mealDate,
            @RequestParam(name = "totalCalories") double totalCalories,
            @RequestParam(name = "totalCarbs") double totalCarbs,
            @RequestParam(name = "totalProtein") double totalProtein,
            @RequestParam(name = "totalFat") double totalFat) {
    	
        // ✅ userId가 null이면 기본값 설정 (로그인 기능이 없는 동안)
        if (userId == null) {
            userId = 1L; // 예제 기본값 (로그인 기능 구현 후 변경 필요)
        }

        // ✅ mealDate가 비어 있을 경우 현재 날짜 사용
        if (mealDate == null || mealDate.isEmpty()) {
            mealDate = LocalDate.now().toString();
        }

        MealDTO mealDTO = new MealDTO();
        mealDTO.setUserId(userId);
        mealDTO.setMealDate(LocalDate.parse(mealDate)); // 🔥 FullCalendar에서 선택된 날짜 적용
        mealDTO.setTotalCalories(totalCalories);
        mealDTO.setTotalCarbs(totalCarbs);
        mealDTO.setTotalProtein(totalProtein);
        mealDTO.setTotalFat(totalFat);

        mealService.saveMeal(mealDTO);
        return "redirect:/meals?mealDate=" + mealDate; // ✅ FullCalendar에서 선택한 날짜로 이동
    }
 /*   
	@PostMapping("/meals")
	public String saveMeal(
	        @RequestParam(name = "userId", required = false) Long userId,
	        @RequestParam(name = "mealDate", required = false) String mealDate,
	        @RequestParam(name = "totalCalories") int totalCalories,
	        @RequestParam(name = "totalCarbs") int totalCarbs,
	        @RequestParam(name = "totalProtein") int totalProtein,
	        @RequestParam(name = "totalFat") int totalFat) {

	    MealDTO mealDTO = new MealDTO();
	    mealDTO.setUserId(userId);

	    // ✅ mealDate가 비어있다면 오늘 날짜로 설정
	    LocalDate parsedDate;
	    try {
	        parsedDate = (mealDate == null || mealDate.isEmpty()) ? LocalDate.now() : LocalDate.parse(mealDate);
	    } catch (DateTimeParseException e) {
	        // ✅ 날짜 형식 오류 발생 시 예외 처리 (기본값: 오늘 날짜)
	        parsedDate = LocalDate.now();
	    }
	    mealDTO.setMealDate(parsedDate);

	    mealDTO.setTotalCalories(totalCalories);
	    mealDTO.setTotalCarbs(totalCarbs);
	    mealDTO.setTotalProtein(totalProtein);
	    mealDTO.setTotalFat(totalFat);

	    mealService.saveMeal(mealDTO);
	    return "redirect:/meals?mealDate=" + parsedDate.toString(); // ✅ 날짜가 정상적으로 적용되도록 수정
	}
  */ 
	



    // ✅ 특정 식단 삭제 (FullCalendar 적용)
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
    
 // ✅ 특정 식단 수정 (수정 페이지 혹은 모달에서 호출)
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
}
