package djj.spitching_be.Domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "presentation_slides")
public class PresentationSlide {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="presentaion_id")
    private Presentation presentation; // 특정 발표 연습과 연결됨

    private Integer slideNumber;  // 슬라이드 번호

    private String imageUrl;  // 변환된 이미지의 URL

    @Lob
    private String script;  // 발표 대본

    private LocalDateTime createdAt = LocalDateTime.now();
}

