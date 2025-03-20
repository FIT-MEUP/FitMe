package fitmeup.repository;

import fitmeup.entity.ChatEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

/**
 * ChatRepository는 채팅 데이터를 관리하는 JPA 리포지토리 인터페이스입니다.
 * 기본 CRUD 외에 사용자 id 기반의 조회 메서드를 제공합니다.
 */
public interface ChatRepository extends JpaRepository<ChatEntity, Long> {

  /**
   * 특정 사용자의 id를 기준으로 해당 사용자가 발신자 또는 수신자인 모든 채팅 메시지를 조회합니다.
   *
   * @param userId 조회할 사용자의 id
   * @return 해당 사용자가 보낸 또는 받은 모든 채팅 메시지 리스트
   */
  @Query("SELECT c FROM ChatEntity c WHERE c.sender.userId = :userId OR c.receiver.userId = :userId")
  List<ChatEntity> findBySenderOrReceiver(@Param("userId") Long userId);

  /**
   * 두 사용자 간의 채팅 내역을 id 기반으로 조회합니다.
   * 양방향(한쪽이 발신자, 다른 쪽이 수신자 또는 그 반대) 메시지를 오래된 순으로 정렬하여 반환합니다.
   *
   * @param userId1 첫 번째 사용자의 id
   * @param userId2 두 번째 사용자의 id
   * @return 두 사용자가 주고받은 모든 채팅 메시지 리스트
   */
  @Query("SELECT c FROM ChatEntity c " +
      "WHERE (c.sender.userId = :userId1 AND c.receiver.userId = :userId2) " +
      "   OR (c.sender.userId = :userId2 AND c.receiver.userId = :userId1) " +
      "ORDER BY c.sentAt ASC")
  List<ChatEntity> findConversation(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

  /**
   * 서버에 저장된 파일명을 기준으로 채팅 엔티티를 조회합니다.
   *
   * @param savedFileName 서버에 저장된 UUID 파일명
   * @return 해당 파일명을 가진 채팅 메시지 엔티티 (존재하면)
   */
  Optional<ChatEntity> findBySavedFileName(String savedFileName);
}