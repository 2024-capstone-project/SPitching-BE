package djj.spitching_be.Dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EyeDto {
    // 사용자 및 발표 ID (from AI 서버)
    private Long userId;
    private Long presentationId;
    private Long practiceId;  // 돌고돌아 AI 서버가 보내는 Practice ID
    private String videoUrl;

    // 시선 추적 분석 결과
    private int eyecontactScore;
}
