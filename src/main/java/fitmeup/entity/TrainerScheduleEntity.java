

package fitmeup.entity;

import fitmeup.dto.TrainerScheduleDTO;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name="trainer_schedule")
@Entity
public class TrainerScheduleEntity {
		
		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		private Integer trainerScheduleId;
	    
		@Builder.Default
		@Column(name="trainer_id")
		private Integer trainerId = 1;
		
		 @Column(name = "start_time", nullable = false)
	    private LocalDateTime startTime;
		 @Column(name = "end_time", nullable = false)
	    private LocalDateTime endTime;

	    // DTO->Entity 변환 메서드
	    public static TrainerScheduleEntity toEntity(TrainerScheduleDTO trainerScheduleDTO) {
	        return TrainerScheduleEntity.builder()
	        	.trainerScheduleId(trainerScheduleDTO.getTrainerScheduleId())
	            .trainerId(trainerScheduleDTO.getTrainerId())
	            .startTime(trainerScheduleDTO.getStartTime())
	            .endTime(trainerScheduleDTO.getEndTime())
	        		.build();
	    }

}

















/*
package fitmeup.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "trainer_schedule")
public class TrainerScheduleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trainer_schedule_id")
    private Integer trainerScheduleId;

    // Trainer 엔티티와 다대일 관계 (이 엔티티가 many 쪽)
    // 변수명이 trainerId 이지만 실제로는 Trainer 객체임에 주의!
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", nullable = false)
    private Trainer trainerId;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    // DTO -> Entity 변환 메서드
    public static TrainerScheduleEntity toEntity(TrainerScheduleDTO dto) {
        return TrainerScheduleEntity.builder()
        		.startTime(dto.getStartTime())
        		.endTime(dto.getEndTime())
            .trainerId(Trainer.builder().trainerId(dto.getTrainerId()).build())
            .build();
    }
    
}
*/