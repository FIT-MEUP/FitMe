package fitmeup.dto;

import java.time.LocalDate;
import java.util.List;

import fitmeup.entity.MealEntity;
import fitmeup.entity.UserEntity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealDTO {

    private Long id;
    private Long userId; // 사용자 ID
    private LocalDate mealDate;
    private String imageUrl;
    private List<String> foodList;
    private int totalCalories;
    private int carbs;
    private int protein;
    private int fat;

    // ✅ DTO → Entity 변환 메서드
    public MealEntity toEntity(UserEntity userEntity) {
        return MealEntity.builder()
                .id(this.id)
                .user(userEntity) // UserEntity 사용
                .mealDate(this.mealDate)
                .imageUrl(this.imageUrl)
                .foodList(this.foodList)
                .totalCalories(this.totalCalories)
                .carbs(this.carbs)
                .protein(this.protein)
                .fat(this.fat)
                .build();
    }

    // ✅ Entity → DTO 변환 메서드
    public static MealDTO fromEntity(MealEntity mealEntity) {
        return MealDTO.builder()
                .id(mealEntity.getId())
                .userId(mealEntity.getUser().getId()) // User ID만 저장
                .mealDate(mealEntity.getMealDate())
                .imageUrl(mealEntity.getImageUrl())
                .foodList(mealEntity.getFoodList())
                .totalCalories(mealEntity.getTotalCalories())
                .carbs(mealEntity.getCarbs())
                .protein(mealEntity.getProtein())
                .fat(mealEntity.getFat())
                .build();
    }
}

