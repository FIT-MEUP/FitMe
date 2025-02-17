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
@Setter
@Getter
@ToString
@Entity
@Table(name="workout_data")
public class WorkoutEntity {
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
