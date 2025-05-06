package djj.spitching_be.Dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SttTranscriptSegmentDto {
    private Long start;
    private Long end;
    private String tag;
    private String result;
}
