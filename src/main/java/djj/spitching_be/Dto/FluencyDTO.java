package djj.spitching_be.Dto;

import java.util.List;
import java.util.Map;

public class FluencyDTO {
    private Double fluencyScore;  // transcript에서 "발표 유창성 점수"
    private Map<String, Integer> fillerWords;  // 추임새 통계
    private Map<String, Double> silenceStats;  // 침묵 통계
    private List<TranscriptSegment> transcript;  // 전체 대본

    public FluencyDTO() {
    }

    public Double getFluencyScore() {
        return fluencyScore;
    }

    public void setFluencyScore(Double fluencyScore) {
        this.fluencyScore = fluencyScore;
    }

    public Map<String, Integer> getFillerWords() {
        return fillerWords;
    }

    public void setFillerWords(Map<String, Integer> fillerWords) {
        this.fillerWords = fillerWords;
    }

    public Map<String, Double> getSilenceStats() {
        return silenceStats;
    }

    public void setSilenceStats(Map<String, Double> silenceStats) {
        this.silenceStats = silenceStats;
    }

    public List<TranscriptSegment> getTranscript() {
        return transcript;
    }

    public void setTranscript(List<TranscriptSegment> transcript) {
        this.transcript = transcript;
    }
}
