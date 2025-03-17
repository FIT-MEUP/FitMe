package fitmeup.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import fitmeup.dto.LoginUserDetails;
import fitmeup.entity.TrainerApplicationEntity;
import fitmeup.repository.TrainerApplicationRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final TrainerApplicationRepository trainerApplicationRepository;
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        log.info("로그인 성공");
        
        // 세션에 사용자 이메일 저장
        request.getSession().setAttribute("userEmail", authentication.getName());

        // 사용자 권한 목록을 생성 (ROLE_ 접두어가 붙은 경우와 그렇지 않은 경우 모두 체크)
        List<String> roleNames = new ArrayList<>();
        authentication.getAuthorities().forEach(auth -> roleNames.add(auth.getAuthority()));

        // LoginUserDetails를 통해 사용자 ID 추출
        Long userId = ((LoginUserDetails) authentication.getPrincipal()).getUserId();

        // 권한에 따라 리다이렉트 처리
        if (roleNames.contains("ROLE_ADMIN") || roleNames.contains("Admin")) {
            response.sendRedirect("/admin/adminpage");
        } else if (roleNames.contains("ROLE_PENDINGTRAINER") || roleNames.contains("PendingTrainer")) {
            response.sendRedirect("/user/pendingTrainer");
        } else if (roleNames.contains("ROLE_TRAINER") || roleNames.contains("Trainer")) {
            response.sendRedirect("/firstTrainerSchedule");
        } else if (roleNames.contains("ROLE_USER") || roleNames.contains("User")) {
            // TrainerApplicationEntity에서 현재 사용자의 신청 상태 조회
            Optional<TrainerApplicationEntity> applicationOpt = trainerApplicationRepository.findByUserUserId(userId);
            if (applicationOpt.isPresent() && applicationOpt.get().getStatus() == TrainerApplicationEntity.Status.Approved) {
                response.sendRedirect("/firstUserCalendar");
            } else {
                // 신청 엔티티가 없거나, 상태가 Approved가 아니면 홈으로 리다이렉트
                response.sendRedirect("/");
            }
        } else {
            // 기타 다른 권한의 경우 기본 홈으로 리다이렉트
            response.sendRedirect("/");
        }
    }
}
