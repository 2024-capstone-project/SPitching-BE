package djj.spitching_be.Domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor // 매개변수 없는 기본 생성자를 생성
@AllArgsConstructor // 모든 필드를 매개변수로 갖는 생성자를 생성
@Builder
@EntityListeners(AuditingEntityListener.class)  // 이 어노테이션 추가
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