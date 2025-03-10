package fitmeup.service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import fitmeup.dto.FoodDTO;
import fitmeup.entity.FoodEntity;
import fitmeup.repository.FoodRepository;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class FoodService {
	
	private final FoodRepository foodRepository;

    // 음식 검색 기능 : DB에서 검색된 결과를 DTO로 변환해서 반환 
	public List<FoodDTO> searchFood(String query) {
	    List<FoodEntity> foodEntities = foodRepository.findByFoodNameContaining(query);

	    return foodEntities.stream()
	        .map(foodEntity -> {
	            try {
	                return FoodDTO.builder()
	                    .foodId(foodEntity.getFoodId())
	                    .mealId(foodEntity.getMeal() != null ? foodEntity.getMeal().getMealId() : null) // ✅ meal_id가 null이면 null 처리
	                    .foodName(foodEntity.getFoodName())
	                    .calories(foodEntity.getCalories())
	                    .carbs(foodEntity.getCarbs())
	                    .protein(foodEntity.getProtein())
	                    .fat(foodEntity.getFat())
	                    .standardWeight(foodEntity.getStandardWeight())
	                    .build();
	            } catch (Exception e) {
	                System.err.println("❌ 변환 중 오류 발생: " + foodEntity.getFoodName());
	                return null; // 오류 발생 시 해당 데이터 건너뛰기
	            }
	        })
	        .filter(Objects::nonNull) // ✅ 변환 실패한 데이터 제외
	        .collect(Collectors.toList());
	}


}
