package fitmeup.dto;

import lombok.*;
import fitmeup.entity.UserEntity;
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long userId;  // DB에서 생성된 회원 고유 ID
    private String email;
    private String userPwd;
    private String userName;
    private String userGender;
    private String userBirthdate;
    private String userContact;
    private String roles;

    // Entity -> DTO 변환
    public static UserDTO toDTO(UserEntity entity) {
        return UserDTO.builder()
                .userId(entity.getUserId())
                .email(entity.getEmail())
                .userPwd(entity.getUserPwd()) // 보안상 응답에서 제거할 수도 있음
                .userName(entity.getUserName())
                .userGender(entity.getUserGender())
                .userBirthdate(entity.getUserBirthdate())
                .userContact(entity.getUserContact())
                .roles(entity.getRoles())
                .build();
    }
}