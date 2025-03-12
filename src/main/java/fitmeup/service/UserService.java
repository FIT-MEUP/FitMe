package fitmeup.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
     * 회원 가입 처리 (중복 검사 추가)
     */
    public void joinProc(UserDTO userDTO) {
        if (userRepository.findByUserEmail(userDTO.getUserEmail()).isPresent()) {
            throw new IllegalStateException("이미 존재하는 이메일입니다!");
        }

        if (userRepository.findByUserContact(userDTO.getUserContact()).isPresent()) {
            throw new IllegalStateException("이미 등록된 전화번호입니다!");
        }

        // 비밀번호 암호화 후 저장
        String encryptedPassword = bCryptPasswordEncoder.encode(userDTO.getPassword());
        UserEntity entity = userDTO.toEntity(encryptedPassword);
        userRepository.save(entity);
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
    
    /**
     * 비밀번호 찾기 - 콘솔에 임시 비밀번호 출력
     */
    @Transactional
    public boolean verifyUserAndGenerateTempPassword(String userName, String userEmail, String userContact) {
        log.info("🔍 입력된 값: 이름={}, 이메일={}, 연락처={}", userName, userEmail, userContact);

        // 📌 수정된 부분: 전화번호 변환 제거
        Optional<UserEntity> userOpt = userRepository.findByUserNameAndUserEmailAndUserContact(userName, userEmail, userContact);

        if (userOpt.isEmpty()) {
            log.warn("❌ 일치하는 회원 정보 없음: 이름={}, 이메일={}, 연락처={}", userName, userEmail, userContact);

            // 📌 개별 필드별로 디버깅
            Optional<UserEntity> checkByNameAndEmail = userRepository.findByUserNameAndUserEmail(userName, userEmail);
            if (checkByNameAndEmail.isPresent()) {
                UserEntity user = checkByNameAndEmail.get();
                log.warn("✅ 이름과 이메일은 일치! 하지만 연락처가 다름. DB 저장된 연락처: {}", user.getUserContact());
            } else {
                log.warn("❌ 이름과 이메일도 불일치!");
            }

            return false;
        }

        UserEntity user = userOpt.get();

        // ✅ 1. 임시 비밀번호 생성 (8자리 랜덤 문자열)
        String tempPassword = UUID.randomUUID().toString().substring(0, 8);
        String encryptedPassword = bCryptPasswordEncoder.encode(tempPassword);

        // ✅ 2. DB에 저장 (임시 비밀번호)
        user.setPassword(encryptedPassword);
        userRepository.save(user);

        // ✅ 3. 콘솔에 임시 비밀번호 출력 (이메일 전송 대신)
        log.info("📩 임시 비밀번호 생성 완료: {}", tempPassword);
        log.info("✅ {} 님의 이메일 ({})로 비밀번호를 전송했다고 가정합니다.", userName, userEmail);

        return true;
    }
    
    /**
     * 비밀번호 변경 (현재 비밀번호 확인 후 변경)
     */
    @Transactional
    public String changePassword(String email, String currentPassword, String newPassword) {
        Optional<UserEntity> userOpt = userRepository.findByUserEmail(email);

        if (userOpt.isEmpty()) {
            log.warn("❌ 이메일 [{}]에 해당하는 사용자를 찾을 수 없음", email);
            return "이메일이 존재하지 않습니다.";
        }

        UserEntity user = userOpt.get();

        // ✅ 현재 비밀번호 검증
        if (!bCryptPasswordEncoder.matches(currentPassword, user.getPassword())) {
            log.warn("❌ 현재 비밀번호 불일치 (이메일: {})", email);
            return "현재 비밀번호가 일치하지 않습니다.";
        }

        // ✅ 현재 비밀번호와 새 비밀번호가 동일한 경우 예외 처리
        if (bCryptPasswordEncoder.matches(newPassword, user.getPassword())) {
            log.warn("❌ 새 비밀번호가 현재 비밀번호와 동일 (이메일: {})", email);
            return "바꿀 비밀번호는 현재 비밀번호와 같을 수 없습니다.";
        }

        // ✅ 새 비밀번호 유효성 검사 (6자 이상 & 특수문자 포함)
        if (!newPassword.matches("^(?=.*[!@#$%^&*(),.?\":{}|<>]).{6,}$")) {
            log.warn("❌ 비밀번호 유효성 검사 실패 (이메일: {})", email);
            return "비밀번호는 6자 이상이며 특수문자를 포함해야 합니다.";
        }

        // ✅ 새 비밀번호 암호화 후 저장
        user.setPassword(bCryptPasswordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("✅ 비밀번호 변경 완료 (이메일: {})", email);
        return null;  // 성공 시 null 반환
    }
    
    
    @Transactional
    public boolean deleteUser(String email, String password, RedirectAttributes redirectAttributes) {
        Optional<UserEntity> userOpt = userRepository.findByUserEmail(email);

        if (userOpt.isEmpty()) {
            log.warn("❌ 이메일 [{}]에 해당하는 사용자를 찾을 수 없음", email);
            redirectAttributes.addFlashAttribute("error", "이메일을 찾을 수 없습니다!");
            return false;
        }

        UserEntity user = userOpt.get();

        // 비밀번호 확인
        if (!bCryptPasswordEncoder.matches(password, user.getPassword())) {
            log.warn("❌ 비밀번호 불일치 (이메일: {})", email);
            redirectAttributes.addFlashAttribute("error", "비밀번호가 올바르지 않습니다!");
            return false;
        }

        // 사용자 계정 삭제
        userRepository.delete(user);

        log.info("✅ 회원 탈퇴 완료 (이메일: {})", email);
        redirectAttributes.addFlashAttribute("success", "회원 탈퇴가 성공적으로 완료되었습니다.");

        return true;
    }
}
