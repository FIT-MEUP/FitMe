package fitmeup.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "food")
public class FoodEntity {
    
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long foodId;  // 음식 ID (PK)

    @ManyToOne
    @JoinColumn(name = "meal_id", nullable = false)
    private MealEntity meal; // 해당 음식이 속한 식단 (Meal 테이블 참조)

    @Column(nullable = false)
    private String foodName; // 음식 이름

    @Column(nullable = false)
    private double calories;  // 칼로리 (kcal)

    @Column(nullable = false)
    private double carbs;  // 탄수화물 (g)

    @Column(nullable = false)
    private double protein;  // 단백질 (g)

    @Column(nullable = false)
    private double fat;  // 지방 (g)
}
