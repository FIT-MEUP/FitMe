package fitmeup.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "workout")
public class WorkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "workout_id")
    private Long workoutId; // 운동 기록 ID (PK)

    @ManyToOne(fetch = FetchType.LAZY) // User 테이블과 연관
    @JoinColumn(name = "user_id", nullable = false) //
    private UserEntity user;

    @Column(name = "part", nullable = false, length = 255)
    private String part; // 운동 부위 (예: 가슴, 어깨 등)

    @Column(name = "exercise", nullable = false, length = 255)
    private String exercise; // 운동 이름

    @Column(name = "sets", nullable = false)
    private int sets; // 세트 수

    @Column(name = "reps", nullable = false)
    private int reps; // 반복 횟수

    @Column(name = "weight", nullable = false)
    private double weight; // 무게 (KG)

    @Column(name = "workout_date", nullable = false)
    private LocalDate workoutDate; // 운동 날짜

//    @ManyToOne(fetch = FetchType.LAZY) // 스케줄과 연결 (NULL 가능)
//    @JoinColumn(name = "schedule_id", nullable = true)
//    private ScheduleEntity schedule;
}