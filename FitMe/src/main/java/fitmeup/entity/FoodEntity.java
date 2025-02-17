package fitmeup.entity;

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
@Table(name="food")
public class FoodEntity {
    private int foodId;
    private int mealId;
    private String foodName;
    private double calories;
    private double carbs;
    private double protein;
    private double fat;
    
}
