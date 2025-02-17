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
@Table(name="trainer_application")
public class TrainerApplicationEntity {
    private int applicationId;
    private int userId;
    private int trainerId;
    private String status;
    private LocalDateTime appliedAt;
    private LocalDateTime responseAt;
    
}
