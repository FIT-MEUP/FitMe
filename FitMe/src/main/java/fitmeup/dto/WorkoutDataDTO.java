package fitmeup.dto;

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
public class WorkoutDataDTO {
    private int dataId;
    private int workoutId;
    private String originalFileName;
    private String savedFileName;
    
}
