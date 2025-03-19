package fitmeup.dto;

import java.time.LocalDateTime;

import fitmeup.entity.ScheduleEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ScheduleDTO {
    private Integer scheduleId;
    private Long trainerId;
    private Long userId;
    private String userName;         // 추가: 사용자 이름
    private String status;          // Enum → String 변환
    private String attendanceStatus; // Enum → String 변환
    private Boolean sessionDeducted;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // Entity → DTO 변환 메서드 (Trainer와 User 엔티티에서 각각 ID 추출)
    public static ScheduleDTO toDTO(ScheduleEntity entity) {
        return ScheduleDTO.builder()
            .scheduleId(entity.getScheduleId())
            .trainerId(entity.getTrainer() != null ? entity.getTrainer().getTrainerId() : null)
            .userId(entity.getUser() != null ? entity.getUser().getUserId() : null)
            .userName(entity.getUser() != null ? entity.getUser().getUserName() : null) // userName 설정
            .status(entity.getStatus().name())
            .attendanceStatus(entity.getAttendanceStatus().name())
            .sessionDeducted(entity.getSessionDeducted())
            .startTime(entity.getStartTime())
            .endTime(entity.getEndTime())
            .build();
    }
}
