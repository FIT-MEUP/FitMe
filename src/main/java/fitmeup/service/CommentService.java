package fitmeup.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import fitmeup.dto.CommentDTO;
import fitmeup.entity.CommentEntity;
import fitmeup.entity.MealEntity;
import fitmeup.entity.WorkEntity;
import fitmeup.repository.CommentRepository;
import fitmeup.repository.MealRepository;
import fitmeup.repository.WorkRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final WorkRepository workRepository;
    private final MealRepository mealRepository;

    // ✅ 댓글 저장 (운동/식단 게시판 구분)
    @Transactional
    public CommentDTO saveComment(CommentDTO commentDTO, LocalDate requestedDate) {
        WorkEntity workout = null;
        MealEntity meal = null;

        if (commentDTO.getWorkoutId() != null) {
            workout = workRepository.findById(commentDTO.getWorkoutId())
                    .orElseThrow(() -> new IllegalArgumentException("운동 게시글을 찾을 수 없습니다."));
        }
        if (commentDTO.getMealId() != null) {
            meal = mealRepository.findById(commentDTO.getMealId())
                    .orElseThrow(() -> new IllegalArgumentException("식단 게시글을 찾을 수 없습니다."));
        }

        CommentEntity comment = CommentEntity.builder()
                .workout(workout)
                .meal(meal)
                .content(commentDTO.getContent())
                .createdAt(requestedDate.atTime(12, 0, 0)) // 🔥 12시로 고정
                .build();

        commentRepository.save(comment);
        return CommentDTO.fromEntity(comment);
    }

    // 특정 운동 게시글의 댓글 조회
    public List<CommentDTO> getCommentsByWorkout(Long workoutId) {
        WorkEntity workout = workRepository.findById(workoutId)
                .orElseThrow(() -> new IllegalArgumentException("운동 게시글을 찾을 수 없습니다."));
        
        return commentRepository.findByWorkout(workout).stream()
                .map(CommentDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // 특정 식단 게시글의 댓글 조회
    public List<CommentDTO> getCommentsByMeal(Long mealId) {
        MealEntity meal = mealRepository.findById(mealId)
                .orElseThrow(() -> new IllegalArgumentException("식단 게시글을 찾을 수 없습니다."));

        return commentRepository.findByMeal(meal).stream()
                .map(CommentDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    public List<CommentDTO> getMealCommentsByDate(String mealDate) {
        // ✅ 기존 getCommentsByDate() 활용하여 전체 댓글 조회
        List<CommentDTO> allComments = getCommentsByDate(mealDate);

        System.out.println("🔍 전체 조회된 댓글 개수: " + allComments.size());

        // ✅ 식단 댓글만 필터링
        List<CommentDTO> mealComments = allComments.stream()
                .filter(comment -> comment.getMealId() != null) // 운동 댓글 제외
                .collect(Collectors.toList());

        System.out.println("✅ 필터링된 식단 댓글 개수: " + mealComments.size());

        return mealComments;
    }

 // 특정 날짜의 댓글 조회 (운동 + 식단 댓글 포함)
    public List<CommentDTO> getCommentsByDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(date, formatter);

        // 해당 날짜의 00:00:00 ~ 23:59:59 범위 설정
        LocalDateTime startOfDay = localDate.atStartOfDay();
        LocalDateTime endOfDay = localDate.atTime(23, 59, 59);

        System.out.println("🔥 댓글 조회 날짜: " + localDate);
        System.out.println("✅ 조회 범위: " + startOfDay + " ~ " + endOfDay);

        // 정확한 날짜 필터링 추가 (created_at이 요청된 날짜 범위에 속하는지 확인)
        List<CommentEntity> comments = commentRepository.findByCreatedAtDate(startOfDay, endOfDay);

        System.out.println("✅ 조회된 댓글 개수: " + comments.size());

        return comments.stream()
                .filter(comment -> comment.getCreatedAt().toLocalDate().equals(localDate)) // 날짜 정확히 필터링
                .map(CommentDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteComment(Long commentId) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다. ID: " + commentId));

        commentRepository.delete(comment);
    }

}
