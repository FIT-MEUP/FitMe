package fitmeup.controller;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import fitmeup.dto.MealDTO;
import fitmeup.service.MealService;

@RestController
@RequestMapping("/meals")
public class MealController {

    @Autowired
    private MealService mealService;

    // ✅ 1. 특정 날짜의 식단 조회 (GET) - 예외 처리 추가
    @GetMapping("/{mealDate}")
    public List<MealDTO> getMealsByDate(@PathVariable String mealDate) {
        try {
            LocalDate date = LocalDate.parse(mealDate); // 문자열을 LocalDate로 변환
            return mealService.getMealsByDate(date);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("잘못된 날짜 형식입니다. (예: yyyy-MM-dd)");
        }
    }

    // ✅ 2. 새로운 식단 추가 (POST)
    @PostMapping
    public MealDTO saveMeal(@RequestBody MealDTO mealDTO) {
        return mealService.saveMeal(mealDTO);
    }
}
