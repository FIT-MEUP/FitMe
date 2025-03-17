package fitmeup.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fitmeup.dto.CommentDTO;
import fitmeup.dto.LoginUserDetails;
import fitmeup.entity.CommentEntity;
import fitmeup.entity.UserEntity;
import fitmeup.repository.MealRepository;
import fitmeup.repository.UserRepository;
import fitmeup.repository.WorkRepository;
import fitmeup.service.CommentService;
import fitmeup.service.TrainerApplicationService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentController {

	@Autowired
    private final CommentService commentService;
	@Autowired
    private final WorkRepository workRepository;
	@Autowired
    private final MealRepository mealRepository;
	@Autowired
	private final UserRepository userRepository;
	@Autowired 
	private final TrainerApplicationService trainerApplicationService; 
	
	  //// 특정 운동 게시글의 댓글 조회
	@GetMapping("/workout/{workoutId}")
	public ResponseEntity<List<Map<String, Object>>> getWorkoutComments(
	        @PathVariable("workoutId") Long workoutId,
	        @AuthenticationPrincipal LoginUserDetails loginUser) {

	    System.out.println("🔥 특정 운동 기록의 댓글 조회 요청: " + workoutId);

	    if (loginUser == null) {
	        System.out.println("❌ 로그인 정보 없음");
	        return ResponseEntity.status(401).build();
	    }

	    Long loggedInUserId = loginUser.getUserId();
	    boolean isTrainerUser = "Trainer".equals(loginUser.getRoles());

	    List<CommentDTO> comments = commentService.getCommentsByWorkout(workoutId, loginUser);
	    System.out.println("✅ 반환된 운동 댓글 개수: " + comments.size());

	    List<Map<String, Object>> commentDataList = comments.stream().map(comment -> {
	        Map<String, Object> commentData = new HashMap<>();
	        commentData.put("commentId", comment.getCommentId());
	        commentData.put("content", comment.getContent());
	        commentData.put("workoutId", comment.getWorkoutId());
	        commentData.put("createdAt", comment.getCreatedAt());

	        // ✅ 댓글 작성자 정보 가져오기
	        UserEntity user = userRepository.findById(comment.getUserId()).orElse(null);
	        String userName = (user != null) ? user.getUserName() : "알 수 없음";
	        boolean isTrainer = user != null && user.getRole().equals(UserEntity.Role.Trainer);

	        commentData.put("userName", isTrainer ? "트레이너" : userName);
	        commentData.put("isTrainer", isTrainer);

	        // ✅ 삭제 버튼 노출 여부 (본인 댓글이거나 트레이너일 경우만 삭제 가능)
	        boolean isOwner = comment.getUserId().equals(loggedInUserId);
	        commentData.put("isOwnerOrTrainer", isOwner);

	        return commentData;
	    }).collect(Collectors.toList());

	    return ResponseEntity.ok(commentDataList);
	}
	
	 // 특정 날짜의 식단 댓글 가져오기 (AJAX 요청)
	 // 식단 댓글 조회
	    @GetMapping("/meal/{mealId}")
	    public ResponseEntity<List<Map<String, Object>>> getMealComments(
	    		@PathVariable("mealId") Long mealId, @AuthenticationPrincipal LoginUserDetails loginUser) {

	        if (loginUser == null) return ResponseEntity.status(401).build();

	        Long loggedInUserId = loginUser.getUserId();

	        List<CommentDTO> comments = commentService.getCommentsByMeal(mealId, loginUser);

	        List<Map<String, Object>> commentDataList = buildCommentResponse(comments, loggedInUserId);

	        return ResponseEntity.ok(commentDataList);
	    }
	    
	    // 공통 메서드로 분리 (운동/식단 댓글 둘 다 사용)
	    private List<Map<String, Object>> buildCommentResponse(List<CommentDTO> comments, Long loggedInUserId) {
	        return comments.stream().map(comment -> {
	            Map<String, Object> commentData = new HashMap<>();
	            commentData.put("commentId", comment.getCommentId());
	            commentData.put("content", comment.getContent());
	            commentData.put("workoutId", comment.getWorkoutId());
	            commentData.put("mealId", comment.getMealId());
	            commentData.put("createdAt", comment.getCreatedAt());

	            UserEntity user = userRepository.findById(comment.getUserId()).orElse(null);
	            String userName = (user != null) ? user.getUserName() : "알 수 없음";
	            boolean isTrainer = user != null && user.getRole().equals(UserEntity.Role.Trainer);

	            commentData.put("userName", isTrainer ? "트레이너" : userName);
	            commentData.put("isTrainer", isTrainer);
	            boolean isOwner = comment.getUserId().equals(loggedInUserId);
	            commentData.put("isOwnerOrTrainer", isOwner);

	            return commentData;
	        }).collect(Collectors.toList());
	    }



 ////  댓글 추가 (운동 or 식단)
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addComment(@RequestBody CommentDTO commentDTO,
                                                          @AuthenticationPrincipal LoginUserDetails loginUser) {
        if (loginUser == null) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }

        LocalDate currentDate = LocalDate.now();
        CommentDTO savedComment = commentService.saveComment(commentDTO, currentDate, loginUser);

        // 🔥 댓글 데이터를 JSON으로 반환 (프론트엔드에서 즉시 추가할 수 있도록)
        Map<String, Object> response = new HashMap<>();
        response.put("commentId", savedComment.getCommentId());
        response.put("content", savedComment.getContent());
        response.put("workoutId", savedComment.getWorkoutId());
        response.put("createdAt", savedComment.getCreatedAt());

        // ✅ 댓글 작성자의 정보 가져오기
        UserEntity user = userRepository.findById(savedComment.getUserId()).orElse(null);
        String userName = (user != null) ? user.getUserName() : "알 수 없음";
        response.put("userName", userName);

        // ✅ 트레이너 여부 정확한 판별 (문자열 비교 X, Enum 사용)
        boolean isTrainer = user != null && user.getRole() == UserEntity.Role.Trainer;
        response.put("isTrainer", isTrainer);

        // ✅ 삭제 권한 확인 (본인 댓글만 삭제 가능)
        boolean isOwner = savedComment.getUserId().equals(loginUser.getUserId());
        response.put("isOwnerOrTrainer", isOwner);

        return ResponseEntity.ok(response);
    }

     
 // ✅ 댓글 삭제 (트레이너는 본인 댓글만 삭제 가능)
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable("commentId") Long commentId,
                                              @AuthenticationPrincipal LoginUserDetails loginUser) {
        System.out.println("🔥 댓글 삭제 요청 - commentId: " + commentId);
        
        if (loginUser == null) {
            System.out.println("❌ 로그인 정보 없음");
            return ResponseEntity.status(401).build(); // 인증 실패
        }

        // ✅ 댓글 가져오기 (findCommentById 활용)
        CommentEntity comment = commentService.findCommentById(commentId);
        
        // ✅ 트레이너는 본인 댓글만 삭제 가능, 회원은 본인 댓글만 삭제 가능
        boolean isOwner = comment.getUser().getUserId().equals(loginUser.getUserId());
        boolean isTrainer = "Trainer".equals(loginUser.getRoles());

        if (isTrainer && !isOwner) {
            System.out.println("❌ 트레이너는 본인 댓글만 삭제 가능");
            return ResponseEntity.status(403).build(); // 삭제 권한 없음
        }

        commentService.deleteComment(commentId, loginUser);
        System.out.println("✅ 댓글 삭제 완료 - commentId: " + commentId);
        
        return ResponseEntity.ok().build();
    }

}
