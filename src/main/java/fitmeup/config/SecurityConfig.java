package fitmeup.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import fitmeup.handler.CustomLogoutSuccessHandler;
import fitmeup.handler.LoginFailureHandler;
import fitmeup.handler.LoginSuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    // 로그인, 로그아웃 핸들러 등록
    private final LoginSuccessHandler loginSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;
    private final CustomLogoutSuccessHandler logoutHandler;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests((auth) -> auth
            .requestMatchers(
                "/",
                "/user/login",
                "/board/boardList",
                "/board/boardDetail",
                "/board/download",
                "/user/join",
                "/user/idCheck",
                "/user/joinProc",
                "/trainer/**",
                "/trainers",
                "/api/**",
                "/images/**",
                "/js/**",
                "/css/**").permitAll()
            // 관리자 접근: hasAuthority("Admin")로 변경 (DB에 Admin 권한은 "Admin" 문자열로 저장)
            .requestMatchers("/admin/**").hasAuthority("Admin")
            // 마이페이지 접근: ADMIN 또는 USER 권한 필요
            .requestMatchers("/user/mypage/**").hasAnyAuthority("Admin", "User")
            .anyRequest().authenticated());
        
        // 인증 실패 시, 리다이렉트 대신 401 JSON 응답을 반환
        http.exceptionHandling(exception -> exception
            .authenticationEntryPoint((request, response, authException) -> {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Unauthorized\"}");
            })
        );
        
        // 로그인 설정: 이메일 로그인으로 변경 (usernameParameter -> "userEmail")
        http.formLogin((auth) -> auth
            .loginPage("/user/login")
            .loginProcessingUrl("/user/loginProc")
            .usernameParameter("userEmail")
            .passwordParameter("password")
            .successHandler(loginSuccessHandler)
            .failureHandler(loginFailureHandler)
            .permitAll());
        
        // 로그아웃 설정
        http.logout((auth) -> auth
            .logoutUrl("/user/logout")
            .logoutSuccessHandler(logoutHandler)
            .invalidateHttpSession(true)
            .clearAuthentication(true));
        
        http.csrf((auth) -> auth.disable());
        
        return http.build();
    }
    
    @Bean
    BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
