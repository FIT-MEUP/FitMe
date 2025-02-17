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
public class NotificationDTO {
    private int notificationId;
    private int userId;
    private String message;
    private LocalDateTime createdAt;
    private String type;
    
}
