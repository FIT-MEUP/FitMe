package fitmeup.dto;

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
public class TrainerDTO {
    private int trainerId;
    private int userId;
    private String specialization;
    private int experience;
    private double fee;
    private String bio;
    
}
