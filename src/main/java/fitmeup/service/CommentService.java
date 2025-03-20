package fitmeup.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import fitmeup.dto.CommentDTO;
import fitmeup.dto.LoginUserDetails;
import fitmeup.entity.CommentEntity;
import fitmeup.entity.MealEntity;
import fitmeup.entity.UserEntity;
import fitmeup.entity.WorkEntity;
import fitmeup.repository.CommentRepository;
import fitmeup.repository.MealRepository;
import fitmeup.repository.UserRepository;
import fitmeup.repository.WorkRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final WorkRepository workRepository;
    private final MealRepository mealRepository;
    private final TrainerApplicationService trainerApplicationService;
    private final UserRepository userRepository;

 // 특정 운동 게시글의 댓글 조회 //
    public List<CommentDTO> getCommentsByWorkout(Long workoutId, LoginUserDetails loginUser) {
        validateWorkoutAccess(workoutId, loginUser); // 트레이너 or 회원 권한 체크

        WorkEntity workout = workRepository.findById(workoutId)
                .orElseThrow(() -> new IllegalArgumentException("해당 운동 기록을 찾을 수 없습니다."));

        // workout_id 로 연결된 댓글 모두 가져옴
        return commentRepository.findByWorkout(workout)
                .stream()
                .map(CommentDTO::fromEntity)
                .collect(Collectors.toList());
    }


    // 특정 식단 게시글의 댓글 조회
    public List<CommentDTO> getCommentsByMeal(Long mealId, LoginUserDetails loginUser) {
        validateMealAccess(mealId, loginUser);
        return commentRepository.findByMeal(
            mealRepository.findById(mealId)
                .orElseThrow(() -> new IllegalArgumentException("해당 식단 기록을 찾을 수 없습니다."))
        ).stream().map(CommentDTO::fromEntity).collect(Collectors.toList());
    }
    

 // ✅ 댓글 저장 (운동/식단 게시판 구분)
    @Transactional
    public CommentDTO saveComment(CommentDTO commentDTO, LocalDate requestedDate, LoginUserDetails loginUser) {
        WorkEntity workout = null;
        MealEntity meal = null;

        // ✅ 로그인한 사용자 조회
        UserEntity user = userRepository.findById(loginUser.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // ✅ 운동 게시글에 대한 댓글인 경우
        if (commentDTO.getWorkoutId() != null) {
            workout = workRepository.findById(commentDTO.getWorkoutId())
                    .orElseThrow(() -> new IllegalArgumentException("운동 게시글을 찾을 수 없습니다."));
            
            // ✅ 운동 게시판 접근 권한 확인
            validateWorkoutAccess(commentDTO.getWorkoutId(), loginUser);
        }

        // ✅ 식단 게시글에 대한 댓글인 경우
        if (commentDTO.getMealId() != null) {
            meal = mealRepository.findById(commentDTO.getMealId())
                    .orElseThrow(() -> new IllegalArgumentException("식단 게시글을 찾을 수 없습니다."));
            
            // ✅ 식단 게시판 접근 권한 확인
            validateMealAccess(commentDTO.getMealId(), loginUser);
        }

        // ✅ 댓글 엔티티 생성 및 저장
        CommentEntity comment = CommentEntity.builder()
                .workout(workout)
                .meal(meal)
                .user(user) // ✅ 로그인한 사용자 정보 추가
                .content(commentDTO.getContent())
                .createdAt(requestedDate.atTime(12, 0, 0)) // ✅ 기존 시간 처리 방식 유지 (12시 고정)
                .build();

        commentRepository.save(comment);
        return CommentDTO.fromEntity(comment);
    }


 // 댓글 삭제 (권한 확인 후 삭제)
    @Transactional
    public void deleteComment(Long commentId, LoginUserDetails loginUser) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("❌ 존재하지 않는 댓글입니다."));

        if (!isOwnerOrTrainer(comment, loginUser)) {
            throw new SecurityException("❌ 삭제 권한이 없습니다.");
        }

        commentRepository.delete(comment);
        System.out.println("✅ 댓글 삭제 완료 - commentId: " + commentId);
    }
    
    //  특정 댓글의 소유자 또는 트레이너인지 확인
    private boolean isOwnerOrTrainer(CommentEntity comment, LoginUserDetails loginUser) {
        Long commentOwnerId = comment.getUser().getUserId();
        Long loginUserId = loginUser.getUserId();
        boolean isTrainer = "Trainer".equals(loginUser.getRoles());

        // ✅ 트레이너는 자신의 댓글만 삭제 가능, 회원도 자신의 댓글만 삭제 가능
        return commentOwnerId.equals(loginUserId);
    }
    
    public CommentEntity findCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("❌ 존재하지 않는 댓글입니다."));
    }

    //  식단 댓글 접근 권한 검증 (회원 & 트레이너)
    private void validateMealAccess(Long mealId, LoginUserDetails loginUser) {
        MealEntity meal = mealRepository.findById(mealId)
                .orElseThrow(() -> new IllegalArgumentException("해당 식단 기록을 찾을 수 없습니다."));

        Long ownerId = meal.getUser().getUserId();
        Long loginUserId = loginUser.getUserId();

        if (!ownerId.equals(loginUserId) && 
            (!"Trainer".equals(loginUser.getRoles()) || !trainerApplicationService.isTrainerOfUser(loginUserId, ownerId))) {
            throw new SecurityException("이 회원의 식단 댓글을 관리할 권한이 없습니다.");
        }
    }

    //  운동 댓글 접근 권한 검증 (회원 & 트레이너)
    private void validateWorkoutAccess(Long workoutId, LoginUserDetails loginUser) {
        WorkEntity workout = workRepository.findById(workoutId)
                .orElseThrow(() -> new IllegalArgumentException("해당 운동 기록을 찾을 수 없습니다."));

        Long ownerId = workout.getUser().getUserId();
        Long loginUserId = loginUser.getUserId();

        if (!ownerId.equals(loginUserId) && 
            (!"Trainer".equals(loginUser.getRoles()) || !trainerApplicationService.isTrainerOfUser(loginUserId, ownerId))) {
            throw new SecurityException("이 회원의 운동 댓글을 관리할 권한이 없습니다.");
        }
    }



}
