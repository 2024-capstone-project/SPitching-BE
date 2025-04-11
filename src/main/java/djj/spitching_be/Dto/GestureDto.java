package djj.spitching_be.Dto;

import lombok.Data;

@Data
public class GestureDto {
    // 사용자 및 발표 ID (from AI 서버)
    private Long userId;
    private Long presentationId;
    private Long practiceId;  // 돌고돌아 AI 서버가 보내는 Practice ID

    // 제스처 분석 결과
    private int gestureScore;
    private int straightScore;
    private int explainScore;
    private int crossedScore;
    private int raisedScore;
    private int faceScore;
    private String videoUrl;
}
