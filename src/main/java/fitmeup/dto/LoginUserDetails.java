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
    private String password;
    private String userName;
    private String email;
    private String roles;
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(roles));
    }
    
    @Override
    public String getPassword() {
        return this.password;
    }
    
    @Override
    public String getUsername() {
        return userId != null ? userId.toString() : "";
    }
    
    // 추가: 화면에 표시할 이름 (실제 회원가입 시 입력한 이름)
    public String getDisplayName() {
        return this.userName;
    }
    
    public static LoginUserDetails toDTO(UserEntity entity) {
        return LoginUserDetails.builder()
                .userId(entity.getUserId())
                .password(entity.getPassword())
                .userName(entity.getUserName())
                .email(entity.getUserEmail())
                .roles(entity.getRole().name())
                .build();
    }
}
