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
        return userRepository.existsById(userId); // 불필요한 반전(!) 제거
    }

    /**
     * 회원 가입 처리
     * 회원가입 폼에서 입력받은 raw 비밀번호는 UserDTO의 password 필드에 담긴다고 가정.
     */
    public boolean joinProc(UserDTO userDTO) {
        // 이미 가입된 이메일인지 확인
        if (userRepository.findByUserEmail(userDTO.getUserEmail()).isPresent()) {
            return false; // 중복 이메일 방지
        }
        
        // 비밀번호 암호화
        String encryptedPassword = bCryptPasswordEncoder.encode(userDTO.getPassword());
        
        // DTO → Entity 변환 및 저장
        UserEntity entity = userDTO.toEntity(encryptedPassword);
        userRepository.save(entity);
        
        return entity.getUserId() != null;
    }

    /**
     * 입력한 비밀번호가 맞는지 확인
     */
    public boolean pwdCheck(Long userId, String userPwd) {
        Optional<UserEntity> temp = userRepository.findById(userId);
        if (temp.isPresent()) {
            UserEntity entity = temp.get();
            return bCryptPasswordEncoder.matches(userPwd, entity.getPassword()); // true/false 반환
        }
        return false; // 아이디가 없으면 false 반환
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
            entity.setPassword(bCryptPasswordEncoder.encode(userDTO.getPassword()));
            entity.setUserEmail(userDTO.getUserEmail());
            userRepository.save(entity); // 변경사항 저장 추가!
        }
    }

    
    // 이름과 연락처를 기반으로 이메일 찾기
    public String findUserEmail(String userName, String userContact) {
        return userRepository.findEmailByUserNameAndUserContact(userName, userContact)
                .orElse("존재하지 않는 회원정보입니다.");
    }
}
