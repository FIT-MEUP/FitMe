package fitmeup.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import fitmeup.service.LoginUserDetailsService;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final LoginUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 보호는 필요에 따라 활성화
            .csrf().disable()
            .authorizeHttpRequests(authorize -> authorize
                // 회원가입, 로그인 페이지, 정적 리소스는 모두 접근 허용
                .requestMatchers("/user/join", "/user/register", "/user/login", "/css/**", "/js/**").permitAll()
                // 그 외의 모든 요청은 인증 필요
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                // 커스텀 로그인 페이지 지정
                .loginPage("/user/login")
                // 로그인 폼의 action URL (로그인 처리 URL)
                .loginProcessingUrl("/user/loginProc")
                // 로그인 성공 시 리다이렉트할 기본 URL
                .defaultSuccessUrl("/home", true)
                // 로그인 실패 시 리다이렉트할 URL (error 파라미터 활용)
                .failureUrl("/user/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/user/logout")
                .logoutSuccessUrl("/user/login")
                .permitAll()
            )
            // 세션 기반 인증 사용 (JWT 등 토큰 기반 인증을 사용할 경우 stateless로 변경)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            );
        return http.build();
    }

    // PasswordEncoder 빈 (회원가입 시 암호화, 로그인 시 매칭)
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // DaoAuthenticationProvider 빈 설정 (UserDetailsService와 PasswordEncoder 설정)
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);  // 커스텀 UserDetailsService 사용
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
}
