package fitmeup.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class WorkoutDTO {
    private int workoutId;
    private int userId;
    private String part;
    private String exercise;
    private int sets;
    private int reps;
    private double weight;
    private Integer scheduleId;
    private LocalDateTime workoutDate;
    
}
