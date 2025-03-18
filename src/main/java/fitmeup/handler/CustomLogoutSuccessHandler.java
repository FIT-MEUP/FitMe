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
@RequiredArgsConstructor
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

	private final UserService userService;

	private final SimpMessagingTemplate messagingTemplate;

	@Override
	public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {


		if (authentication != null) {
			Long userId = ((LoginUserDetails) authentication.getPrincipal()).getUserId();

			// (1) DB isOnline = false
			userService.setOnline(userId, false);
			log.info("유저({}) 로그아웃 -> isOnline=false", userId);

			// (2) STOMP 브로드캐스트 알림
			messagingTemplate.convertAndSend("/topic/onlineStatus",
					"LOGOUT:" + userId);
		}


		String refererUrl = request.getHeader("Referer");
		if (refererUrl != null) {
			response.sendRedirect(refererUrl); // 이전 페이지로 리다이렉트
		} else {
			response.sendRedirect("/"); // Referer가 없을 경우 기본 URL로 리다이렉트
		}
	}

}