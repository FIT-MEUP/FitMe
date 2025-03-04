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
            .filter(Objects::nonNull)
            .map((FoodEntity foodEntity) -> {  // FoodEntity를 FoodDTO로 변환
                try {
                    return FoodDTO.builder()
                        .foodId(foodEntity.getFoodId())
                        .mealId(foodEntity.getMeal() != null ? foodEntity.getMeal().getMealId() : null)
                        .foodName(foodEntity.getFoodName())
                        .calories(foodEntity.getCalories())
                        .carbs(foodEntity.getCarbs())
                        .protein(foodEntity.getProtein())
                        .fat(foodEntity.getFat())
                        .standardWeight(foodEntity.getStandardWeight())
                        .build();
                } catch (Exception e) {
                    System.err.println("❌ 변환 중 오류 발생: " + foodEntity.getFoodName());
                    return new FoodDTO();  // 예외 발생 시 빈 FoodDTO 반환
                }
            })
            .collect(Collectors.toList());
    }
}
