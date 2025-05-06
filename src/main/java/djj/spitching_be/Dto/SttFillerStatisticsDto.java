package djj.spitching_be.Dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
class SttFillerStatisticsDto {
    private Integer eo; // 어
    private Integer eum; // 음
    private Integer geu; // 그
    private Integer totalFillerCount; // 불필요한 추임새 총 개수
    private Double fillerRatio; // 발화시간 대비 추임새 비율(%)
}
