package fitmeup.entity;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.ElementCollection;
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
@Table(name = "meals")
@Builder
public class MealEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 식단 ID

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user; // 누가 등록했는지 (회원 정보)

    private LocalDate mealDate; // 식단 날짜
    private String imageUrl; // 업로드한 사진 경로

    @ElementCollection
    private List<String> foodList; // 음식 목록

    private int totalCalories; // 총 칼로리
    private int carbs; // 탄수화물
    private int protein; // 단백질
    private int fat; // 지방
}

