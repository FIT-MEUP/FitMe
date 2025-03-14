package fitmeup.service;

import fitmeup.dto.ChatMessage;
import fitmeup.dto.UserDTO;
import fitmeup.entity.ChatEntity;
import fitmeup.entity.TrainerApplicationEntity;
import fitmeup.entity.UserEntity;
import fitmeup.repository.ChatRepository;
import fitmeup.repository.TrainerApplicationRepository;
import fitmeup.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

  private final ChatRepository chatRepository;
  private final UserRepository userRepository;
  private final TrainerApplicationRepository trainerApplicationRepository;


  /**
   * applicationId를 통해 신청서에서 신청한 회원의 정보를 조회하여 UserDTO로 반환합니다.
   * (로그인한 트레이너가 대상 회원을 선택한 경우)
   */
  public UserDTO getUserDTOByApplicationId(Long applicationId) {
    TrainerApplicationEntity application = trainerApplicationRepository.findById(applicationId)
        .orElseThrow(() -> new RuntimeException("Application not found with id: " + applicationId));
    UserEntity applicant = application.getUser();
    return UserDTO.builder()
        .userId(applicant.getUserId())
        .userName(applicant.getUserName())
        .build();
  }

  /**
   * applicationId를 통해 신청서에서 트레이너의 정보를 조회하여 UserDTO로 반환합니다.
   * (로그인한 유저가 대상 트레이너와 대화할 경우)
   */
  public UserDTO getTrainerUserDTOByApplicationId(Long applicationId) {
    TrainerApplicationEntity application = trainerApplicationRepository.findById(applicationId)
        .orElseThrow(() -> new RuntimeException("Application not found with id: " + applicationId));
    UserEntity trainerUser = application.getTrainer().getUser();
    return UserDTO.builder()
        .userId(trainerUser.getUserId())
        .userName(trainerUser.getUserName())
        .build();
  }


  /**
   * 두 사용자 간의 대화 내역을 id 기반으로 조회합니다.
   * 현재 사용자와 대화 상대의 UserEntity를 조회한 후,
   * 두 사용자 간의 채팅 메시지를 오래된 순으로 정렬하여 가져오며,
   * 현재 사용자가 수신한 읽지 않은 메시지는 읽음 처리합니다.
   *
   * @param otherUserId 대화 상대의 사용자 id
   * @param currentUserId 현재 로그인한 사용자의 id
   * @return 두 사용자 간의 대화 내용을 ChatMessage 리스트로 반환
   */
  public List<ChatMessage> getConversation(Long otherUserId, Long currentUserId) {
    UserEntity currentUser = userRepository.findById(currentUserId)
        .orElseThrow(() -> new RuntimeException("현재 사용자를 찾을 수 없습니다."));
    UserEntity otherUser = userRepository.findById(otherUserId)
        .orElseThrow(() -> new RuntimeException("대화 상대방 사용자를 찾을 수 없습니다."));

    List<ChatEntity> chatEntities = chatRepository.findConversation(
        currentUser.getUserId(), otherUser.getUserId());

    // 읽지 않은 메시지를 읽음 처리
    chatEntities.forEach(chat -> {
      if (chat.getReceiver().getUserId().equals(currentUserId) && Boolean.FALSE.equals(chat.getIsRead())) {
        chat.setIsRead(true);
        chatRepository.save(chat);
      }
    });

    return chatEntities.stream()
        .map(chat -> ChatMessage.builder()
            .senderId(chat.getSender().getUserId())
            .receiverId(chat.getReceiver().getUserId())
            .content(chat.getMessage())
            .time(chat.getSentAt())
            .build())
        .collect(Collectors.toList());
  }

  /**
   * 새로운 채팅 메시지를 저장합니다.
   * 전달받은 ChatMessage의 senderId, receiverId를 이용해 UserEntity를 조회하고,
   * ChatEntity를 생성하여 DB에 저장한 후, 저장된 엔티티를 ChatMessage로 변환하여 반환합니다.
   *
   * @param chatMessage 저장할 메시지 데이터 (senderId, receiverId, content 등 포함)
   * @return 저장된 메시지를 ChatMessage로 반환
   */
  public ChatMessage saveMessage(ChatMessage chatMessage) {
    UserEntity sender = userRepository.findById(chatMessage.getSenderId())
        .orElseThrow(() -> new RuntimeException("발신자 정보를 찾을 수 없습니다."));
    UserEntity receiver = userRepository.findById(chatMessage.getReceiverId())
        .orElseThrow(() -> new RuntimeException("수신자 정보를 찾을 수 없습니다."));

    // 메시지 내용은 content에서 가져오고, 현재 시각(LocalDateTime.now())을 sentAt에 할당
    ChatEntity chatEntity = ChatEntity.builder()
        .sender(sender)
        .receiver(receiver)
        .message(chatMessage.getContent())
        .sentAt(LocalDateTime.now())
        .isRead(false)
        .build();

    ChatEntity savedEntity = chatRepository.save(chatEntity);
    log.info("메시지 저장 성공: {}", savedEntity);

    return ChatMessage.builder()
        .senderId(savedEntity.getSender().getUserId())
        .receiverId(savedEntity.getReceiver().getUserId())
        .content(savedEntity.getMessage())
        .time(savedEntity.getSentAt())
        .build();
  }

  /**
   * 서버에 저장된 파일명을 기준으로 채팅 메시지를 조회하여 ChatMessage로 변환합니다.
   *
   * @param savedFileName 서버에 저장된 UUID 파일명
   * @return 해당 파일명을 가진 채팅 메시지의 ChatMessage (존재하면)
   */
  // findBySavedFileName 메서드를 ChatMessage 기반으로 수정 (파일 관련 정보가 없으므로 저장된 파일명으로 대체)
  public Optional<ChatMessage> findBySavedFileName(String savedFileName) {
    return chatRepository.findBySavedFileName(savedFileName)
        .map(chat -> ChatMessage.builder()
            .senderId(chat.getSender().getUserId())
            .receiverId(chat.getReceiver().getUserId())
            .content(chat.getMessage())
            .time(chat.getSentAt())
            .build());
  }
}
