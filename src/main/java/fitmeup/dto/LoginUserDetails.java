package fitmeup.dto;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import fitmeup.entity.UserEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class LoginUserDetails implements UserDetails {
    private static final long serialVersionUID = 1L;
    
    private Long userId;
    // 암호화된 비밀번호를 담는 필드 이름 통일: passwordHash
    private String passwordHash;
    private String userName;
    private String email;
    private String roles;
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(roles));
    }
    
    @Override
    public String getPassword() {
        return this.passwordHash;
    }
    
    @Override
    public String getUsername() {
        return userId != null ? userId.toString() : "";
    }
    
    public static LoginUserDetails toDTO(UserEntity entity) {
        return LoginUserDetails.builder()
                .userId(entity.getUserId())
                .passwordHash(entity.getPasswordHash())
                .userName(entity.getUserName())
                .email(entity.getUserEmail())
                .roles(entity.getRole().name())
                .build();
    }
}
