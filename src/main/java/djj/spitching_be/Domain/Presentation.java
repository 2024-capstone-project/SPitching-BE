package djj.spitching_be.Domain;

import djj.spitching_be.Dto.PresentationRequestDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity(name="presentations")
public class Presentation extends Timestamped{
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;

    // 발표 묶음 제목
    @Column(nullable = false)
    private String title;

    // 발표 묶음 내용
    private String description;

    // 연습 횟수
    private Integer practiceCount;

    // requestDto 정보를 가져와서 entity 만들 때 사용
    public Presentation(PresentationRequestDto requestDto) {
        this.title = requestDto.getTitle();
        this.description = requestDto.getDescription();
        this.practiceCount = requestDto.getPracticeCount();
    }

    // 업데이트 메소드
    public void update(PresentationRequestDto requestDto) {
        this.title = requestDto.getTitle();
        this.description = requestDto.getDescription();
        this.practiceCount = requestDto.getPracticeCount();
    }

    // 발표 수정 전용 메소드
    public void updateTitle(String title) {
        this.title = title;
    }
}
