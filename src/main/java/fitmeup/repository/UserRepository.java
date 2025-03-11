package fitmeup.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import fitmeup.entity.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUserEmail(String email);
    Optional<UserEntity> findByUserContact(String contact);
    List<UserEntity> findByRole(UserEntity.Role role);
    Optional<UserEntity> findById(Long id);
    
 // 이름과 연락처가 일치하는 이메일 조회
    @Query("SELECT u.userEmail FROM UserEntity u WHERE u.userName = :userName AND u.userContact = :userContact")
    Optional<String> findEmailByUserNameAndUserContact(@Param("userName") String userName, @Param("userContact") String userContact);


}
