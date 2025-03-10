package fitmeup.dto;

import java.time.LocalDateTime;

import fitmeup.entity.TrainerScheduleEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainerScheduleDTO {

	   private Integer trainerScheduleId;
	    private Long trainerId;
	    private LocalDateTime startTime;
	    private LocalDateTime endTime;

	    @Override
	    public String toString() {
	        return "TrainerScheduleDTO{" +
	                "trainerScheduleId='" + trainerScheduleId + '\'' +
	                ", trainerId='" + trainerId + '\'' +
	                ", startTime='" + startTime + '\'' +
	                ", endTime='" + endTime + '\'' +
	                '}';
	    }

	    
	    // Entity → DTO 변환 메서드
	    public static TrainerScheduleDTO toDTO(TrainerScheduleEntity entity) {
	        return TrainerScheduleDTO.builder()
	                .trainerScheduleId(entity.getTrainerScheduleId())
	                .trainerId(entity.getTrainer() != null ? entity.getTrainer().getTrainerId() : null)
	                .startTime(entity.getStartTime())
	                .endTime(entity.getEndTime())
	                .build();
	    }

}
