package fitmeup.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FoodDTO {

    private Long id;
    private String foodName;
    private int calories;
    private int carbs;
    private int protein;
    private int fat;
}
