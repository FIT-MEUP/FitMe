


package fitmeup.entity;

import java.time.LocalDateTime;

import fitmeup.dto.TrainerScheduleDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
    @Column(name = "trainer_schedule_id")
    private Integer trainerScheduleId;

    // TrainerEntity와 다대일 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", nullable = false)
    private TrainerEntity trainer;

    @Column(name = "startTime", nullable = false)
    private LocalDateTime startTime;
    
    
    @Column(name = "endTime", nullable = false)
    private LocalDateTime endTime;

    // DTO -> Entity 변환 메서드
    public static TrainerScheduleEntity toEntity(TrainerScheduleDTO dto) {
        return TrainerScheduleEntity.builder()
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .trainer(TrainerEntity.builder()
                        .trainerId(dto.getTrainerId())
                        .build())
                .build();
    }

}
















