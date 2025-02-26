package fitmeup.handler;

import java.io.IOException;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler{
	@Override
	public void onAuthenticationFailure(
			HttpServletRequest request, 
			HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
	
		String errMessage = "";
		
		if (exception instanceof BadCredentialsException) {
			errMessage += "아이디나 비밀번호가 잘못되었습니다.";
		} else {
			errMessage += "로그인에 실패했습니다. 관리자에게 문의하세요.";
		}
		
	    // URL 인코딩 처리
	    String encodedErrMessage = java.net.URLEncoder.encode(errMessage + " (" + exception.getMessage() + ")", "UTF-8");

	    // 리다이렉트 처리
	    response.sendRedirect("/user/login?error=true&errMessage=" + encodedErrMessage);
	}
	
}