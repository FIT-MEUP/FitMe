package fitmeup.handler;

import fitmeup.service.UserService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.messaging.simp.SimpMessagingTemplate;
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


    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        log.info("로그인 성공");


        Long userId = ((LoginUserDetails) authentication.getPrincipal()).getUserId();
        userService.setOnline(userId, true);

        // STOMP 브로드캐스트: "/topic/onlineStatus"에 userId 전달
        // 다른 화면에서 이 userId가 온라인임을 실시간으로 반영 가능
        messagingTemplate.convertAndSend("/topic/onlineStatus",
            "LOGIN:" + userId);

        // 세션에 사용자 이메일 저장
        request.getSession().setAttribute("userEmail", authentication.getName());

        // 사용자 권한 목록 생성 (ROLE_ 접두어가 붙은 경우와 그렇지 않은 경우 모두 체크)
        List<String> roleNames = new ArrayList<>();
        authentication.getAuthorities().forEach(auth -> roleNames.add(auth.getAuthority()));


        // 권한에 따라 리다이렉트 처리
        if (roleNames.contains("ROLE_ADMIN") || roleNames.contains("Admin")) {
            response.sendRedirect("/admin/adminpage");
        } else if (roleNames.contains("ROLE_PENDINGTRAINER") || roleNames.contains("PendingTrainer")) {
            response.sendRedirect("/user/pendingTrainer");
        } else if (roleNames.contains("ROLE_TRAINER") || roleNames.contains("Trainer")) {
            response.sendRedirect("/firstTrainerSchedule");
        } else if (roleNames.contains("ROLE_USER") || roleNames.contains("User")) {
            // Approved 상태의 TrainerApplicationEntity 리스트를 조회합니다.
            List<TrainerApplicationEntity> applications = 
                    trainerApplicationRepository.findByUserUserIdAndStatus(userId, TrainerApplicationEntity.Status.Approved);
            if (!applications.isEmpty()) {
                response.sendRedirect("/firstUserCalendar");
            } else {
                response.sendRedirect("/");
            }
        } else {
            response.sendRedirect("/");
        }
    }
}
