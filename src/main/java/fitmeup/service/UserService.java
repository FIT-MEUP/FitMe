package fitmeup.service;

import fitmeup.dto.UserDTO;
import fitmeup.entity.UserEntity;
import fitmeup.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    
    /**
     * 이메일 중복 확인 (회원가입 시 사용)
     */
    public boolean existEmail(String email) {
        boolean exists = userRepository.existsByEmail(email);
        log.info("이메일 존재여부: {}", exists);
        return !exists;
    }
    
    /**
     * 회원가입 처리
     */
    public boolean joinProc(UserDTO userDTO) {
        // 비밀번호 암호화
        userDTO.setUserPwd(bCryptPasswordEncoder.encode(userDTO.getUserPwd()));
        // DTO -> Entity 변환 (UserEntity.toEntity 메서드가 DDL에 맞게 필드를 매핑해야 함)
        UserEntity entity = UserEntity.toEntity(userDTO);
        userRepository.save(entity); // 저장 성공 시 예외가 없으면 true 반환
        return true;
    }
    
    /**
     * 입력한 비밀번호가 맞는지 확인 (로그인 또는 개인정보 수정 전)
     */
    public UserDTO pwdCheck(String email, String userPwd) {
        Optional<UserEntity> temp = userRepository.findByEmail(email);
        if (temp.isPresent()) {
            UserEntity entity = temp.get();
            String encodedPwd = entity.getUserPwd(); // 암호화된 비밀번호
            if (bCryptPasswordEncoder.matches(userPwd, encodedPwd)) {
                return UserDTO.toDTO(entity);
            }
        }
        return null;
    }
    
    /**
     * DB에서 개인정보 수정 처리
     */
    @Transactional
    public void updateProc(UserDTO userDTO) {
        // DTO의 userId 필드는 Long 타입이어야 함
        Long id = userDTO.getUserId();
        Optional<UserEntity> temp = userRepository.findById(id);
        if (temp.isPresent()) {
            UserEntity entity = temp.get();
            // 사용자가 입력한 새로운 비밀번호와 이메일 업데이트
            String encodedPwd = bCryptPasswordEncoder.encode(userDTO.getUserPwd());
            entity.setUserPwd(encodedPwd);
            entity.setEmail(userDTO.getEmail());
            // (필요하다면 다른 필드도 업데이트)
        }
    }
}
