package djj.spitching_be.Domain;

import djj.spitching_be.Dto.PracticeRequestDto;
import djj.spitching_be.Dto.PresentationRequestDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity(name="practices")
public class Practice extends Timestamped{
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;

    // 연습 타입
    public enum PracticeType
    {
        PARTIAL,
        FULL
    };

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PracticeType practice_type;

    // 스크립트 유사도 필드
    @Column(name = "script_similarity")
    private Double scriptSimilarity;

    // 하나의 발표 연습(presentation)객체는 여러 연습을 가질 수 있다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="presentation_id", nullable = false)
    private Presentation presentation;

    // requestDto 정보를 가져와서 entity 만들 때 사용
    public Practice(PracticeRequestDto requestDto, Presentation presentation){
        this.practice_type = requestDto.getPractice_type();
        this.presentation = presentation;
    }

    // 업데이트 메소드
    public void update(PracticeRequestDto requestDto) {
        this.practice_type = requestDto.getPractice_type();
    }
}
