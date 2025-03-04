package fitmeup.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import fitmeup.entity.UserEntity;

@Repository
public interface AdminRepository extends JpaRepository<UserEntity, Long> {

    // 역할별 사용자 조회
    List<UserEntity> findByRole(UserEntity.Role role);
}
