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
    private UserEntity user;

    @Column(nullable = false)
    private String specialization;

    @Column(nullable = false)
    private int experience;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal fee;

    @Column(nullable = false)
    private String shortIntro;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String bio;

    @OneToMany(mappedBy = "trainer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TrainerPhotoEntity> photos;

    public String getPhotoUrl() {
        if (photos != null && !photos.isEmpty()) {
            return photos.get(0).getPhotoUrl();
        }
        return "/images/default-trainer.png";
    }
}
