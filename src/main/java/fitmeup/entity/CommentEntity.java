package fitmeup.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
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
@Table(name = "comment")
public class CommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId; // 댓글 ID (PK)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = true) // 추후에 user 반영해 false로 변경해야된다. 
    private UserEntity user; // 작성자 (FK)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_id", nullable = true)
    private WorkEntity workout; // 운동 게시판 (FK, NULL 허용)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_id", nullable = true)
    private MealEntity meal; // 식단 게시판 (FK, NULL 허용)

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content; // 댓글 내용

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 댓글 작성 시간

}
