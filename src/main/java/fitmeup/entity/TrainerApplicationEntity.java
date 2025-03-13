package fitmeup.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import fitmeup.dto.TrainerApplicationDTO;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "trainer_application")
public class TrainerApplicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_id")
    private Long applicationId; // 신청 ID (PK)

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user; // 상담 신청한 사용자 (FK)

    @ManyToOne
    @JoinColumn(name = "trainer_id", nullable = false)
    private TrainerEntity trainer; // 상담 대상 트레이너 (FK)

    @Enumerated(EnumType.STRING)
    @Column(name="status", nullable = false)
    private Status status = Status.Pending; // 신청 상태 (기본값: Pending)

    @Column(name = "applied_at", nullable = false, updatable = false)
    private LocalDateTime appliedAt = LocalDateTime.now(); // 신청 시간 (기본값: 현재 시간)

    @Column(name = "response_at")
    private LocalDateTime responseAt; // 승인/거절 응답 시간

    public enum Status {
    	Pending, Approved, Rejected
    }

    public static TrainerApplicationEntity toEntity(TrainerApplicationDTO dto, UserEntity user, TrainerEntity trainer) {
        return TrainerApplicationEntity.builder()
                .applicationId(dto.getApplicationId())
                .user(user)
                .trainer(trainer)
                .status(Status.valueOf(dto.getStatus().toUpperCase())) //  String → ENUM 변환
                .appliedAt(dto.getAppliedAt() != null ? dto.getAppliedAt() : LocalDateTime.now()) // null 방지
                .responseAt(dto.getResponseAt())
                .build();
    }
}
