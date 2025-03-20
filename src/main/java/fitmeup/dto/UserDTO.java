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
    private Long userId;
    private String userName;
    private String userGender;
    private LocalDate userBirthdate;
    private String userEmail;
    private String userContact;
    private String role;
    private TrainerDTO trainerInfo;
    private Boolean isOnline;

    // raw 비밀번호를 입력받음; joinProc()에서 암호화하여 Entity로 변환
    private String password;

    /**
     * Entity → DTO 변환
     */
    public static UserDTO fromEntity(UserEntity userEntity, TrainerDTO trainerDTO) {
        return UserDTO.builder()
                .userId(userEntity.getUserId())
                .userName(userEntity.getUserName())
                .userGender(userEntity.getUserGender().name())
                .userBirthdate(userEntity.getUserBirthdate())
                .userEmail(userEntity.getUserEmail())
                .userContact(userEntity.getUserContact())
                .role(userEntity.getRole().name())
                .trainerInfo(trainerDTO)
                .build();
    }

    /**
     * DTO → Entity 변환 (암호화된 비밀번호 전달)
     */
    public UserEntity toEntity(String encryptedPassword) {
        return UserEntity.builder()
                .userId(this.userId)
                .password(encryptedPassword)
                .userName(this.userName)
                .userGender(convertToGenderEnum(this.userGender))  // ✅ 변환 함수 사용
                .userBirthdate(this.userBirthdate)
                .userEmail(this.userEmail)
                .userContact(this.userContact)
                .isOnline(this.isOnline)
                .role(convertToRoleEnum(this.role))  // ✅ 변환 함수 사용
                .build();
    }

    // ✅ 안전한 Gender 변환 함수
    private UserEntity.Gender convertToGenderEnum(String gender) {
        try {
            return UserEntity.Gender.valueOf(gender.substring(0, 1).toUpperCase() + gender.substring(1).toLowerCase());
        } catch (Exception e) {
            return UserEntity.Gender.Other;  // ❗예외 발생 시 기본값 반환 (Optional)
        }
    }

    // ✅ 안전한 Role 변환 함수
    private UserEntity.Role convertToRoleEnum(String role) {
        try {
            return UserEntity.Role.valueOf(role.substring(0, 1).toUpperCase() + role.substring(1).toLowerCase());
        } catch (Exception e) {
            return UserEntity.Role.User;  // ❗예외 발생 시 기본값 반환 (Optional)
        }
    }
}