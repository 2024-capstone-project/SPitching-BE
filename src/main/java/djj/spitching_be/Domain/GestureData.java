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
    private LocalDateTime createdAt;

    @Column(length = 255)
    private String videoUrl;

    // Presentation 엔티티와의 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "presentation_id", nullable = false)
    private Presentation presentation;

    // User 엔티티와의 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 테이블 간의 관계
    // 1. 한 명의 유저는 여러 발표 연습을 가질 수 있다.
    // 2. 하나의 발표 연습은 여러 제스처 피드백 데이터와 연결이 되어있다.
    // 3. 한 명의 유저는 여러개의 제스처 데이터를 가질 수 있다.
    // User (1) ---> (N) Presentation (1) ---> (N) GestureData

}
