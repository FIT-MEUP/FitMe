package fitmeup.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="trainer")
public class TrainerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trainer_id")
    private Long trainerId;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user; // 트레이너는 User와 연결됨

    @Column(nullable = false)
    private String specialization;

    @Column(nullable = false)
    private int experience;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal fee;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String bio;

    // 🔹 트레이너는 여러 개의 사진을 가질 수 있음
    @OneToMany(mappedBy = "trainer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TrainerPhotoEntity> photos;

    // ✅ 대표 사진을 가져오는 메서드 추가
    public String getPhotoUrl() {
        if (photos != null && !photos.isEmpty()) {
            return photos.get(0).getPhotoUrl(); // 첫 번째 사진을 대표 사진으로 사용
        }
        return "/images/default-trainer.png"; // 기본 이미지
    }
}
