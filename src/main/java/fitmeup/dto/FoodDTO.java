package fitmeup.dto;

import fitmeup.entity.FoodEntity;
import fitmeup.entity.MealEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodDTO {

	private Long foodId;   // 음식 ID (PK)
    private Long mealId;   // 식단 ID (Meal 테이블 참조)
    private String foodName; // 음식 이름
    private double calories;   // 칼로리 (kcal)
    private double carbs;      // 탄수화물 (g)
    private double protein;    // 단백질 (g)
    private double fat;        // 지방 (g)
    
    private String standardWeight; // 표준 중량
    
 // ✅ Entity → DTO 변환
    public static FoodDTO fromEntity(FoodEntity foodEntity) {
        return FoodDTO.builder()
                .foodId(foodEntity.getFoodId())
                .mealId(foodEntity.getMeal().getMealId()) // MealEntity → Meal ID 변환
                .foodName(foodEntity.getFoodName())
                .calories(foodEntity.getCalories())
                .carbs(foodEntity.getCarbs())
                .protein(foodEntity.getProtein())
                .fat(foodEntity.getFat())
                
                .standardWeight(foodEntity.getStandardWeight()) // 표준중량 추가 
                .mealId(foodEntity.getMeal() != null ? foodEntity.getMeal().getMealId() : null) //  Null 체크 추가
                .build();
    }

    // ✅ DTO → Entity 변환
    public FoodEntity toEntity(MealEntity mealEntity) {
        return FoodEntity.builder()
                .meal(mealEntity) // MealEntity를 직접 설정
                .foodName(this.foodName)
                .calories(this.calories)
                .carbs(this.carbs)
                .protein(this.protein)
                .fat(this.fat)
                
                .standardWeight(this.standardWeight) // 표준중량 추가 
               
               
                .build();
    }
}

/*
 - `food_id (PK)`: 음식 ID
- `meal_id (FK)`: 식단 ID (Meal 테이블 참조)
- `food_name`: 음식 이름
- `calories`: 칼로리 (kcal)
- `carbs`: 탄수화물 (g)
- `protein`: 단백질 (g)
- `fat`: 지방 (g)
*/
