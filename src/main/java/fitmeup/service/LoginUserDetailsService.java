package fitmeup.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import fitmeup.dto.LoginUserDetails;
import fitmeup.entity.UserEntity;
import fitmeup.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoginUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    
    /**
     * 이메일(문자열)을 받아 사용자 정보를 조회 (이메일 로그인)
     */
    @Override
    public UserDetails loadUserByUsername(String userEmail) throws UsernameNotFoundException {
        // 이메일을 이용하여 사용자 조회
        UserEntity userEntity = userRepository.findByUserEmail(userEmail)
            .orElseThrow(() -> new UsernameNotFoundException("이메일이나 비밀번호가 틀렸습니다."));
        return LoginUserDetails.toDTO(userEntity);
    }
}
