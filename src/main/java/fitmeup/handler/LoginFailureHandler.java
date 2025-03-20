package fitmeup.handler;

import java.io.IOException;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LoginFailureHandler implements AuthenticationFailureHandler {

	  @Override
	    public void onAuthenticationFailure(HttpServletRequest request, 
	                                        HttpServletResponse response,
	                                        AuthenticationException exception) throws IOException, ServletException {
	        log.warn("🚨 로그인 실패: {}", exception.getMessage());

	        // ✅ Flash Attribute로 에러 메시지 저장
	        request.getSession().setAttribute("errorMessage", "이메일 또는 비밀번호가 올바르지 않습니다.");

	        // ✅ 로그인 페이지로 리다이렉트
	        response.sendRedirect("/user/login");
	    }
	}