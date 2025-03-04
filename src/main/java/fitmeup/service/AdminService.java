package fitmeup.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import fitmeup.entity.UserEntity;
import fitmeup.entity.UserEntity.Role;
import fitmeup.repository.AdminRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;

    /**
     * 승인된 트레이너 목록 (role이 Trainer)
     */
    public List<UserEntity> getTrainers() {
        return adminRepository.findByRole(Role.Trainer);
    }

    /**
     * 승인 대기 중인 트레이너 목록 (role이 PendingTrainer)
     */
    public List<UserEntity> getPendingTrainers() {
        return adminRepository.findByRole(Role.PendingTrainer);
    }

    /**
     * 일반 회원 목록 (role이 User)
     */
    public List<UserEntity> getUsers() {
        return adminRepository.findByRole(Role.User);
    }

    /**
     * 트레이너 삭제 (거절 또는 삭제 처리)
     */
    @Transactional
    public void deleteTrainer(Long trainerId) {
        adminRepository.deleteById(trainerId);
    }

    /**
     * 트레이너 승인 처리: PendingTrainer → Trainer로 역할 변경
     */
    @Transactional
    public void approveTrainer(Long trainerId) {
        UserEntity trainer = adminRepository.findById(trainerId)
            .orElseThrow(() -> new IllegalArgumentException("해당 트레이너가 존재하지 않습니다."));
        // 현재 역할이 PendingTrainer인 경우 승인 처리
        if (trainer.getRole() == Role.PendingTrainer) {
            trainer.setRole(Role.Trainer);
            adminRepository.save(trainer);
        }
    }

    /**
     * 트레이너 승인 거절: PendingTrainer 상태인 경우 삭제
     */
    @Transactional
    public void rejectTrainer(Long trainerId) {
        adminRepository.deleteById(trainerId);
    }

    /**
     * 일반 회원 삭제
     */
    @Transactional
    public void deleteUser(Long userId) {
        adminRepository.deleteById(userId);
    }

    /**
     * 공지사항 업데이트 (구현 보류)
     */
    @Transactional
    public void updateNotice(String noticeText) {
        // TODO: Announcement 엔티티를 이용하여 DB에 저장하는 로직 구현
        System.out.println("공지사항 업데이트: " + noticeText);
    }
}
