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
public class CommentDTO {
    private int commentId;
    private int userId;
    private Integer workoutId;
    private Integer mealId;
    private String content;
    private LocalDateTime createdAt;
    
}
