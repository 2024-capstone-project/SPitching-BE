package djj.spitching_be.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LatestPresentationSummaryDto {
    private Long presentationId;
    private Long practiceId;  // 해당 presentation의 가장 최근 practice id
    private String title;
    private String description;
    private LocalDateTime created;
    private LocalDateTime lastPractice;
    private Integer practiceCount;
    private GraphDto graph;
    private List<TagDto> tags;
    private String firstSlideImageUrl;
}
