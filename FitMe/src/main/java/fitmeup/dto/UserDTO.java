package fitmeup.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class UserDTO {
    private int userId;
    private String passwordHash;
    private String userName;
    private String userGender;
    private LocalDate userBirthdate;
    private String userEmail;
    private String userContact;
    private String role;

    
}
