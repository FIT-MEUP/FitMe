package fitmeup.dto;

import java.time.LocalDateTime;

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
public class MealDTO {
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
