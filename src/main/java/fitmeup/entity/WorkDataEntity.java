package fitmeup.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "workout_data")
public class WorkDataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "data_id")
    private Long dataId; // 운동 데이터 ID (PK)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_id", nullable = false)
    private WorkEntity workout; // 운동 기록과 연결

    @Column(name = "original_file_name", length = 500)
    private String originalFileName; // 업로드된 파일 원본명

    @Column(name = "saved_file_name", length = 500)
    private String savedFileName; // 저장된 파일명
}
