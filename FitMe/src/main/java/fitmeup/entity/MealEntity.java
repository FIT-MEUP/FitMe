package fitmeup.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name="meal")
public class MealEntity {
    private int mealId;
    private int userId;
    private LocalDateTime mealDate;
    private double totalCalories;
    private double totalCarbs;
    private double totalProtein;
    private double totalFat;
    private String originalFileName;
    private String savedFileName;
    
}
