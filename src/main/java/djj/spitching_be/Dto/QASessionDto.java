package djj.spitching_be.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QASessionDto {
    private Long presentationId;
    private String presentationTitle;
    private List<Map<String, Object>> scripts;  // 대본 정보만 포함하는 단순화된 구조
    private List<ChatMessageDto> messages;
}
