package fitmeup.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user")  // ✅ 테이블명 설정
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;  // 회원 고유 ID (PK)

    @Column(name = "password_hash", length = 255)
    private String passwordHash;  // 비밀번호 해시 (NULL 허용)

    @Column(name = "user_name", nullable = false, length = 100)
    private String userName;  // 이름

    @Enumerated(EnumType.STRING)
    @Column(name = "user_gender", nullable = false)
    private Gender userGender;  // 성별 (Male, Female, Other)

    @Column(name = "user_birthdate", nullable = false)
    private LocalDate userBirthdate;  // 생년월일

    @Column(name = "user_email", unique = true, nullable = false, length = 255)
    private String userEmail;  // 이메일 (유니크)

    @Column(name = "user_contact", unique = true, nullable = false, length = 20)
    private String userContact;  // 연락처 (유니크)

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;  // 역할 (User, Trainer, Admin)

    // ✅ Enum 정의
    public enum Gender {
        Male, Female, Other
    }

    public enum Role {
        User, Trainer, Admin
    }
}
