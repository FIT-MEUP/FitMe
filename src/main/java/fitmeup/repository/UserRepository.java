package fitmeup.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import fitmeup.entity.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUserEmail(String email);
    Optional<UserEntity> findByUserContact(String contact);
    List<UserEntity> findByRole(UserEntity.Role role);
    Optional<UserEntity> findById(Long id);
}
