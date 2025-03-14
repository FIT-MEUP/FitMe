package fitmeup.dto;

import java.time.LocalDate;

import fitmeup.entity.WorkEntity;
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
public class WorkDTO {

    private Long workoutId; // 운동 기록 ID
    private Long userId; // 사용자 ID
    private String part; // 운동 부위
    private String exercise; // 운동 이름
    private int sets; // 세트 수
    private int reps; // 반복 횟수
    private double weight; // 무게 (KG)
    private LocalDate workoutDate; // 운동 날짜
//    private Long scheduleId; // 스케줄 ID (NULL 가능)

    // ✅ Entity → DTO 변환
    public static WorkDTO fromEntity(WorkEntity entity) {
        return WorkDTO.builder()
                .workoutId(entity.getWorkoutId())
                .userId(entity.getUser().getUserId())
                .part(entity.getPart())
                .exercise(entity.getExercise())
                .sets(entity.getSets())
                .reps(entity.getReps())
                .weight(entity.getWeight())
                .workoutDate(entity.getWorkoutDate())
//                .scheduleId(entity.getSchedule() != null ? entity.getSchedule().getScheduleId() : null)
                .build();
    }
    

}
