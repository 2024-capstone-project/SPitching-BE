package djj.spitching_be.Service;

import djj.spitching_be.Domain.*;
import djj.spitching_be.Repository.EyeRepository;
import djj.spitching_be.Repository.GestureRepository;
import djj.spitching_be.Repository.PracticeRepository;
import djj.spitching_be.Repository.SttRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TotalScoreService {

    private final PracticeRepository practiceRepository;
    private final GestureRepository gestureRepository;
    private final EyeRepository eyeRepository;
    private final SttRepository sttRepository;

    /**
     * 특정 연습의 모든 점수를 조회하고 평균 점수를 계산하여 저장합니다.
     */
    @Transactional
    public Map<String, Object> calculateAndSaveTotalScore(Long practiceId) {
        // 연습 정보 조회
        Practice practice = practiceRepository.findById(practiceId)
                .orElseThrow(() -> new EntityNotFoundException("Practice not found with ID: " + practiceId));

        // 각 점수 조회
        Double scriptSimilarity = practice.getScriptSimilarity();

        // Gesture 점수 조회
        Optional<GestureData> gestureDataOpt = gestureRepository.findByPracticeId(practiceId);
        Integer gestureScore = gestureDataOpt.map(GestureData::getGestureScore).orElse(null);

        // Eye 점수 조회
        Optional<EyeData> eyeDataOpt = eyeRepository.findByPracticeId(practiceId);
        Integer eyeScore = eyeDataOpt.map(EyeData::getEyecontactScore).orElse(null);

        // STT 점수 조회
        Optional<SttData> sttDataOpt = sttRepository.findByPracticeId(practiceId);
        Double fluencyScore = sttDataOpt.map(SttData::getFluencyScore).orElse(null);

        // 결과 맵 생성
        Map<String, Object> scoreDetails = new HashMap<>();
        scoreDetails.put("scriptSimilarity", scriptSimilarity);
        scoreDetails.put("gestureScore", gestureScore);
        scoreDetails.put("eyeScore", eyeScore);
        scoreDetails.put("fluencyScore", fluencyScore);

        // 모든 점수가 있는지 확인
        if (scriptSimilarity == null || gestureScore == null || eyeScore == null || fluencyScore == null) {
            log.warn("Not all scores are available for practice ID: {}", practiceId);
            practice.setScoreCalculated(false);
            scoreDetails.put("allScoresAvailable", false);
            return scoreDetails;
        }

        // 점수 평균 계산
        double totalScore = (scriptSimilarity + gestureScore + eyeScore + fluencyScore) / 4.0;

        // 점수를 0.0 ~ 100.0 범위로 조정 (필요한 경우)
        if (totalScore > 100.0) {
            totalScore = 100.0;
        } else if (totalScore < 0.0) {
            totalScore = 0.0;
        }

        // 점수 저장
        practice.setTotalScore(totalScore);
        practice.setScoreCalculated(true);
        practiceRepository.save(practice);

        log.info("Total score calculated and saved for practice ID: {}, score: {}", practiceId, totalScore);

        // 결과 업데이트
        scoreDetails.put("totalScore", totalScore);
        scoreDetails.put("allScoresAvailable", true);

        return scoreDetails;
    }

    /**
     * 특정 연습의 전체 점수 정보를 가져옵니다. 점수가 계산되지 않았다면 계산을 시도합니다.
     */
    @Transactional
    public Map<String, Object> getTotalScoreDetails(Long practiceId) {
        Practice practice = practiceRepository.findById(practiceId)
                .orElseThrow(() -> new EntityNotFoundException("Practice not found with ID: " + practiceId));

        // 점수가 계산되지 않았다면 계산 시도
        Boolean scoreCalculated = practice.getScoreCalculated();
        if (scoreCalculated == null || !scoreCalculated) {
            return calculateAndSaveTotalScore(practiceId);
        }

        // 각 점수 조회
        Double scriptSimilarity = practice.getScriptSimilarity();

        // Gesture 점수 조회
        Optional<GestureData> gestureDataOpt = gestureRepository.findByPracticeId(practiceId);
        Integer gestureScore = gestureDataOpt.map(GestureData::getGestureScore).orElse(null);

        // Eye 점수 조회
        Optional<EyeData> eyeDataOpt = eyeRepository.findByPracticeId(practiceId);
        Integer eyeScore = eyeDataOpt.map(EyeData::getEyecontactScore).orElse(null);

        // STT 점수 조회
        Optional<SttData> sttDataOpt = sttRepository.findByPracticeId(practiceId);
        Double fluencyScore = sttDataOpt.map(SttData::getFluencyScore).orElse(null);

        // 결과 맵 생성
        Map<String, Object> scoreDetails = new HashMap<>();
        scoreDetails.put("practiceId", practiceId);
        scoreDetails.put("totalScore", practice.getTotalScore());
        scoreDetails.put("scriptSimilarity", scriptSimilarity);
        scoreDetails.put("gestureScore", gestureScore);
        scoreDetails.put("eyeScore", eyeScore);
        scoreDetails.put("fluencyScore", fluencyScore);
        scoreDetails.put("scoreCalculated", practice.getScoreCalculated());

        return scoreDetails;
    }

    /**
     * 특정 연습의 전체 점수만 간단히 조회합니다.
     */
    public Double getTotalScore(Long practiceId) {
        Practice practice = practiceRepository.findById(practiceId)
                .orElseThrow(() -> new EntityNotFoundException("Practice not found with ID: " + practiceId));

        Double totalScore = practice.getTotalScore();
        return totalScore != null ? totalScore : 0.0;
    }

    /**
     * 모든 피드백이 저장된 후 호출하여 전체 점수를 계산합니다.
     */
    @Transactional
    public void calculateTotalScoreIfAllAvailable(Long practiceId) {
        try {
            Map<String, Object> result = calculateAndSaveTotalScore(practiceId);
            if (Boolean.TRUE.equals(result.get("allScoresAvailable"))) {
                log.info("All scores are available, total score calculated: {}", result.get("totalScore"));
            } else {
                log.info("Not all scores are available yet, total score not calculated");
            }
        } catch (Exception e) {
            log.error("Error calculating total score", e);
        }
    }

    /**
     * practice ID로 해당 practice의 presentation을 찾고,
     * 그 presentation에 속한 모든 practice들의 세부 점수를 조회합니다.
     */
    public Map<String, Object> getPresentationScoresByPracticeId(Long practiceId) {
        // 1. practice ID로 practice 조회
        Practice practice = practiceRepository.findById(practiceId)
                .orElseThrow(() -> new EntityNotFoundException("Practice not found with id: " + practiceId));

        // 2. practice에서 presentation 정보 가져오기
        Presentation presentation = practice.getPresentation();
        Long presentationId = presentation.getId();

        // 3. 해당 presentation에 속한 모든 practice들 조회
        List<Practice> practices = practiceRepository.findByPresentationIdOrderByCreatedAtAsc(presentationId);

        // 4. 각 practice별 점수 데이터 수집
        List<Map<String, Object>> scoreList = new ArrayList<>();
        int period = 1;

        for (Practice p : practices) {
            Map<String, Object> scoreData = new HashMap<>();
            scoreData.put("period", String.valueOf(period));

            // Gesture Score 조회
            Optional<GestureData> gestureData = gestureRepository.findByPracticeId(p.getId());
            scoreData.put("gestureScore", gestureData.map(GestureData::getGestureScore).orElse(0));

            // Eye Contact Score 조회
            Optional<EyeData> eyeData = eyeRepository.findByPracticeId(p.getId());
            scoreData.put("eyeContactScore", eyeData.map(EyeData::getEyecontactScore).orElse(0));

            // Cosine Similarity (Script Similarity) 조회
            Double cosineSimilarity = p.getScriptSimilarity();
            scoreData.put("cosineSimilarity", cosineSimilarity != null ? cosineSimilarity.intValue() : 0);

            // STT Score 조회
            Optional<SttData> sttData = sttRepository.findByPracticeId(p.getId());
            Double fluencyScore = sttData.map(SttData::getFluencyScore).orElse(0.0);
            scoreData.put("sttScore", fluencyScore.intValue());

            scoreList.add(scoreData);
            period++;
        }

        // 5. 결과 구성
        Map<String, Object> result = new HashMap<>();
        result.put("presentationId", presentationId);
        result.put("score", scoreList);

        return result;
    }
}
