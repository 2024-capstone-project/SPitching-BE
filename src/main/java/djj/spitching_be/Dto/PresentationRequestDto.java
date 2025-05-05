package djj.spitching_be.Dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class PresentationRequestDto {

    // presentation에 데이터를 넣을 때 입력 요청값을 받음

    private String title;

    private String description;

    private Integer practiceCount;

    private String duration; // 발표 시간
}