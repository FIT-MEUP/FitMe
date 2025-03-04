
package fitmeup.entity;

import java.time.LocalDateTime;

import fitmeup.dto.TrainerScheduleDTO;
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
