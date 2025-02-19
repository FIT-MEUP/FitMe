package fitmeup.entity;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "meal")
@Builder
public class MealEntity {
    
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mealId; // 식단 ID (PK)

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user; // 식단을 등록한 사용자 (FK)

    private LocalDate mealDate; // 식사 날짜

    @OneToMany(mappedBy = "meal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FoodEntity> foodList; // 해당 식단에 포함된 음식들 (FoodEntity 리스트)

    private double totalCalories; // 총 칼로리
    private double totalCarbs; // 총 탄수화물
    private double totalProtein; // 총 단백질
    private double totalFat; // 총 지방

    private String originalFileName; // 원본 파일명 (NULL 허용)
    private String savedFileName; // 서버 저장 파일명 (NULL 허용)
}

