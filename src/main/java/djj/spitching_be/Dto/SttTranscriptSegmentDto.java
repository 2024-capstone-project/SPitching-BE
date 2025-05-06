package djj.spitching_be.Dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
class SttTranscriptSegmentDto {
    private Long start;
    private Long end;
    private String tag;
    private String result;
}
