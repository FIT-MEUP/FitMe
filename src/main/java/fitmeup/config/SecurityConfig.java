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

    private final CustomLogoutSuccessHandler logoutSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;
    private final LoginSuccessHandler loginSuccessHandler;



    // ★ Method Injection: LoginSuccessHandler, CustomLogoutSuccessHandler를
    //   이 filterChain() 메서드의 파라미터로 주입받는다
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http)
        throws Exception {
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/", "/user/**", "/trainer/**", "/trainers","/trainerJoin","/videos/**",
                "/api/**", "/images/**", "/js/**", "/css/**"
            ).permitAll()
            .requestMatchers("/admin/**").hasAuthority("Admin")
            .requestMatchers("/user/mypage/**").hasAnyAuthority("Admin", "User")
            .requestMatchers("/user/deleteAccount").authenticated()
            .anyRequest().permitAll() 
        );

        // ✅ 로그인 설정 (세션 유지)
        http.formLogin(auth -> auth
            .loginPage("/user/login")
            .loginProcessingUrl("/user/loginProc")
            .usernameParameter("userEmail")
            .passwordParameter("password")
            .successHandler(loginSuccessHandler)
            .failureHandler(loginFailureHandler)  
            .permitAll()
        );

        // ✅ remember-me 설정 추가 (자동 로그인 기능)
        http.rememberMe(auth -> auth
            .key("uniqueAndSecret")
            .tokenValiditySeconds(86400)  // 1일 동안 유지
        );

        // 로그아웃 설정
        http.logout(auth -> auth
            .logoutUrl("/user/logout")
            .logoutSuccessHandler(logoutSuccessHandler)
            .invalidateHttpSession(true)
            .clearAuthentication(true)
            .deleteCookies("JSESSIONID", "remember-me")
        );

        http.csrf(auth -> auth.disable());

        return http.build();
    }

    @Bean
    BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
