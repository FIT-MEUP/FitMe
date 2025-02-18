package fitmeup.dto;

import fitmeup.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class LoginUserDetails implements UserDetails {
    private static final long serialVersionUID = 1L;
    
    private Long userId;       // PK로 Long 사용
    private String userPwd;
    private String userName;	
    private String email;
    private String roles;
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(roles));
    }
    
    @Override
    public String getPassword() { 
        return this.userPwd;
    }
    
    @Override
    public String getUsername() {
        return this.email; // 로그인 시 email을 username으로 사용
    }
    
    // 사용자 실명에 접근하기 위한 추가 getter
    public String getUserName() {
        return this.userName;
    }
    
    public String getEmail() {
        return this.email;
    }
    
    // UserEntity -> LoginUserDetails 변환 메서드
    public static LoginUserDetails toDTO(UserEntity entity) {
        return LoginUserDetails.builder()
                .userId(entity.getUserId())
                .userPwd(entity.getUserPwd())
                .userName(entity.getUserName())
                .email(entity.getEmail())    // entity의 getEmail() 사용
                .roles(entity.getRoles())       // entity의 getRole() 사용
                .build();
    }
    
    // 나머지 UserDetails 메서드 구현 (모두 true로 처리)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    @Override
    public boolean isEnabled() {
        return true;
    }
}
