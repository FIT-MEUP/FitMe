package fitmeup.dto;

import java.time.LocalDate;

import fitmeup.entity.UserEntity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long userId; // 회원 고유 ID
    private String userName;
    private String userGender; // String 변환
    private LocalDate userBirthdate;
    private String userEmail;
    private String userContact;
    private String role; // 역할 (User, Trainer, Admin)

    // ✅ 트레이너 정보 추가 (TrainerDTO)
    private TrainerDTO trainerInfo; // 트레이너일 경우만 포함

    // ✅ Entity → DTO 변환
    public static UserDTO fromEntity(UserEntity userEntity, TrainerDTO trainerDTO) {
        return UserDTO.builder()
                .userId(userEntity.getUserId())
                .userName(userEntity.getUserName())
                .userGender(userEntity.getUserGender().name()) // Enum → String 변환
                .userBirthdate(userEntity.getUserBirthdate())
                .userEmail(userEntity.getUserEmail())
                .userContact(userEntity.getUserContact())
                .role(userEntity.getRole().name()) // Enum → String 변환
                .trainerInfo(trainerDTO) // 트레이너 정보 연결
                .build();
    }

    // ✅ DTO → Entity 변환
    public UserEntity toEntity(String passwordHash) {
        return UserEntity.builder()
                .userId(this.userId)
                .passwordHash(passwordHash) // 비밀번호 저장
                .userName(this.userName)
                .userGender(UserEntity.Gender.valueOf(this.userGender)) // String → Enum 변환
                .userBirthdate(this.userBirthdate)
                .userEmail(this.userEmail)
                .userContact(this.userContact)
                .role(UserEntity.Role.valueOf(this.role)) // String → Enum 변환
                .build();
    }
}
