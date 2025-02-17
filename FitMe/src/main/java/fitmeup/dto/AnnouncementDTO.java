package fitmeup.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class AnnouncementDTO {
    private int announcementId;
    private int authorId;
    private String content;
    private LocalDateTime createdAt;
    
}
