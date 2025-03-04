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
                .userGender(UserEntity.Gender.valueOf(this.userGender))
                .userBirthdate(this.userBirthdate)
                .userEmail(this.userEmail)
                .userContact(this.userContact)
                .role(UserEntity.Role.valueOf(this.role))
                .build();
    }
}
