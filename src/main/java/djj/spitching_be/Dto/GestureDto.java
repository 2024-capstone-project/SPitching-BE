package djj.spitching_be.Dto;

import lombok.Data;

@Data
public class GestureDto {
    // 사용자 및 발표 ID (from AI 서버)
    private Long userId;
    private Long presentationId;

    // 제스처 분석 결과
    private int gestureScore;
    private int straight_score;
    private int explain_score;
    private int crossed_score;
    private int raised_score;
    private int face_score;
    private String videoUrl;
}
