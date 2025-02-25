package fitmeup.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name="trainer")
public class TrainerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trainer_id")
    private Long trainerId;

    //트레이너 이름 등등 가져오기
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private UserEntity userEntity;

    @Column(nullable = false)
    private String specialization;

    // @Column(nullable = false)
    // private String photo;
    

    @Column(nullable = false)
    private int experience; // 🚨 누락된 필드 추가

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal fee; // 🚨 `DECIMAL(10,2)`에 맞춰 BigDecimal 사용

    @Column(nullable = false, columnDefinition = "TEXT")
    private String bio; // 🚨 긴 텍스트를 저장하기 위해 TEXT 타입 지정

    // 기본 생성자 추가 (JPA 요구 사항)
    public TrainerEntity() {}

    // Getter & Setter 추가
    public Long getTrainerId() { return trainerId; }
    public void setTrainerId(Long trainerId) { this.trainerId = trainerId; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    // public String getPhoto() { return photo; }
    // public void setPhoto(String photo) { this.photo = photo; }

    public int getExperience() { return experience; }
    public void setExperience(int experience) { this.experience = experience; }

    public BigDecimal getFee() { return fee; }
    public void setFee(BigDecimal fee) { this.fee = fee; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
}
