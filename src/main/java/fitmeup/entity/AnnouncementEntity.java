package fitmeup.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

	    @Column(name = "author_id", nullable = false)
	    private Long authorId;

	    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
	    private String content;

	    @Column(name = "created_at", nullable = false)
	    private LocalDateTime createdAt;
	}

