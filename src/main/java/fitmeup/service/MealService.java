package fitmeup.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fitmeup.dto.FoodDTO;
import fitmeup.dto.MealDTO;
import fitmeup.entity.FoodEntity;
import fitmeup.entity.MealEntity;
import fitmeup.entity.UserEntity;
import fitmeup.repository.FoodRepository;
import fitmeup.repository.MealRepository;
import fitmeup.repository.UserRepository;
import jakarta.transaction.Transactional;

@Service
public class MealService {

    @Autowired
    private MealRepository mealRepository;

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private UserRepository userRepository;

 // ✅ 1. 특정 회원의 특정 날짜 식단 조회
    public List<MealDTO> getMealsByUserAndDate(Long userId, LocalDate mealDate) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        return mealRepository.findByUserAndMealDate(user, mealDate)
                .stream()
                .map(meal -> {
                    List<FoodDTO> foodList = foodRepository.findByMeal(meal)
                            .stream()
                            .map(FoodDTO::fromEntity)
                            .collect(Collectors.toList());

                    return MealDTO.fromEntity(meal, foodList);
                })
                .collect(Collectors.toList());
    }

    // ✅ 2. 특정 회원의 전체 식단 조회 (날짜 순 정렬)
    public List<MealDTO> getMealsByUser(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        return mealRepository.findByUserOrderByMealDateDesc(user)
                .stream()
                .map(meal -> {
                    List<FoodDTO> foodList = foodRepository.findByMeal(meal)
                            .stream()
                            .map(FoodDTO::fromEntity)
                            .collect(Collectors.toList());

                    return MealDTO.fromEntity(meal, foodList);
                })
                .collect(Collectors.toList());
    }

    // ✅ 새로운 식단 저장 (음식도 함께 저장)
    @Transactional
    public MealDTO saveMeal(MealDTO mealDTO) {
        UserEntity userEntity = userRepository.findById(mealDTO.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없음"));

        // 1️⃣ MealEntity 저장
        MealEntity meal = mealDTO.toEntity(userEntity);
        MealEntity savedMeal = mealRepository.save(meal);

        // 2️⃣ FoodEntity 저장 (음식 리스트가 있으면 추가)
        if (mealDTO.getFoodList() != null && !mealDTO.getFoodList().isEmpty()) {
            List<FoodEntity> foodEntities = mealDTO.getFoodList().stream()
                    .map(foodDTO -> foodDTO.toEntity(savedMeal)) // MealEntity와 연결
                    .collect(Collectors.toList());

            foodRepository.saveAll(foodEntities);
        }

        // 3️⃣ 저장된 데이터를 다시 조회하여 반환 (음식 정보 포함)
        List<FoodDTO> savedFoodList = foodRepository.findByMeal(savedMeal)
                .stream()
                .map(FoodDTO::fromEntity)
                .collect(Collectors.toList());

        return MealDTO.fromEntity(savedMeal, savedFoodList);
    }
    
 // ✅ 4. 특정 식단 삭제 (음식도 함께 삭제)
    @Transactional
    public void deleteMeal(Long mealId) {
        MealEntity meal = mealRepository.findById(mealId)
                .orElseThrow(() -> new IllegalArgumentException("해당 식단을 찾을 수 없습니다."));

        // ✅ 먼저 연결된 음식 데이터 삭제
        foodRepository.deleteByMeal(meal);

        // ✅ 식단 삭제
        mealRepository.delete(meal);
    }

    
    // 식단 수정 
    @Transactional
	public void updateMeal(Long mealId, double totalCalories, double totalCarbs, double totalProtein, double totalFat) {
		 MealEntity meal = mealRepository.findById(mealId)
		            .orElseThrow(() -> new IllegalArgumentException("해당 식단을 찾을 수 없습니다."));

		    meal.setTotalCalories(totalCalories);
		    meal.setTotalCarbs(totalCarbs);
		    meal.setTotalProtein(totalProtein);
		    meal.setTotalFat(totalFat);

		    mealRepository.save(meal); // ✅ 변경된 내용 저장
		
	}
}
