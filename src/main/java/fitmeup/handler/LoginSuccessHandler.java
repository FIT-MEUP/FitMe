package fitmeup.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        log.info("로그인 성공");
        
     // ✅ 세션 유지 확인
        request.getSession().setAttribute("userEmail", authentication.getName());

        // 사용자 권한 리스트를 생성 (ROLE_ 접두어가 붙어있거나 안 붙어있을 수 있으므로 둘 다 처리)
        List<String> roleNames = new ArrayList<>();
        authentication.getAuthorities().forEach(auth -> roleNames.add(auth.getAuthority()));

        // 권한에 따라 리다이렉트 처리
        if (roleNames.contains("ROLE_ADMIN") || roleNames.contains("Admin")) {
            response.sendRedirect("/admin/adminpage");
        } else if (roleNames.contains("ROLE_PENDINGTRAINER") || roleNames.contains("PendingTrainer")) {
            response.sendRedirect("/user/pendingTrainer");
        } else if (roleNames.contains("ROLE_TRAINER") || roleNames.contains("Trainer")) {
            response.sendRedirect("/trainerschedule");
        } else if (roleNames.contains("ROLE_USER") || roleNames.contains("User")) {
            response.sendRedirect("/trainers");
        } else {
            // 다른 권한이 있을 경우 기본적으로 홈으로 리다이렉트
            response.sendRedirect("/");
        }
    }
}
