package fitmeup.dto;

import java.time.LocalDateTime;

import fitmeup.entity.AnnouncementEntity;
import fitmeup.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AnnouncementDTO {
    private Long announcementId;
    private Long authorId;
    private String content;
    private LocalDateTime createdAt;
    
    public static AnnouncementDTO toDTO(AnnouncementEntity entity, UserEntity user){
        return AnnouncementDTO.builder()
                .announcementId(entity.getAnnouncementId())
                .authorId(user.getUserId())
                .content(entity.getContent())
                .createdAt(entity.getCreatedAt())
                .build();
    }

}