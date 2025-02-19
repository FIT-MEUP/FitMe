package fitmeup.dto;

import java.time.LocalDate;
import java.util.List;

import fitmeup.entity.MealEntity;
import fitmeup.entity.UserEntity;
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
public class MealDTO {
	
	private Long mealId; // 식단 ID (PK)
    private Long userId; // 사용자 ID (FK)
    private LocalDate mealDate; // 식사 날짜
    private List<FoodDTO> foodList; // 음식 목록 (FoodDTO 리스트)

    private double totalCalories; // 총 칼로리
    private double totalCarbs; // 총 탄수화물
    private double totalProtein; // 총 단백질
    private double totalFat; // 총 지방

    private String originalFileName; // 원본 파일명 (NULL 허용)
    private String savedFileName; // 서버 저장 파일명 (NULL 허용)

    // ✅ DTO → Entity 변환 메서드
    public MealEntity toEntity(UserEntity userEntity) {
        return MealEntity.builder()
                .mealId(this.mealId)
                .user(userEntity)
                .mealDate(this.mealDate)
                .totalCalories(this.totalCalories)
                .totalCarbs(this.totalCarbs)
                .totalProtein(this.totalProtein)
                .totalFat(this.totalFat)
                .originalFileName(this.originalFileName)
                .savedFileName(this.savedFileName)
                .build();
    }

    // ✅ Entity → DTO 변환 메서드
    public static MealDTO fromEntity(MealEntity mealEntity, List<FoodDTO> foodDTOList) {
        return MealDTO.builder()
                .mealId(mealEntity.getMealId())
                .userId(mealEntity.getUser().getUserId())
                .mealDate(mealEntity.getMealDate())
                .foodList(foodDTOList) // ✅ 음식 정보 포함
                .totalCalories(mealEntity.getTotalCalories())
                .totalCarbs(mealEntity.getTotalCarbs())
                .totalProtein(mealEntity.getTotalProtein())
                .totalFat(mealEntity.getTotalFat())
                .originalFileName(mealEntity.getOriginalFileName())
                .savedFileName(mealEntity.getSavedFileName())
                .build();
    }
}

/*
 * ### **식단 (Meal)**

- `meal_id (PK)`: 식단 ID
- `user_id (FK)`: 사용자 ID (User 테이블 참조)
- `meal_date`: 식사 날짜 및 시간
- `total_calories`: 총 칼로리 (kcal)
- `total_carbs`: 총 탄수화물 (g)
- `total_protein`: 총 단백질 (g)
- `total_fat`: 총 지방 (g)
- `original_file_name`: 원본 파일명 (NULL 허용)
- `saved_file_name`: 서버 저장 파일명 (NULL 허용)
*/
