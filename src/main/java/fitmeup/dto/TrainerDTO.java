package fitmeup.dto;

import fitmeup.entity.TrainerEntity;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainerDTO {
    private Long trainerId;
    private Long userId;
    private String specialization;
    private int experience;
    private BigDecimal fee;
    private String bio;
    private List<String> photoUrls; // 트레이너 사진 리스트

    // ✅ Entity → DTO 변환
    public static TrainerDTO fromEntity(TrainerEntity trainerEntity, List<String> photos) {
        return TrainerDTO.builder()
                .trainerId(trainerEntity.getTrainerId())
                .userId(trainerEntity.getUser().getUserId())
                .specialization(trainerEntity.getSpecialization())
                .experience(trainerEntity.getExperience())
                .fee(trainerEntity.getFee())
                .bio(trainerEntity.getBio())
                .photoUrls(photos) // 사진 리스트 추가
                .build();
    }
}
