package djj.spitching_be.Domain;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
public class EyeData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int eyecontactScore;

    @CreatedDate
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

    // Practice 엔티티와의 관계
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "practice_id", nullable = false)
    private Practice practice;
}