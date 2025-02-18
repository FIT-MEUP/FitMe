package fitmeup.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "foods")
public class FoodEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 음식 ID

    private String foodName; // 음식 이름
    private int calories; // 칼로리
    private int carbs; // 탄수화물
    private int protein; // 단백질
    private int fat; // 지방
}
