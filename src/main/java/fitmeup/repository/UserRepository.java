package fitmeup.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fitmeup.entity.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

	
	 // ✅ 이메일로 사용자 찾기 (로그인 또는 중복 체크)

    Optional<UserEntity> findByUserEmail(String email);

    // ✅ 연락처로 사용자 찾기 (중복 체크)
    Optional<UserEntity> findByUserContact(String contact);
    
    // ✅ 특정 역할(예: 트레이너)인 사용자 찾기
    List<UserEntity> findByRole(UserEntity.Role role);
    
    // 사용자 ID로 사용자 조회
    Optional<UserEntity> findById(Long id);
}
