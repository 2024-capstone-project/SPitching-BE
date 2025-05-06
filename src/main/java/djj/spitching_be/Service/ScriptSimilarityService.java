package djj.spitching_be.Service;

import djj.spitching_be.Domain.Practice;
import djj.spitching_be.Domain.Presentation;
import djj.spitching_be.Domain.PresentationSlide;
import djj.spitching_be.Dto.SttDto;
import djj.spitching_be.Repository.PracticeRepository;
import djj.spitching_be.Repository.PresentationRepository;
import djj.spitching_be.Repository.PresentationSlideRepository;
import djj.spitching_be.Domain.TextSimilarityUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScriptSimilarityService {

    private final PracticeRepository practiceRepository;
    private final PresentationRepository presentationRepository;
    private final PresentationSlideRepository presentationSlideRepository;
    private final TextSimilarityUtil textSimilarityUtil;

    /**
     * 전사본과 전체 대본의 유사도를 계산하고 저장
     */
    @Transactional
    public double calculateAndSaveScriptSimilarity(SttDto sttDto) {
        Long practiceId = sttDto.getPracticeId();
        Long presentationId = sttDto.getPresentationId();

        // 연습 정보 조회
        Practice practice = practiceRepository.findById(practiceId)
                .orElseThrow(() -> new EntityNotFoundException("Practice not found with ID: " + practiceId));

        // 발표의 모든 슬라이드 조회하여 전체 대본 생성
        List<PresentationSlide> slides = presentationSlideRepository.findByPresentationIdOrderBySlideNumber(presentationId);

        if (slides.isEmpty()) {
            log.warn("No slides found for presentation ID: {}", presentationId);
            return 0.0;
        }

        // 모든 슬라이드의 대본을 하나로 합쳐서 전체 대본 생성
        StringBuilder fullScript = new StringBuilder();
        for (PresentationSlide slide : slides) {
            String slideScript = slide.getScript();
            if (slideScript != null && !slideScript.trim().isEmpty()) {
                fullScript.append(slideScript).append(" ");
            }
        }

        String completeScript = fullScript.toString().trim();

        if (completeScript.isEmpty()) {
            log.warn("Complete script is empty for presentation ID: {}", presentationId);
            return 0.0;
        }

        // STT 트랜스크립트에서 발화 내용 추출
        List<Map<String, Object>> transcriptList = new ArrayList<>();
        for (Object segment : sttDto.getTranscript()) {
            if (segment instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> segmentMap = (Map<String, Object>) segment;
                transcriptList.add(segmentMap);
            }
        }

        String transcribedText = textSimilarityUtil.extractSpeechFromTranscript(transcriptList);

        if (transcribedText.isEmpty()) {
            log.warn("Transcribed text is empty for practice ID: {}", practiceId);
            return 0.0;
        }

        // 전체 대본과 전사본 간의 코사인 유사도 계산
        double similarity = textSimilarityUtil.calculateCosineSimilarity(completeScript, transcribedText);

        // 유사도가 음수이거나 NaN인 경우 0으로 처리
        if (Double.isNaN(similarity) || similarity < 0) {
            similarity = 0.0;
        }

        // 유사도가 1보다 큰 경우 1로 제한
        if (similarity > 1.0) {
            similarity = 1.0;
        }

        // 유사도를 Practice 테이블에 저장
        practice.setScriptSimilarity(similarity);
        practiceRepository.save(practice);

        log.info("Script similarity calculated and saved for practice ID: {}, similarity: {}",
                practiceId, similarity);

        return similarity;
    }

    /**
     * 특정 연습의 스크립트 유사도 조회
     */
    public double getScriptSimilarity(Long practiceId) {
        Practice practice = practiceRepository.findById(practiceId)
                .orElseThrow(() -> new EntityNotFoundException("Practice not found with ID: " + practiceId));

        Double similarity = practice.getScriptSimilarity();

        return similarity != null ? similarity : 0.0;
    }
}
