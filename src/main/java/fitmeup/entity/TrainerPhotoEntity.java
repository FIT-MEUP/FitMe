package fitmeup.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="trainer_photo")
public class TrainerPhotoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "photo_id")
    private Long photoId; // 📌 트레이너 사진의 고유 ID (Primary Key)

    @ManyToOne
    @JoinColumn(name = "trainer_id", nullable = false)
    private TrainerEntity trainer; // 📌 해당 사진이 속한 트레이너 (TrainerEntity와 다대일 관계)

    @Column(name = "photo_url", nullable = false, length = 1000)
    private String photoUrl; // 📌 사진 URL (이미지 경로 또는 URL 저장)
}
