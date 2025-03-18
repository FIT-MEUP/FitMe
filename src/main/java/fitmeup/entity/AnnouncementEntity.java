package fitmeup.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.CurrentTimestamp;

import fitmeup.dto.AnnouncementDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "Announcement")
public class AnnouncementEntity {
	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long announcementId;

	    @ManyToOne
	    @JoinColumn(name="author_id",referencedColumnName = "user_id")
        private UserEntity user;

	    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
	    private String content;

	    @Column(name = "created_at", nullable = false)
	    @CreationTimestamp
	    private LocalDateTime createdAt;

        public static AnnouncementEntity toEntity(AnnouncementDTO dto, UserEntity user){
            return AnnouncementEntity.builder()
                    .announcementId(dto.getAnnouncementId())
                    .user(user)
                    .content(dto.getContent())
                    .createdAt(dto.getCreatedAt())
                    .build();
        }
	}

