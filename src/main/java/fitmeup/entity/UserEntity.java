package fitmeup.entity;

import jakarta.persistence.*;
import lombok.*;
import fitmeup.dto.UserDTO;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user") // DB 테이블명 지정
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id") // PK 지정
    private Long userId;

    @Column(name = "password_hash", nullable = false) // 패스워드 해시 (Bcrypt 암호화 저장)
    private String userPwd;

    @Column(name = "user_name", nullable = false) // 사용자 이름
    private String userName;

    @Column(name = "user_gender", nullable = false) // 성별
    private String userGender;

    @Column(name = "user_birthdate", nullable = false) // 생년월일
    private String userBirthdate;

    @Column(name = "user_email", nullable = false, unique = true) // 이메일 (유니크)
    private String email;

    @Column(name = "user_contact", nullable = false, unique = true) // 연락처 (유니크)
    private String userContact;

    @Column(name = "role", nullable = false) // 역할 (User, Trainer, Admin)
    private String roles;

    // DTO -> Entity 변환
    public static UserEntity toEntity(UserDTO dto) {
        return UserEntity.builder()
                .userPwd(dto.getUserPwd()) // 회원가입 시 암호화된 비밀번호가 들어옴
                .userName(dto.getUserName())
                .userGender(dto.getUserGender())
                .userBirthdate(dto.getUserBirthdate())
                .email(dto.getEmail())
                .userContact(dto.getUserContact())
                .roles(dto.getRoles())
                .build();
    }
}
