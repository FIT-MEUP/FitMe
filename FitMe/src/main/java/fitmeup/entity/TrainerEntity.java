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
@Setter
@Getter
@ToString
@Entity
@Table(name="trainer")
public class TrainerEntity {
    private int trainerId;
    private int userId;
    private String specialization;
    private int experience;
    private double fee;
    private String bio;
    
}
