package fitmeup.entity;

import java.time.LocalDate;

import fitmeup.dto.UserDTO;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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
@Entity
@Builder
@Table(name="user")
public class UserEntity {
    private int userId;
    private String passwordHash;
    private String userName;
    private String userGender;
    private LocalDate userBirthdate;
    private String userEmail;
    private String userContact;
    private String role;

    
    public UserEntity toEntity(UserDTO userDTO){
        return UserEntity.builder()
            .userId(userDTO.getUserId())
            .passwordHash(userDTO.getPasswordHash())
            .userName(userDTO.getUserName())
            .userGender(userDTO.getUserGender())
            .userBirthdate(userDTO.getUserBirthdate())
            .userEmail(userDTO.getUserEmail())
            .userContact(userDTO.getUserContact())
            .role(userDTO.getRole())
            .build();
    }
    
}
