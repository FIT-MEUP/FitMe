package fitmeup.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

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
    private Long applicationId; // 신청 ID (PK)

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user; // 상담 신청한 사용자 (FK)

    @ManyToOne
    @JoinColumn(name = "trainer_id", nullable = false)
    private TrainerEntity trainer; // 상담 대상 트레이너 (FK)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING; // 신청 상태 (기본값: Pending)

    @Column(nullable = false, updatable = false)
    private LocalDateTime appliedAt = LocalDateTime.now(); // 신청 시간 (기본값: 현재 시간)

    private LocalDateTime responseAt; // 승인/거절 응답 시간

    public enum Status {
        PENDING, APPROVED, REJECTED
    }
}
