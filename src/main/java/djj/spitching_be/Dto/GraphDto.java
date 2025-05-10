package djj.spitching_be.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class GraphDto {
    private Double currentScore;
    private List<Double> previousScores;  // 최근 -> 옛날 순서
    private Double eyeScore;
    private Double gestureScore;
    private Double sttScore;
    private Double cosineSimilarity;
}

