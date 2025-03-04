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
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "user_name", nullable = false, length = 100)
    private String userName;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_gender", nullable = false)
    private Gender userGender;

    @Column(name = "user_birthdate", nullable = false)
    private LocalDate userBirthdate;

    @Column(name = "user_email", unique = true, nullable = false, length = 255)
    private String userEmail;

    @Column(name = "user_contact", unique = true, nullable = false, length = 20)
    private String userContact;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;
    
    // ✅ isOnline 필드 추가
    @Builder.Default
    @Column(name = "is_online", nullable = false)
    private Boolean isOnline = false;  // 기본값 false (오프라인)

    public enum Gender {
        Male, Female, Other
    }

    public enum Role {
        User,            // 일반 회원
        PendingTrainer,  // 트레이너 승인 대기
        Trainer,         // 승인된 트레이너
        Admin            // 관리자
    }
}
