package fitmeup.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import fitmeup.entity.CommentEntity;
import fitmeup.entity.MealEntity;
import fitmeup.entity.WorkEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDTO {

    private Long commentId;  // 댓글 ID
//    private Long userId;  // ✅ [추후 반영] 작성자 (User ID) - 현재는 사용하지 않음.
    private Long workoutId;  // 운동 게시글 ID (NULL 허용)
    private Long mealId;  // 식단 게시글 ID (NULL 허용)
    private String content;  // 댓글 내용
    private LocalDateTime createdAt;  // 작성 시간

    // ✅ Entity → DTO 변환
    public static CommentDTO fromEntity(CommentEntity entity) {
        return CommentDTO.builder()
                .commentId(entity.getCommentId())
//                .userId(entity.getUser().getUserId())  // ✅ [추후 반영] User 정보 추가 필요
                .workoutId(entity.getWorkout() != null ? entity.getWorkout().getWorkoutId() : null)
                .mealId(entity.getMeal() != null ? entity.getMeal().getMealId() : null)
                .content(entity.getContent())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    // ✅ DTO → Entity 변환 (저장 시 사용)
    public CommentEntity toEntity(WorkEntity workout, MealEntity meal, LocalDate requestedDate) {
        return CommentEntity.builder()
                .workout(workout)
                .meal(meal)
                .content(this.content)
                .createdAt(requestedDate.atTime(0, 0, 0)) // 12시 고정
                .build();
    }
    // 추후 userid 반영 필? 




}
