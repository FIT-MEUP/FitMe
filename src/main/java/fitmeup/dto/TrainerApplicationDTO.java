package fitmeup.dto;

import java.time.LocalDateTime;

import fitmeup.entity.TrainerApplicationEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainerApplicationDTO {
    private Long applicationId;
    private Long userId;
    private Long trainerId;
    private String status;
    private LocalDateTime appliedAt;
    private LocalDateTime responseAt;

    public static TrainerApplicationDTO toDTO(TrainerApplicationEntity entity){
        return TrainerApplicationDTO.builder()
                .applicationId(entity.getApplicationId())
                .userId(entity.getUser().getUserId())
                .trainerId(entity.getTrainer().getTrainerId())
                .status(entity.getStatus().toString())
                .appliedAt(entity.getAppliedAt())
                .responseAt(entity.getResponseAt())
                .build();
    }
}


/**
 * CREATE TABLE trainer_application (
    application_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    trainer_id INT NOT NULL,
    status ENUM('Pending', 'Approved', 'Rejected') NOT NULL,
    applied_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    response_at DATETIME NULL,
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (trainer_id) REFERENCES trainer(trainer_id) ON DELETE CASCADE
);
 */
