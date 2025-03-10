package fitmeup.entity;

import java.time.LocalDateTime;

import fitmeup.dto.ScheduleDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Entity
@Table(name = "schedule")
public class ScheduleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Integer scheduleId;

    // TrainerEntity와 다대일 관계로 연결 (trainer_id 참조)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", nullable = false)
    private TrainerEntity trainer;

    // UserEntity와 다대일 관계로 연결 (user_id 참조)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.Pending;

    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_status", nullable = false)
    private AttendanceStatus attendanceStatus = AttendanceStatus.PT_Session;

    @Column(name = "session_deducted", nullable = false)
    private Boolean sessionDeducted = false;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    public enum Status {
        Pending, Approved, Rejected
    }

    public enum AttendanceStatus {
        Present, Absent, PT_Session
    }

    // DTO -> Entity 변환 메서드
    public static ScheduleEntity toEntity(ScheduleDTO dto) {
        return ScheduleEntity.builder()
                .scheduleId(dto.getScheduleId())
                .trainer(TrainerEntity.builder().trainerId(dto.getTrainerId()).build())
                .user(UserEntity.builder().userId(dto.getUserId()).build())
                .status(dto.getStatus() == null ? ScheduleEntity.Status.Approved
                        : ScheduleEntity.Status.valueOf(dto.getStatus()))
                .attendanceStatus(dto.getAttendanceStatus() == null ? ScheduleEntity.AttendanceStatus.PT_Session
                        : ScheduleEntity.AttendanceStatus.valueOf(dto.getAttendanceStatus()))
                .sessionDeducted(dto.getSessionDeducted() != null ? dto.getSessionDeducted() : false)
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .build();
    }
}
