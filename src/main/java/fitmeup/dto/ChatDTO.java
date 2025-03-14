package fitmeup.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import fitmeup.entity.ChatEntity;

/**
 * ChatDTO 클래스
 *
 * 채팅 메시지 데이터 전송 객체(DTO)입니다.
 * - 발신자, 수신자, 메시지 내용, 파일 정보 등을 포함하여 채팅 메시지를 전달하는 데 사용됩니다.
 * - ChatEntity와 변환하여 데이터베이스와 연동됩니다.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ChatDTO {

  private Long chatId;                // chats 테이블의 chat_id
  private Long senderId;              // sender_id: 발신자 사용자 id
  private Long receiverId;            // receiver_id: 수신자 사용자 id
  private String message;             // 메시지 내용
  private LocalDateTime sentAt;       // 전송 시각
  private Boolean isRead;             // 메시지 읽음 여부
  private String originalFileName;    // 업로드된 원본 파일명
  private String savedFileName;       // 서버에 저장된 파일명 (UUID 처리된 값)
  private String fileType;            // 파일 타입 (ENUM: 'image', 'video', 'document', 'audio')
  private String fileUrl;             // 파일 URL

  /**
   * ChatEntity를 ChatDTO로 변환하는 정적 메서드
   *
   * @param entity 채팅 메시지 엔티티
   * @return 변환된 ChatDTO 객체
   */
  public static ChatDTO toDTO(ChatEntity entity) {
    if(entity == null) return null;
    return ChatDTO.builder()
        .chatId(entity.getChatId())
        .senderId(entity.getSender().getUserId())
        .receiverId(entity.getReceiver().getUserId())
        .message(entity.getMessage())
        .sentAt(entity.getSentAt())
        .isRead(entity.getIsRead())
        .originalFileName(entity.getOriginalFileName())
        .savedFileName(entity.getSavedFileName())
        .fileType(entity.getFileType())
        .fileUrl(entity.getFileUrl())
        .build();
  }
}
