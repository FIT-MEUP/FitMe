package fitmeup.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fitmeup.dto.CommentDTO;
import fitmeup.entity.MealEntity;
import fitmeup.entity.WorkEntity;
import fitmeup.repository.MealRepository;
import fitmeup.repository.WorkRepository;
import fitmeup.service.CommentService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;
    private final WorkRepository workRepository;
    private final MealRepository mealRepository;

 //  댓글 추가 (운동 or 식단)
    @PostMapping("/add")
    public ResponseEntity<?> addComment(@RequestBody CommentDTO commentDTO) {
        if (commentDTO.getWorkoutId() == null && commentDTO.getMealId() == null && commentDTO.getCreatedAt() == null) {
            return ResponseEntity.badRequest().body("운동 게시글 ID, 식단 게시글 ID 또는 날짜 정보 중 하나는 필요합니다.");
        }

        try {
            LocalDate requestedDate;

            if (commentDTO.getCreatedAt() != null) {
                // 클라이언트에서 전송된 `createdAt` 값을 `LocalDate`로 변환
                requestedDate = commentDTO.getCreatedAt().toLocalDate();
            } else if (commentDTO.getWorkoutId() != null) {
                requestedDate = workRepository.findById(commentDTO.getWorkoutId())
                        .map(WorkEntity::getWorkoutDate)
                        .orElseThrow(() -> new IllegalArgumentException("운동 게시글을 찾을 수 없습니다."));
            } else {
                requestedDate = mealRepository.findById(commentDTO.getMealId())
                        .map(MealEntity::getMealDate)
                        .orElseThrow(() -> new IllegalArgumentException("식단 게시글을 찾을 수 없습니다."));
            }

            CommentDTO savedComment = commentService.saveComment(commentDTO, requestedDate);
            return ResponseEntity.ok(savedComment);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("댓글 저장 중 오류 발생: " + e.getMessage());
        }
    }

    // 특정 운동 게시글의 댓글 가져오기
    @GetMapping("/workout/{workoutId}")
    public ResponseEntity<List<CommentDTO>> getWorkoutComments(@PathVariable Long workoutId) {
        return ResponseEntity.ok(commentService.getCommentsByWorkout(workoutId));
    }

    // 특정 식단 게시글의 댓글 가져오기
    @GetMapping("/meal/{mealId}")
    public ResponseEntity<List<CommentDTO>> getMealComments(@PathVariable Long mealId) {
        return ResponseEntity.ok(commentService.getCommentsByMeal(mealId));
    }

    // 특정 날짜의 모든 식단 댓글 가져오기 (mealDate 기준)
    @GetMapping("/meal/date/{mealDate}")
    public ResponseEntity<List<CommentDTO>> getMealCommentsByDate(@PathVariable("mealDate") String mealDate) {
    	   System.out.println("🔥 특정 날짜의 식단 댓글 조회 요청: " + mealDate);

    	    List<CommentDTO> comments = commentService.getMealCommentsByDate(mealDate);
    	    System.out.println("✅ 반환된 식단 댓글 개수: " + comments.size());

    	    // ✅ 실제 반환되는 댓글 목록 출력
    	    comments.forEach(comment -> System.out.println("📝 댓글 내용: " + comment.getContent() + ", ID: " + comment.getCommentId()));

    	    return ResponseEntity.ok(comments);
    }
    
    // 날짜별 댓글 불러오기 
    @GetMapping("/date/{date}")
    public ResponseEntity<List<CommentDTO>> getCommentsByDate(@PathVariable("date") String date) {
        System.out.println("🔥 날짜별 댓글 조회 요청: " + date);
        
        List<CommentDTO> comments = commentService.getCommentsByDate(date); // userId 나중에 처리 필요 
        System.out.println("✅ 반환된 댓글 목록: " + comments);

        return ResponseEntity.ok(comments);
    }

    
    // 댓글 삭제 
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable("commentId") Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok().build();
    }

}
