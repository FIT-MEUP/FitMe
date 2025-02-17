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
public class HealthDataDTO {
    private int dataId;
    private int userId;
    private double weight;
    private double muscleMass;
    private double height;
    private double bmi;
    private double basalMetabolicRate;
    private LocalDateTime recordDate;
    
}
