package fitmeup.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ChatDTO {
    private int chatId;
    private int senderId;
    private int receiverId;
    private String message;
    private LocalDateTime sentAt;
    private boolean isRead;
    private String originalFileName;
    private String savedFileName;
    private String fileType;
    private String fileUrl;
    
}
