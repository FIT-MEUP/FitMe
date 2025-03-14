package fitmeup.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import fitmeup.dto.ChatDTO;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "chat")  // DDL의 테이블명 "chats"와 일치
public class ChatEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "chat_id")
  private Long chatId;

  @ManyToOne
  @JoinColumn(name = "sender_id", nullable = false, foreignKey = @ForeignKey(name = "FK_sender"))
  private UserEntity sender;

  @ManyToOne
  @JoinColumn(name = "receiver_id", nullable = false, foreignKey = @ForeignKey(name = "FK_receiver"))
  private UserEntity receiver;

  @Column(name = "message", columnDefinition = "TEXT")
  private String message;

  @Column(name = "sent_at", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
  private LocalDateTime sentAt;

  @Column(name = "is_read")
  private Boolean isRead;

  @Column(name = "original_file_name", length = 500)
  private String originalFileName;

  @Column(name = "saved_file_name", length = 500)
  private String savedFileName;

  @Column(name = "file_type", length = 20)
  private String fileType;

  @Column(name = "file_url", length = 1000)
  private String fileUrl;

  /**
   * ChatDTO와 UserEntity(발신자, 수신자)를 이용하여 ChatEntity로 변환하는 헬퍼 메서드입니다.
   */
  public static ChatEntity toEntity(ChatDTO chatDTO, UserEntity sender, UserEntity receiver) {
    return ChatEntity.builder()
        .sender(sender)
        .receiver(receiver)
        .message(chatDTO.getMessage())
        .sentAt(LocalDateTime.now())
        .isRead(false)  // 기본값 false
        .originalFileName(chatDTO.getOriginalFileName())
        .savedFileName(chatDTO.getSavedFileName())
        .fileType(chatDTO.getFileType())
        .fileUrl(chatDTO.getFileUrl())
        .build();
  }
}
