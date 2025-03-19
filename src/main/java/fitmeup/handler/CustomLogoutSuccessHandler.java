package fitmeup.handler;

import fitmeup.dto.LoginUserDetails;
import fitmeup.service.UserService;
import java.io.IOException;
import java.util.Collection;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {


	@Override
	public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {

		String refererUrl = request.getHeader("Referer");
		if (refererUrl != null) {
			response.sendRedirect(refererUrl); // 이전 페이지로 리다이렉트
		} else {
			response.sendRedirect("/"); // Referer가 없을 경우 기본 URL로 리다이렉트
		}
	}

}