package djj.spitching_be.Domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "gesture_data")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GestureData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int gestureScore;
    private int straightScore;
    private int explainScore;
    private int crossedScore;
    private int raisedScore;
    private int faceScore;

    @Column(length = 255)
    private String videoUrl;

    private LocalDateTime createdAt;
    private Long presentationId;

}
