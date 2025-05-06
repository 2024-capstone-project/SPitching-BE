package djj.spitching_be.Dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SttSilenceStatisticsDto {
    private Integer silenceRatio; // 침묵비율(%)
    private Integer speakingRatio; // 발화비율(%)
    private Integer totalPresentationTime; // 전체발표시간(초)
}
