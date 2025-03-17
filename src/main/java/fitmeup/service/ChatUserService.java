package fitmeup.service;

import lombok.RequiredArgsConstructor;
import fitmeup.dto.ChatUserDTO;
import fitmeup.entity.ChatEntity;
import fitmeup.entity.UserEntity;
import fitmeup.repository.ChatRepository;
import fitmeup.repository.UserRepository;
import fitmeup.dto.LoginUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatUserService {

  private final ChatRepository chatRepository;
  private final UserRepository userRepository;

  /**
   * 현재 로그인한 사용자를 제외한 모든 사용자에 대해,
   * 해당 사용자와의 대화에서 읽지 않은 메시지 개수와 온라인 상태를 계산하여 반환합니다.
   *
   * @return 채팅 가능한 사용자 목록 (userId, unreadCount, 온라인 상태 포함)
   */
  public List<ChatUserDTO> getChatUserList() {
    // 현재 로그인한 사용자의 userId를 LoginUserDetails를 통해 가져옵니다.
    Long currentUserId = ((LoginUserDetails) SecurityContextHolder.getContext()
        .getAuthentication().getPrincipal()).getUserId();

    // userId 기준으로 현재 사용자 정보를 조회합니다.
    UserEntity currentUser = userRepository.findById(currentUserId)
        .orElseThrow(() -> new RuntimeException("현재 사용자를 찾을 수 없습니다."));

    // 전체 사용자 목록에서 현재 사용자는 제외합니다.
    List<UserEntity> allUsers = userRepository.findAll();
    allUsers.removeIf(user -> user.getUserId().equals(currentUser.getUserId()));

    // 현재 사용자와 관련된 모든 채팅 메시지를 조회합니다.
    // ChatRepository에서는 userId를 기준으로 조회하는 메서드를 구현해야 합니다.
    List<ChatEntity> chatEntities = chatRepository.findBySenderOrReceiver(currentUserId);
    Map<Long, Integer> unreadCountMap = new HashMap<>();

    // 각 메시지에 대해 읽지 않은 메시지 수를 계산합니다 (현재 사용자가 수신자인 경우).
    for (ChatEntity chat : chatEntities) {
      // 로그 찍기: 각 메시지의 sender, receiver, isRead 상태
      System.out.println("Chat Message - Sender: " + chat.getSender().getUserId() +
          ", Receiver: " + chat.getReceiver().getUserId() +
          ", isRead: " + chat.getIsRead());

      Long otherUserId = chat.getSender().getUserId().equals(currentUser.getUserId())
          ? chat.getReceiver().getUserId()
          : chat.getSender().getUserId();

      if (chat.getReceiver().getUserId().equals(currentUser.getUserId()) && !chat.getIsRead()) {
        unreadCountMap.put(otherUserId, unreadCountMap.getOrDefault(otherUserId, 0) + 1);
      }
    }

    // 로그 찍기: unreadCountMap 최종 내용
    System.out.println("UnreadCountMap: " + unreadCountMap);

    // 사용자별로 ChatUserDTO를 생성 (온라인 상태 포함)
    List<ChatUserDTO> contactedUsers = new ArrayList<>();
    List<ChatUserDTO> notContactedUsers = new ArrayList<>();
    for (UserEntity user : allUsers) {
      Long otherUserId = user.getUserId();
      int unreadCount = unreadCountMap.getOrDefault(otherUserId, 0);
      ChatUserDTO dto = ChatUserDTO.builder()
          .userId(otherUserId)
          .unreadCount(unreadCount)
          .online(user.getIsOnline())  // UserEntity에서 온라인 상태를 나타내는 getter (예: getIsOnline())가 있다고 가정
          .build();
      if (unreadCount > 0) {
        contactedUsers.add(dto);
      } else {
        notContactedUsers.add(dto);
      }
    }

    // 읽지 않은 메시지가 있는 사용자들을 우선 정렬 후, 나머지 사용자를 정렬합니다.
    contactedUsers.sort(Comparator.comparingInt(ChatUserDTO::getUnreadCount).reversed()
        .thenComparing(ChatUserDTO::getUserId));
    notContactedUsers.sort(Comparator.comparing(ChatUserDTO::getUserId));

    List<ChatUserDTO> result = new ArrayList<>();
    result.addAll(contactedUsers);
    result.addAll(notContactedUsers);

    System.out.println("Final ChatUserDTO list: " + result);

    return result;
  }
}
