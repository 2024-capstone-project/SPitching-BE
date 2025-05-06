package djj.spitching_be.Dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class SttDto {
    // 사용자 및 발표 ID (from AI 서버)
    private Long userId;
    private Long presentationId;
    private Long practiceId;

    // 필러(추임새) 통계 정보
    private List<SttFillerStatisticsDto> statisticsFiller;

    // 침묵 통계 정보
    private List<SttSilenceStatisticsDto> statisticsSilence;

    // STT 점수 피드백
    private List<SttScoreFeedbackDto> sttScoreFeedback;

    // 트랜스크립트 정보
    private List<SttTranscriptSegmentDto> transcript;
}
