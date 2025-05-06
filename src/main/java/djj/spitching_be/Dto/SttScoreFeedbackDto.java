package djj.spitching_be.Dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SttScoreFeedbackDto {
    private Double fluencyScore; // 발표 유창성 점수
}
