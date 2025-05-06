package djj.spitching_be.Domain;

import jakarta.persistence.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SttData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 필러(추임새) 통계
    private Integer fillerEo; // 어
    private Integer fillerEum; // 음
    private Integer fillerGeu; // 그
    private Integer totalFillerCount; // 불필요한 추임새 총 개수
    private Double fillerRatio; // 발화시간 대비 추임새 비율(%)

    // 침묵 통계
    private Integer silenceRatio; // 침묵비율(%)
    private Integer speakingRatio; // 발화비율(%)
    private Integer totalPresentationTime; // 전체발표시간(초)

    // STT 점수
    private Double fluencyScore; // 발표 유창성 점수

    @CreatedDate
    private LocalDateTime createdAt;

    // 연관 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "presentation_id", nullable = false)
    private Presentation presentation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "practice_id", nullable = false)
    private Practice practice;

    // 트랜스크립트 세그먼트 (1:N 관계)
    @OneToMany(mappedBy = "sttData", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SttTranscriptSegment> transcriptSegments = new ArrayList<>();
}
