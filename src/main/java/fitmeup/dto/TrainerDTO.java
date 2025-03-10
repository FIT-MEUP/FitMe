package fitmeup.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainerDTO {
    private Long trainerId;
    private Long userId;
    
    // User 관련 필드 추가
    private String userName;
    private String userEmail;
    private String password; // Raw 비밀번호
    private String userGender; // 예: "Male", "Female", "Other"
    private LocalDate userBirthdate;
    private String userContact;
    
    // Trainer 관련 필드
    private String specialization;
    private int experience;
    private BigDecimal fee;
    private String bio;
    
    private List<String> photoUrls; // 트레이너 사진 리스트

    // 필요시, Entity → DTO 변환 메서드도 업데이트
    public static TrainerDTO fromEntity(fitmeup.entity.TrainerEntity trainerEntity, List<String> photos) {
        return TrainerDTO.builder()
                .trainerId(trainerEntity.getTrainerId())
                .userId(trainerEntity.getUser().getUserId())
                .userName(trainerEntity.getUser().getUserName())
                .userEmail(trainerEntity.getUser().getUserEmail())
                // password는 보통 노출하지 않으므로 변환 시 제외
                .userGender(trainerEntity.getUser().getUserGender().name())
                .userBirthdate(trainerEntity.getUser().getUserBirthdate())
                .userContact(trainerEntity.getUser().getUserContact())
                .specialization(trainerEntity.getSpecialization())
                .experience(trainerEntity.getExperience())
                .fee(trainerEntity.getFee())
                .bio(trainerEntity.getBio())
                .photoUrls(photos)
                .build();
    }
}
