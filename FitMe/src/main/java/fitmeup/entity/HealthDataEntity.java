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
@Table(name="health_data")
public class HealthDataEntity {
    private int dataId;
    private int userId;
    private double weight;
    private double muscleMass;
    private double height;
    private double bmi;
    private double basalMetabolicRate;
    private LocalDateTime recordDate;
    
}
