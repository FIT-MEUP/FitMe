package fitmeup.dto;

import java.time.LocalDate;

import fitmeup.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long userId; // 회원 고유 ID
    private String userName;
    private String userGender; // String으로 변환
    private LocalDate userBirthdate;
    private String userEmail;
    private String userContact;
    private String role; // String으로 변환

    // ✅ Entity → DTO 변환
    public static UserDTO fromEntity(UserEntity userEntity) {
        return UserDTO.builder()
                .userId(userEntity.getUserId())
                .userName(userEntity.getUserName())
                .userGender(userEntity.getUserGender().name()) // Enum → String 변환
                .userBirthdate(userEntity.getUserBirthdate())
                .userEmail(userEntity.getUserEmail())
                .userContact(userEntity.getUserContact())
                .role(userEntity.getRole().name()) // Enum → String 변환
                .build();
    }

    // ✅ DTO → Entity 변환
    public UserEntity toEntity(String passwordHash) {
        return UserEntity.builder()
                .userId(this.userId)
                .passwordHash(passwordHash) // 비밀번호는 DTO에서 받지 않지만, 생성 시 필요
                .userName(this.userName)
                .userGender(UserEntity.Gender.valueOf(this.userGender)) // String → Enum 변환
                .userBirthdate(this.userBirthdate)
                .userEmail(this.userEmail)
                .userContact(this.userContact)
                .role(UserEntity.Role.valueOf(this.role)) // String → Enum 변환
                .build();
    }
}