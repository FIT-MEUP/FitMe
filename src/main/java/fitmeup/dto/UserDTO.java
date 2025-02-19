package fitmeup.dto;

import java.time.LocalDate;

import fitmeup.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Builder
public class UserDTO {
    private int userId;
    private String passwordHash;
    private String userName;
    private String userGender;
    private LocalDate userBirthdate;
    private String userEmail;
    private String userContact;
    private String role;

    public UserDTO toEntity(UserEntity userEntity){
        return UserDTO.builder()
            .userId(userEntity.getUserId())
            .passwordHash(userEntity.getPasswordHash())
            .userName(userEntity.getUserName())
            .userGender(userEntity.getUserGender())
            .userBirthdate(userEntity.getUserBirthdate())
            .userEmail(userEntity.getUserEmail())
            .userContact(userEntity.getUserContact())
            .role(userEntity.getRole())
            .build();
    }
    
}
