package fitmeup.dto;

import java.time.LocalDateTime;

import org.apache.catalina.User;

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
	//계약 인덱스넘버
    private Long applicationId;
    //유저아이디
    private Long userId;
    //트레이너 아이디
    private Long trainerId;
    // 승인 상태
    private String status;
    private String name;
    private LocalDateTime appliedAt;
    private LocalDateTime responseAt;

    public static TrainerApplicationDTO toDTO(TrainerApplicationEntity entity){
        return TrainerApplicationDTO.builder()
                .applicationId(entity.getApplicationId())
                .userId(entity.getUser().getUserId())
                .name(entity.getUser().getUserName())
                .trainerId(entity.getTrainer().getTrainerId())
                .status(entity.getStatus().toString())
                .appliedAt(entity.getAppliedAt())
                .responseAt(entity.getResponseAt())
                .build();
    }
}



