package fitmeup.dto;

import fitmeup.entity.WorkDataEntity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkDataDTO {

    private Long dataId; // 운동 데이터 ID
    private Long workoutId; // 운동 기록 ID
    private String originalFileName; // 원본 파일명
    private String savedFileName; // 저장된 파일명

    // ✅ Entity → DTO 변환
    public static WorkDataDTO fromEntity(WorkDataEntity entity) {
        return WorkDataDTO.builder()
                .dataId(entity.getDataId())
                .workoutId(entity.getWorkout().getWorkoutId())
                .originalFileName(entity.getOriginalFileName())
                .savedFileName(entity.getSavedFileName())
                .build();
    }
}
