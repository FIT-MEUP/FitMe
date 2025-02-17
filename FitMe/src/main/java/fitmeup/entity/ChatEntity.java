package fitmeup.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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
@Entity
@Table(name="chat")
public class ChatEntity {
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
