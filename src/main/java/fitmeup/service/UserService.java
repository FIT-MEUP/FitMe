package fitmeup.service;

import java.util.Optional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import fitmeup.dto.UserDTO;
import fitmeup.entity.UserEntity;
import fitmeup.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    /**
     * DB에 전달 받은 userId가 존재하는지 확인
     */
    public boolean existId(Long userId) {
        boolean result = userRepository.existsById(userId);
        log.info("아이디 존재여부: {}", result);
        return !result;
    }

    /**
     * 회원 가입 처리
     * 회원가입 폼에서 입력받은 raw 비밀번호는 UserDTO의 passwordHash 필드에 담긴다고 가정.
     */
    public boolean joinProc(UserDTO userDTO) {
        // raw 비밀번호를 암호화
        String encryptedPassword = bCryptPasswordEncoder.encode(userDTO.getPasswordHash());
        // DTO → Entity 변환 시 암호화된 비밀번호 전달
        UserEntity entity = userDTO.toEntity(encryptedPassword);
        userRepository.save(entity);
        // 저장된 엔티티의 userId가 생성되었는지 확인하여 성공 여부 반환
        return entity.getUserId() != null;
    }

    /**
     * 입력한 비밀번호가 맞는지 확인
     */
    public UserDTO pwdCheck(Long userId, String userPwd) {
        Optional<UserEntity> temp = userRepository.findById(userId);
        if (temp.isPresent()) {
            UserEntity entity = temp.get();
            String encodedPwd = entity.getPasswordHash();
            boolean result = bCryptPasswordEncoder.matches(userPwd, encodedPwd);
            if (result) {
                return UserDTO.fromEntity(entity, null);
            }
        }
        return null;
    }

    /**
     * DB에서 개인정보 수정 처리 (비밀번호와 이메일만 수정)
     */
    @Transactional
    public void updateProc(UserDTO userDTO) {
        Long id = userDTO.getUserId();
        Optional<UserEntity> temp = userRepository.findById(id);
        if (temp.isPresent()) {
            UserEntity entity = temp.get();
            String encodedPwd = bCryptPasswordEncoder.encode(userDTO.getPasswordHash());
            String email = userDTO.getUserEmail();
            entity.setPasswordHash(encodedPwd);
            entity.setUserEmail(email);
        }
    }
}
