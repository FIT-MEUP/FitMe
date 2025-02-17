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
@Table(name="comment")
public class CommentEntity {
    private int commentId;
    private int userId;
    private Integer workoutId;
    private Integer mealId;
    private String content;
    private LocalDateTime createdAt;
    
}
