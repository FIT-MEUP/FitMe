package fitmeup.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 채팅 메시지 DTO 클래스
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

  private Long senderId;     // 발신자 (메시지를 보낸 사람)
  private Long receiverId;   // 수신자 (1:1 채팅에서 메시지를 받는 사람)
  private String content;    // 메시지 내용 (텍스트 또는 파일 정보 등)
  private LocalDateTime time;       // 메시지 전송 시각 (문자열 형태, 필요에 따라 LocalDateTime 등으로 변경 가능)
}
