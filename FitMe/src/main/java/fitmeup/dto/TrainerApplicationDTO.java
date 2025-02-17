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
public class TrainerApplicationDTO {
    private int applicationId;
    private int userId;
    private int trainerId;
    private String status;
    private LocalDateTime appliedAt;
    private LocalDateTime responseAt;
    
}
